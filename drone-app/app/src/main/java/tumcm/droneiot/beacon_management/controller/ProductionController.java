package tumcm.droneiot.beacon_management.controller;

import com.bluvision.beeks.sdk.domainobjects.Beacon;

import tumcm.droneiot.MainActivity;
import tumcm.droneiot.beacon_management.configurator.Action;
import tumcm.droneiot.beacon_management.data.BeaconUnregistered;

public class ProductionController extends ApplicationController {

  private String targetMacAddress;

  @Override
  public void onCreate() {
    super.onCreate();
    getBeaconScanHandler().doBindService();
    getBeaconDataService().loadBeaconsToSynchronize();
    getBeaconUtils().startAltitudeSensing();
    getBeaconSynchronizationService().start();
    if (START_LOCATION_SENSING) {
      getBeaconUtils().startLocationSensing();
    }
  }

  @Override
  public void onDestroy() {
    getBeaconManager().stopScan();
    getBeaconScanHandler().getScanService().stopScan();
    getBeaconScanHandler().doUnbindService();
    getBeaconUtils().stopLocationSensing();
    getBeaconUtils().stopAltitudeSensing();
    getBeaconUpdateService().stop();
    getBeaconSynchronizationService().stop();
    getExecutorService().getScheduledExecutorService().shutdownNow();
    getBeaconDataService().saveBeaconsToSynchronize();
  }

  @Override
  public void onBeaconFound(final Beacon beacon) {
    getBeaconDataService().addBeacon(beacon);
    getBeaconListFragment().updateBeaconList(beacon.getDevice().getAddress(), beacon.getRssi());
    beacon.addOnBeaconChangeListener(getBeaconChangeListener());
    getBeaconUpdateService().start(getAppConfig().getUpdateBeaconScanTime(int.class));
    automaticBeaconRegister();
  }

  private void automaticBeaconRegister() {
    if (getTargetMacAddress() != null) {
      BeaconUnregistered beaconUnregistered = getBeaconDataService()
              .getUnregisteredBeaconsMap().get(getTargetMacAddress());
      if (beaconUnregistered != null) {
        switchToBeaconRegister(beaconUnregistered);
      }
    }
  }

  @Override
  public void switchToBeaconRegister(BeaconUnregistered beaconUnregistered) {
    getBeaconDataService().setActiveBeacon(beaconUnregistered,
            getBeaconDataService().getDefaultBeaconConfig(), Action.Register,
            getServiceCallback(), getAppConfig());
    getBeaconRegisterFragment().init(getBeaconRegisterService());
    MainActivity.swapFragment(getBeaconListFragment().getFragmentManager(),
            getBeaconRegisterFragment());
  }

  @Override
  public void setTargetBeaconMac(String macAddress) {
    if (!macAddress.contains(":")) {
      macAddress = constructMac(macAddress);
    }
    this.targetMacAddress = macAddress.toUpperCase();
    automaticBeaconRegister();
  }

  public String getTargetMacAddress() {
    return this.targetMacAddress;
  }

  private String constructMac(String mac) {
    StringBuilder str = new StringBuilder(mac);
    int pos = 2;
    int idx = str.length() - pos;
    while (idx > 0){
      str.insert(idx, ":");
      idx = idx - pos;
    }
    return str.toString();
  }

}
