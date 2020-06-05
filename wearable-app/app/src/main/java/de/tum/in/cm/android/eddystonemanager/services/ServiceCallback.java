package de.tum.in.cm.android.eddystonemanager.services;

import android.app.ProgressDialog;

import de.tum.in.cm.android.eddystonemanager.controller.SpeechController;
import de.tum.in.cm.android.eddystonemanager.data.BeaconObject;
import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.speech.SpeechAction;
import de.tum.in.cm.android.eddystonemanager.utils.general.GUIUtils;

public class ServiceCallback {

  private final BeaconDataService beaconDataService;
  private final BeaconRegisterService beaconRegisterService;
  private final ProgressDialog progressDialog;
  private final BeaconSynchronizationService beaconSynchronizationService;
  private SpeechController speechController;

  public ServiceCallback(BeaconDataService beaconDataService,
                         BeaconRegisterService beaconRegisterService,
                         ProgressDialog progressDialog,
                         BeaconSynchronizationService beaconSynchronizationService) {
    this.beaconDataService = beaconDataService;
    this.beaconRegisterService = beaconRegisterService;
    this.progressDialog = progressDialog;
    this.beaconSynchronizationService = beaconSynchronizationService;
  }

  public void verifyConfigAction(boolean result) {
    MainActivity.getInstance().runOnUiThread(new Runnable() {
      @Override
      public void run () {
        getProgressDialog().dismiss();
      }
    });
    String title = "Beacon Config";
    String message;
    if (result) {
      message = "Verify of beacon config was successful.";
    } else {
      message = "Verify of beacon config was not successful.";
    }
    GUIUtils.showOKAlertDialog(getBeaconRegisterService()
                    .getBeaconRegisterFragment().getActivity(), title, message);
  }

  public void identifyAction() {
    if (getSpeechController() != null && getSpeechController().isBeaconSelected()) {
      getSpeechController().speak("Did you saw a light at the desired beacon (Yes or No)?",
              SpeechAction.ConfirmIdentify);
    }
    MainActivity.getInstance().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        getProgressDialog().dismiss();
      }
    });
    GUIUtils.showIdentifyDialog(getBeaconRegisterService()
            .getBeaconRegisterFragment().getActivity(), getBeaconRegisterService());
  }

  public void registerAction(BeaconObject beacon, boolean result) {
    beacon.createImage();
    getBeaconDataService().addBeaconsToSynchronize(beacon);
    getBeaconSynchronizationService().start();
    getProgressDialog().dismiss();
    getBeaconRegisterService().guiActionAfterRegisterBeacon(result);
  }

  public void updateAction(BeaconObject beacon, boolean result) {
    String mac = beacon.getMac();
    if (result) {
      getBeaconDataService().addBeaconsToSynchronize(beacon);
      getBeaconDataService().getBeaconConfigsToUpdate().remove(mac);
      getBeaconDataService().getBeaconsWaitForUpdate().remove(mac);
      getBeaconSynchronizationService().start();
    } else {
      getBeaconDataService().getBeaconsWaitForUpdate().remove(mac);
    }
  }

  public void setSpeechController(SpeechController speechController) {
    this.speechController = speechController;
  }

  private BeaconRegisterService getBeaconRegisterService() {
    return this.beaconRegisterService;
  }

  private ProgressDialog getProgressDialog() {
    return this.progressDialog;
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private BeaconSynchronizationService getBeaconSynchronizationService() {
    return this.beaconSynchronizationService;
  }

  private SpeechController getSpeechController() {
    return this.speechController;
  }

}
