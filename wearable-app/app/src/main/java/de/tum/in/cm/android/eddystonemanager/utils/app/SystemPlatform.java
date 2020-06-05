package de.tum.in.cm.android.eddystonemanager.utils.app;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.BeaconUtils;

public class SystemPlatform {

  private static final String TAG = SystemPlatform.class.getSimpleName();

  private static final int WEARABLE_SCREEN_DENSITY_THRESHOLD = 200;
  private static final int WEARABLE_SCREEN_THRESHOLD_WIDTH = 800;
  private static final int WEARABLE_SCREEN_THRESHOLD_HEIGHT = 600;

  public static boolean isWearable() {
    boolean wearable = false;
    Context context = MainActivity.getInstance();
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics metrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(metrics);
    if (metrics.widthPixels > metrics.heightPixels &&
            metrics.densityDpi < WEARABLE_SCREEN_DENSITY_THRESHOLD &&
            metrics.widthPixels < WEARABLE_SCREEN_THRESHOLD_WIDTH &&
            metrics.heightPixels < WEARABLE_SCREEN_THRESHOLD_HEIGHT) {
      wearable = true;
    }
    return wearable;
  }

  public static void printDeviceInfo() {
    StringBuilder stringBuilder = new StringBuilder("Manufacturer: ");
    stringBuilder.append(Build.MANUFACTURER);
    stringBuilder.append(BeaconUtils.LINE_SEPARATOR);
    stringBuilder.append(", Model: ");
    stringBuilder.append(Build.MODEL);
    stringBuilder.append(BeaconUtils.LINE_SEPARATOR);
    stringBuilder.append(", Product: ");
    stringBuilder.append(Build.PRODUCT);
    stringBuilder.append(BeaconUtils.LINE_SEPARATOR);
    stringBuilder.append(", Device: ");
    stringBuilder.append(Build.DEVICE);
    Log.d(TAG, "Device info: " + stringBuilder.toString());
  }

}
