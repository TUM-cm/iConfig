package de.tum.in.cm.android.eddystonemanager.configurator;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BluetoothGattListener extends BluetoothGattCallback {

  private static final String TAG = BluetoothGattListener.class.getSimpleName();

  private final EddystoneURLGattService eddystoneURLGattService;
  private final List<String> devicesDiscovered;

  public BluetoothGattListener(EddystoneURLGattService eddystoneURLGattService) {
    this.eddystoneURLGattService = eddystoneURLGattService;
    this.devicesDiscovered = new ArrayList<>();
  }

  @Override
  public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
    super.onConnectionStateChange(gatt, status, newState);
    Log.d(TAG, "onConnectionStateChange");
    String mac = gatt.getDevice().getAddress();
    boolean deviceDiscovered = this.devicesDiscovered.contains(mac);
    if (!deviceDiscovered && (
            status == BluetoothProfile.STATE_DISCONNECTED && newState == BluetoothProfile.STATE_CONNECTED)) {
      this.devicesDiscovered.add(mac);
      gatt.discoverServices();
    }
  }

  @Override
  public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    super.onServicesDiscovered(gatt, status);
    Log.d(TAG, "onServicesDiscovered");
    if (getEddystoneURLGattService().isServiceAvailable(gatt)) {
      //getEddystoneURLGattService().readUrl(gatt);
      getEddystoneURLGattService().setUrl(gatt);
    }
  }

  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    byte[] value = characteristic.getValue();
    String strValue = EddystoneURLGattService.parseUrl(value);
    Log.d(TAG, strValue);
    gatt.disconnect();
  }

  @Override
  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    byte[] value = characteristic.getValue();
    String strValue = EddystoneURLGattService.parseUrl(value);
    Log.d(TAG, strValue);
    gatt.disconnect();
    this.devicesDiscovered.clear();
  }

  private EddystoneURLGattService getEddystoneURLGattService() {
    return this.eddystoneURLGattService;
  }

}
