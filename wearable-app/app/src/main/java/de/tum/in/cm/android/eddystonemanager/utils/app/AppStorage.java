package de.tum.in.cm.android.eddystonemanager.utils.app;

import android.os.Environment;

import java.io.File;

public class AppStorage {

  public static final String STORAGE_FOLDER_NAME = "BeaconManager";
  public static final String STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
          File.separator + STORAGE_FOLDER_NAME + File.separator;
  public static final String CONFIG_FILENAME = "config.ini";
  public static final String CONFIG_DEMO_FILENAME = "config_demo.ini";
  public static final String CONFIG_TEST_FILENAME = "test.ini";
  public static final String BEACONS_TO_SYNCHRONIZE_FILENAME = "BeaconsToSynchronize.ser";
  public static final String DEFAULT_BEACON_CONFIG_FILENAME = "DefaultBeaconConfig.ser";

  public static void createDataFolder() {
    File directory = new File(AppStorage.STORAGE_PATH);
    if (!directory.exists()) {
      directory.mkdirs();
    }
  }

  public static float getAppMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long freeSize = runtime.freeMemory();
    long totalSize = runtime.totalMemory();
    long usedSize = totalSize - freeSize;
    return (float) usedSize / 1048576L;
  }

}
