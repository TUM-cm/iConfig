package de.tum.in.cm.android.eddystonemanager.configurator;

import com.bluvision.beeks.sdk.constants.Range;
import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.interfaces.OnBeaconChangeListener;

import de.tum.in.cm.android.eddystonemanager.gui.BeaconListFragment;
import de.tum.in.cm.android.eddystonemanager.services.BeaconDataService;

public class BeaconChangeListener implements OnBeaconChangeListener {

  private final BeaconDataService beaconDataService;
  private final BeaconListFragment beaconListFragment;

  public BeaconChangeListener(BeaconDataService beaconDataService,
                              BeaconListFragment beaconListFragment) {
    this.beaconDataService = beaconDataService;
    this.beaconListFragment = beaconListFragment;
  }

  @Override
  public void onRssiChanged(Beacon beacon, int rssi) {
    String mac = beacon.getDevice().getAddress();
    getBeaconDataService().addRssi(beacon, mac, rssi);
    getBeaconListFragment().updateBeaconList(mac, rssi);
  }

  @Override
  public void onRangeChanged(Beacon beacon, Range range) {
  }

  @Override
  public void onBeaconExit(Beacon beacon) {
  }

  @Override
  public void onBeaconEnter(Beacon beacon) {
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private BeaconListFragment getBeaconListFragment() {
    return this.beaconListFragment;
  }

}
