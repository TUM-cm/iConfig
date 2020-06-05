package de.tum.in.cm.android.eddystonemanager.utils.beacon;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.bluvision.beeks.sdk.helper.EncryptUtilis;
import com.bluvision.blu.BuildConfig;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.UUID;

import de.tum.in.cm.android.eddystonemanager.data.BeaconUnregistered;
import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.services.BeaconDataService;
import de.tum.in.cm.android.eddystonemanager.utils.general.Similarity;

public class BeaconUtils {

  public static final String TAG = BeaconUtils.class.getSimpleName();

  // Altitude of Garching: 475m
  // SELECT avg(air_pressure_477) FROM `weather_additional_data` = 973.19f > 154 m
  // SELECT avg(air_pressure_NN) FROM `weather_additional_data` = 1032.55f > 1011 m
  // PRESSURE_STANDARD_ATMOSPHERE = 1013.25f > 492 m
  public static final float PRESSURE_STANDARD_ATMOSPHERE = 1011.10f;
  public static final int LENGTH_UID = 16;
  public static final int LENGTH_NAMESPACE = 10;
  public static final int LENGTH_INSTANCE_ID = 6;
  public static final int ONE_MINUTE = 1000 * 60 * 1;
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private final BeaconDataService beaconDataService;
  private final LocationManager locationManager;
  private final SensorManager sensorManager;

  public BeaconUtils(BeaconDataService beaconDataService) {
    this.beaconDataService = beaconDataService;
    this.locationManager = (LocationManager) MainActivity.getInstance()
            .getSystemService(MainActivity.getInstance().LOCATION_SERVICE);
    this.sensorManager = (SensorManager) MainActivity.getInstance()
            .getSystemService(MainActivity.getInstance().SENSOR_SERVICE);
  }

  public static EddystoneUuid generateRandomUid() {
    UUID randomUid = UUID.randomUUID();
    ByteBuffer byteBuffer = ByteBuffer.allocate(LENGTH_UID);
    byteBuffer.putLong(randomUid.getMostSignificantBits());
    byteBuffer.putLong(randomUid.getLeastSignificantBits());
    byteBuffer.rewind();

    byte[] nameSpace = new byte[LENGTH_NAMESPACE];
    byteBuffer.get(nameSpace, 0, LENGTH_NAMESPACE);
    String nameSpaceStr = EncryptUtilis.b2hexString(nameSpace, 0,
            nameSpace.length, BuildConfig.FLAVOR);

    byte[] instanceId = new byte[LENGTH_INSTANCE_ID];
    byteBuffer.get(instanceId, 0, LENGTH_INSTANCE_ID);
    String instanceIdStr = EncryptUtilis.b2hexString(instanceId, 0,
            instanceId.length, BuildConfig.FLAVOR);

    return new EddystoneUuid(nameSpaceStr, nameSpace, instanceIdStr, instanceId);
  }

