package tumcm.droneiot.beacon_management.utils.general;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import tumcm.droneiot.beacon_management.services.BeaconRegisterService;

public class GUIUtils {

  private static AlertDialog identifyDialog;

  private static AlertDialog.Builder createFinishAlertDialog(final Activity activity,
                                                             String title, String message) {
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            activity.finish();
            break;
        }
      }
    };
    final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
    dialog.setTitle(title);
    dialog.setMessage(message);
    dialog.setPositiveButton("OK", dialogClickListener);
    return dialog;
  }

  public static void showFinishingAlertDialog(final Activity activity, String title, String message) {
    final AlertDialog.Builder dialog = createFinishAlertDialog(activity, title, message);
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        dialog.show();
      }
    });
  }

  public static void showCloseAlertDialog(final Activity activity, String title, String message) {
    final AlertDialog.Builder dialog = createFinishAlertDialog(activity, title, message);
    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        activity.finishAffinity();
      }
    });
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        dialog.show();
      }
    });
  }

  public static void showOKAlertDialog(final Activity activity, String title, String message) {
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            break;
        }
      }
    };
    final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
    dialog.setTitle(title);
    dialog.setMessage(message);
    dialog.setPositiveButton("OK", dialogClickListener);
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        dialog.show();
      }
    });
  }

  public static void showIdentifyDialog(Activity activity, final BeaconRegisterService service) {
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            service.getBeaconRegisterFragment().enableRegisterAction();
            break;
          case DialogInterface.BUTTON_NEGATIVE:
            if(service.getBeaconDataService().getActiveBeacon().isBroken()) {
              service.registerBrokenBeacon();
            }
            break;
        }
      }
    };
    final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
    dialog.setMessage("Did you saw a light at the desired beacon?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener);
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        identifyDialog = dialog.show();
      }
    });
  }

  public static AlertDialog getIdentifyDialog() {
    return identifyDialog;
  }

}
