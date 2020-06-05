package tumcm.droneiot.beacon_management.utils.general;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

  private static final String TAG = ImageUtils.class.getSimpleName();
  // Default landscape
  private static final int WIDTH = 1024;
  private static final int HEIGHT = 768;
  private static final int QUALITY = 85;

  public void inPlaceAdjustImage(final File file) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        processImage(file);
      }
    };
    AsyncTask.execute(runnable);
  }

  private void processImage(File file) {
    Bitmap scaledBitmap = null;
    String path = file.getPath();
    int width = WIDTH;
    int height = HEIGHT;
    Bitmap unscaledBitmap = ImageScalingUtilities.decodeFile(path, width, height,
            ImageScalingUtilities.ScalingLogic.FIT);
    if (!(unscaledBitmap.getWidth() <= width && unscaledBitmap.getHeight() <= height)) {
      if (unscaledBitmap.getWidth() < unscaledBitmap.getHeight()) {
        // Portrait
        int tmpWidth = WIDTH;
        width = height;
        height = tmpWidth;
      }
      scaledBitmap = ImageScalingUtilities.createScaledBitmap(unscaledBitmap, width, height,
              ImageScalingUtilities.ScalingLogic.FIT);
    } else {
      unscaledBitmap.recycle();
    }
    try {
      FileOutputStream fos = new FileOutputStream(file);
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, fos);
      fos.flush();
      fos.close();
    } catch (FileNotFoundException e) {
      Log.d(TAG, "adjustImage", e);
    } catch (IOException e) {
      Log.d(TAG, "adjustImage", e);
    }
  }

}