  public void startLocationSensing() {
    try {
      long minTime = 2 * 1000; // every 2 seconds
      float minDistance = 0;

      // Initial location
      Location lastNetworkLocation = getLocationManager()
              .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      Location lastGpsLocation = getLocationManager()
              .getLastKnownLocation(LocationManager.GPS_PROVIDER);

      if (lastNetworkLocation != null && lastGpsLocation != null) {
        if (isBetterLocation(lastNetworkLocation, lastGpsLocation)) {
          getBeaconDataService().setCurrentLocation(lastNetworkLocation);
        } else {
          getBeaconDataService().setCurrentLocation(lastGpsLocation);
        }
      } else if (lastNetworkLocation != null) {
        getBeaconDataService().setCurrentLocation(lastNetworkLocation);
      } else if (lastGpsLocation != null) {
        getBeaconDataService().setCurrentLocation(lastGpsLocation);
      }

      // Get position updates
      getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
              minTime,
              minDistance,
              getLocationListener());
      getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER,
              minTime,
              minDistance,
              getLocationListener());
    } catch (SecurityException e) {
      Log.e(TAG, "startLocationSensing", e);
    }
  }

  public void stopLocationSensing() {
    try {
      getLocationManager().removeUpdates(getLocationListener());
    } catch (SecurityException e) {
      Log.e(TAG, "stopLocationSensing", e);
    }
  }

  public void startAltitudeSensing() {
    getSensorManager().registerListener(getSensorEventListener(),
            getSensorManager().getDefaultSensor(Sensor.TYPE_PRESSURE),
            SensorManager.SENSOR_DELAY_NORMAL);
  }

  public void stopAltitudeSensing() {
    getSensorManager().unregisterListener(getSensorEventListener());
  }

  private LocationListener locationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      Location currentBestLocation = getBeaconDataService().getCurrentLocation();
      if (isBetterLocation(location, currentBestLocation)) {
        getBeaconDataService().setCurrentLocation(location);
      }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
  };

  private SensorEventListener sensorEventListener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent event) {
      float pressure_value;
      float altitude;
      if (Sensor.TYPE_PRESSURE == event.sensor.getType()) {
        pressure_value = event.values[0];
        altitude = SensorManager.getAltitude(PRESSURE_STANDARD_ATMOSPHERE, pressure_value);
        getBeaconDataService().setCurrentAltitude(altitude);
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
  };

  // Source: https://developer.android.com/guide/topics/location/strategies.html
  private boolean isBetterLocation(Location location, Location currentBestLocation) {
    if (currentBestLocation == null) {
      // A new location is always better than no location
      return true;
    }

    // Check whether the new location fix is newer or older
    long timeDelta = location.getTime() - currentBestLocation.getTime();
    boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
    boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
    boolean isNewer = timeDelta > 0;

    // If it's been more than two minutes since the current location, use the new location
    // because the user has likely moved
    if (isSignificantlyNewer) {
      return true;
      // If the new location is more than two minutes older, it must be worse
    } else if (isSignificantlyOlder) {
      return false;
    }

    // Check whether the new location fix is more or less accurate
    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
    boolean isLessAccurate = accuracyDelta > 0;
    boolean isMoreAccurate = accuracyDelta < 0;
    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

    // Check if the old and new location are from the same provider
    boolean isFromSameProvider = isSameProvider(location.getProvider(),
            currentBestLocation.getProvider());

    // Determine location quality using a combination of timeliness and accuracy
    if (isMoreAccurate) {
      return true;
    } else if (isNewer && !isLessAccurate) {
      return true;
    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
      return true;
    }
    return false;
  }

  /**
   * Checks whether two providers are the same
   */
  private boolean isSameProvider(String provider1, String provider2) {
    if (provider1 == null) {
      return provider2 == null;
    }
    return provider1.equals(provider2);
  }

  public static final Comparator<BeaconUnregistered> RSSI_COMPARATOR = new Comparator<BeaconUnregistered>() {
    @Override
    public int compare(BeaconUnregistered b1, BeaconUnregistered b2) {
      return -Double.compare(b1.getRssiFilter().getRssi(), b2.getRssiFilter().getRssi());
    }
  };

  public static final Comparator<Similarity> SIMILARITY_COMPARATOR = new Comparator<Similarity>() {
    @Override
    public int compare(Similarity lhs, Similarity rhs) {
      return -Double.compare(lhs.getSimilarity(), rhs.getSimilarity());
    }
  };

  public static String byteArrayToHex(final byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for (final byte b : a) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }

  public static String restrictToDigits(String value, boolean allowPoint) {
    StringBuilder strBuild = new StringBuilder();
    char c;
    for(int i = 0; i < value.length(); i++) {
      c = value.charAt(i);
      if (Character.isDigit(c)) {
        strBuild.append(c);
      } else if (allowPoint && c == '.') {
        strBuild.append(c);
      }
    }
    return strBuild.toString();
  }

  public static String pad(String value, int length) {
    if (value.length() == 0) {
      StringBuilder strBuilder = new StringBuilder();
      for(int i = 0; i < length; i++) {
        strBuilder.append("0");
      }
      return strBuilder.toString();
    }
    int intVal = Integer.valueOf(value);
    return String.format("%0" + length + "d", intVal);
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private LocationManager getLocationManager() {
    return locationManager;
  }

  private SensorManager getSensorManager() {
    return sensorManager;
  }

  private LocationListener getLocationListener() {
    return locationListener;
  }

  private SensorEventListener getSensorEventListener() {
    return sensorEventListener;
  }

}
