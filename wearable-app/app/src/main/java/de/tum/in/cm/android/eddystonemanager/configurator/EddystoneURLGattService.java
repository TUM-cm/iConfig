package de.tum.in.cm.android.eddystonemanager.configurator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.bluvision.beeks.sdk.domainobjects.ConfigurableBeacon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;

public class EddystoneURLGattService {

  private static final String TAG = EddystoneURLGattService.class.getSimpleName();

  public static final String URL_CONFIG_SERVICE = "ee0c2080-8786-40ba-ab96-99b91ac981d8";
  public static final UUID URL_READ_WRITE_CHARACTERISTIC = UUID.fromString("ee0c2084-8786-40ba-ab96-99b91ac981d8");
  public static final UUID URL_LOCK_STATE_CHARACTERISTIC = UUID.fromString("ee0c2081-8786-40ba-ab96-99b91ac981d8");
  public static final UUID UNLOCK_CHARACTERISTIC = UUID.fromString("ee0c2083-8786-40ba-ab96-99b91ac981d8");

  public static String[] PREFIX_ENCODINGS = {"http://www.", "https://www.", "http://", "https://"};
  public static String[] URL_ENCODINGS = {".com/", ".org/", ".edu/", ".net/", ".info/", ".biz/", ".gov/",
          ".com", ".org", ".edu", ".net", ".info", ".biz", ".gov"};

  private String url;

  public static void test(ConfigurableBeacon beacon) {
    BluetoothDevice bluetoothDevice = beacon.getDevice();
    EddystoneURLGattService eddystoneURLGattService = new EddystoneURLGattService();
    eddystoneURLGattService.setUrl("http://abc");
    BluetoothGattListener bluetoothGattListener = new BluetoothGattListener(eddystoneURLGattService);
    bluetoothDevice.connectGatt(MainActivity.getInstance(), true, bluetoothGattListener);
  }

  public void printCharacteristics(BluetoothGattService gattService) {
    Log.d(TAG, "Characteristics of gatt service: " + gattService.getUuid().toString());
    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
      Log.d(TAG, gattCharacteristic.getUuid().toString());
    }
  }

  public void readUrl(BluetoothGatt gatt) {
    BluetoothGattService gattService = gatt.getService(UUID.fromString(URL_CONFIG_SERVICE));
    BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(URL_READ_WRITE_CHARACTERISTIC);
    gatt.readCharacteristic(gattCharacteristic);
  }

  public void setUnlock(BluetoothGatt gatt) {
    BluetoothGattService gattService = gatt.getService(UUID.fromString(URL_CONFIG_SERVICE));
    BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(UNLOCK_CHARACTERISTIC);
    gattCharacteristic.setValue(new byte[] {1});
    gatt.writeCharacteristic(gattCharacteristic);
  }

  public void checkLockState(BluetoothGatt gatt) {
    BluetoothGattService gattService = gatt.getService(UUID.fromString(URL_CONFIG_SERVICE));
    BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(URL_LOCK_STATE_CHARACTERISTIC);
    gatt.readCharacteristic(gattCharacteristic);
  }

  public void setUrl(BluetoothGatt gatt) {
    BluetoothGattService gattService = gatt.getService(UUID.fromString(URL_CONFIG_SERVICE));
    BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(URL_READ_WRITE_CHARACTERISTIC);
    byte[] urlBytes = convertUrl(url);
    gattCharacteristic.setValue(urlBytes);
    boolean success = gatt.writeCharacteristic(gattCharacteristic);
    if (success) {
      Log.d(TAG, "Set url");
    } else {
      Log.d(TAG, "Url not set");
    }
  }

  private byte[] convertUrl(String url) {
    List<Byte> urlBytes = new ArrayList<>();

    for (int i = 0; i < PREFIX_ENCODINGS.length; i++) {
      if (url.startsWith(PREFIX_ENCODINGS[i])) {
        urlBytes.add((byte) i); // prefix
        url = url.substring(PREFIX_ENCODINGS[i].length());
        break;
      }
    }

    for (int i = 0; i < URL_ENCODINGS.length; i++) {
      if (url.contains(URL_ENCODINGS[i])) {
        int startIndex = url.indexOf(URL_ENCODINGS[i]);
        int endIndex = startIndex + URL_ENCODINGS[i].length();

        String beforeUrl = url.substring(0, startIndex);
        String afterUrl = url.substring(endIndex);

        urlBytes.addAll(convertBytes(beforeUrl.getBytes()));
        urlBytes.add((byte) i); // url
        if (afterUrl.length() > 0) {
          urlBytes.addAll(convertBytes(afterUrl.getBytes()));
        }
        url = "";
        break;
      }
    }

    if (url.length() > 0) {
      urlBytes.addAll(convertBytes(url.getBytes()));
    }

    byte[] byteArray = new byte[urlBytes.size()];
    for (int index = 0; index < urlBytes.size(); index++) {
      byteArray[index] = urlBytes.get(index);
    }
    return byteArray;
  }

  private List<Byte> convertBytes(byte[] data) {
    List<Byte> byteList = new ArrayList<>();
    for (byte b : data) {
      byteList.add(b);
    }
    return byteList;
  }

  public static String toString(byte[] data) {
    StringBuilder string = new StringBuilder();
    for (byte b : data) {
      if (string.length() > 0) {
        string.append(",");
      }
      string.append(b);
    }
    return string.toString();
  }

  public boolean isServiceAvailable(BluetoothGatt gatt) {
    List<BluetoothGattService> gattServices = gatt.getServices();
    for (BluetoothGattService gattService : gattServices) {
      if (gattService.getUuid().toString().equals(URL_CONFIG_SERVICE)) {
        return true;
      }
    }
    return false;
  }

  public static String parseUrl(byte[] frameBytes) {
    int lenCheck = 3;
    int indexPrefix = 0;

    // URL Prefix
    final String urlPrefix;
    if (frameBytes.length >= lenCheck) {
      final int prefixIndex = frameBytes[indexPrefix] & 0xff;
      if (prefixIndex >= PREFIX_ENCODINGS.length) {
        return null;
      }
      urlPrefix = PREFIX_ENCODINGS[prefixIndex];
    } else {
      urlPrefix = "";
    }
    // Encoded url
    String urlPostfix = "";
    for (int i = indexPrefix+1; i < frameBytes.length; i++ ) {
      urlPostfix += decodeUrlChar(frameBytes, i);
    }
    return (urlPrefix + urlPostfix);
  }

  private static String decodeUrlChar(
          final byte[] bytes,
          final int position ) {
    final int val = bytes[ position ] & 0xff;

    if ( val <= 13 ) {
      return URL_ENCODINGS[ val ];
    } else if ( val <= 32 ) {
      return "";
    } else if ( val >= 127 ) {
      return "";
    } else {
      return Character.toString( ( char ) val );
    }
  }

  public void setUrl(String url) {
    this.url = url;
  }

  private String getUrl() {
    return this.url;
  }

}
