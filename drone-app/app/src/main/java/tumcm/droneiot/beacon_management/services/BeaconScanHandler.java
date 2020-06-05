package tumcm.droneiot.beacon_management.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import tumcm.droneiot.MainActivity;
import tumcm.droneiot.beacon_management.gui.BeaconListFragment;

public class BeaconScanHandler {

  private final BeaconDataService beaconDataService;
  private final BeaconListFragment beaconListFragment;
  private ServiceConnection serviceConnection;
  private BeaconScanService beaconScanService;

  public BeaconScanHandler(BeaconDataService beaconDataService,
                           BeaconListFragment beaconListFragment) {
    this.beaconDataService = beaconDataService;
    this.beaconListFragment = beaconListFragment;
  }

  public void doStartService() {
    MainActivity.getInstance().startService(new Intent(MainActivity.getInstance(),
            BeaconScanService.class));
  }

  public void doBindService() {
    this.serviceConnection = this.getServiceConnection();
    MainActivity.getInstance().bindService(new Intent(MainActivity.getInstance(),
            BeaconScanService.class), getService(), Context.BIND_AUTO_CREATE);
  }

  public void doUnbindService() {
    MainActivity.getInstance().unbindService(getService());
  }

  private ServiceConnection getServiceConnection() {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
        BeaconScanService.BeaconServiceBinder binder = (BeaconScanService.BeaconServiceBinder) iBinder;
        beaconScanService = binder.getService();
        getScanService().init(getBeaconDataService(), getBeaconListFragment());
        getScanService().startScan();
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName) {
        beaconScanService = null;
      }
    };
  }

  public BeaconScanService getScanService() {
    return this.beaconScanService;
  }

  private ServiceConnection getService() {
    return this.serviceConnection;
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private BeaconListFragment getBeaconListFragment() {
    return this.beaconListFragment;
  }

}
