package de.tum.in.cm.android.eddystonemanager.gui;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.cm.android.eddystonemanager.R;
import de.tum.in.cm.android.eddystonemanager.backend.BeaconRestfulService;
import de.tum.in.cm.android.eddystonemanager.controller.ApplicationController;
import de.tum.in.cm.android.eddystonemanager.controller.MainController;
import de.tum.in.cm.android.eddystonemanager.controller.ProductionController;
import de.tum.in.cm.android.eddystonemanager.utils.app.AppStorage;
import de.tum.in.cm.android.eddystonemanager.utils.general.GUIUtils;

public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getSimpleName();
  public static ApplicationController ACTIVE_CONTROLLER;
  private static final int PERMISSION_MULTIPLE_REQUEST = 1;
  private static Activity ACTIVITY;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ACTIVITY = this;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    if ((MainController.SETTING.isUserStudy() || MainController.SETTING.isTest()) &&
            !BeaconRestfulService.isAvailable()) {
      Log.d(TAG, "Beacon REST service is not available");
      closeApp();
    } else {
      boolean proceedFurtherProcessing = true;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        List<String> requiredPermissions = new ArrayList<>();
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        requiredPermissions.add(Manifest.permission.CAMERA);
        requiredPermissions.add(Manifest.permission.RECORD_AUDIO);
        String[] permissionsToRequest = getPermissionsToRequest(requiredPermissions);
        if (permissionsToRequest.length > 0) {
          proceedFurtherProcessing = false;
          requestPermissions(permissionsToRequest, PERMISSION_MULTIPLE_REQUEST);
        } else {
          ProductionController.START_LOCATION_SENSING = true;
        }
      }
      if (proceedFurtherProcessing) {
        startApplication();
      }
    }
  }

  private String[] getPermissionsToRequest(List<String> requiredPermissions) {
    List<String> requestedPermissions = new ArrayList<>();
    for (String requiredPermission : requiredPermissions) {
      if (checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
        requestedPermissions.add(requiredPermission);
      }
    }
    return requestedPermissions.toArray(new String[requestedPermissions.size()]);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case PERMISSION_MULTIPLE_REQUEST: {
        if (grantResults.length > 0) {
          boolean fineLocationPermission = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
          if (fineLocationPermission) {
            ProductionController.START_LOCATION_SENSING = true;
          } else {
            GUIUtils.showFinishingAlertDialog(this,
                    "Location access is required",
                    "App will close since the permission was denied");
          }
          startApplication();
        }
      }
    }
  }

  private void startApplication() {
    AppStorage.createDataFolder();
    ACTIVE_CONTROLLER = MainController.getInstance();
    setContentView(R.layout.activity_main);
    swapFragment(getFragmentManager(), ACTIVE_CONTROLLER.getBeaconListFragment());
  }

  public static void swapFragment(FragmentManager fragmentManager, Fragment fragment) {
    if (fragment != null) {
      FragmentTransaction ft = fragmentManager.beginTransaction();
      ft.replace(R.id.fragmentContainer, fragment, fragment.getClass().getName());
      ft.addToBackStack(fragment.getClass().getName());
      ft.commitAllowingStateLoss();
    } else {
      Log.e("Fragment", "Fragment is null");
    }
  }

  public static void closeApp() {
    GUIUtils.showCloseAlertDialog(MainActivity.getInstance(),
            "Beacon REST API",
            "The Beacon REST API is currently not available, thus the application will be closed.");
  }

  public static Activity getInstance() {
    return ACTIVITY;
  }

  public static int getConfigResource() {
    if (MainController.SETTING.isDemo()) {
      return R.raw.config_demo;
    } else {
      return R.raw.config;
    }
  }

  public static int getCertificate() {
    return R.raw.server;
  }

  public static int getDefaultBeaconConfig() {
    return R.raw.default_beacon_config;
  }

}