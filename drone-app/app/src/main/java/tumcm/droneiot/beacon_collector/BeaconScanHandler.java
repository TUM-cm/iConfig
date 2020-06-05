package tumcm.droneiot.beacon_collector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BeaconScanHandler {

  private final Activity activity;

  private ServiceConnection serviceConnection;
  private BeaconScanService beaconScanService;

  public BeaconScanHandler(Activity activity) {
    this.activity = activity;
  }

  public void doBindService() {
    this.serviceConnection = this.getServiceConnection();
        getActivity().bindService(new Intent(getActivity(),
                BeaconScanService.class), getService(), Context.BIND_AUTO_CREATE);
  }

  public void doStartService() {
    this.serviceConnection = this.getServiceConnection();
      Thread thread = new Thread() {
          @Override
          public void run() {
              getActivity().startService(new Intent(getActivity(),
                      BeaconScanService.class));
          }
      };
      thread.start();
  }


  public void doUnbindService() {
    getActivity().unbindService(getService());
  }

  private ServiceConnection getServiceConnection() {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
        BeaconScanService.BeaconServiceBinder binder = (BeaconScanService.BeaconServiceBinder) iBinder;
        beaconScanService = binder.getService();
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

  private Activity getActivity() {
    return this.activity;
  }

}
