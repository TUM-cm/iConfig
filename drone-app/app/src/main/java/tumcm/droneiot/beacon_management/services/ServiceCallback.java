package tumcm.droneiot.beacon_management.services;

import android.app.ProgressDialog;

import tumcm.droneiot.MainActivity;
import tumcm.droneiot.beacon_management.data.BeaconObject;
import tumcm.droneiot.beacon_management.utils.general.GUIUtils;

public class ServiceCallback {

  private final BeaconDataService beaconDataService;
  private final BeaconRegisterService beaconRegisterService;
  private final ProgressDialog progressDialog;
  private final BeaconSynchronizationService beaconSynchronizationService;

  public ServiceCallback(BeaconDataService beaconDataService,
                         BeaconRegisterService beaconRegisterService,
                         ProgressDialog progressDialog,
                         BeaconSynchronizationService beaconSynchronizationService) {
    this.beaconDataService = beaconDataService;
    this.beaconRegisterService = beaconRegisterService;
    this.progressDialog = progressDialog;
    this.beaconSynchronizationService = beaconSynchronizationService;
  }

  public void identifyAction() {
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

}
