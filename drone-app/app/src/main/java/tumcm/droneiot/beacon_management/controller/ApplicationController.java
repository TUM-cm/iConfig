package tumcm.droneiot.beacon_management.controller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.bluvision.beeks.sdk.constants.BeaconType;
import com.bluvision.beeks.sdk.interfaces.BeaconListener;
import com.bluvision.beeks.sdk.util.BeaconManager;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import tumcm.droneiot.MainActivity;
import tumcm.droneiot.beacon_management.backend.BeaconRestfulService;
import tumcm.droneiot.beacon_management.configurator.BeaconChangeListener;
import tumcm.droneiot.beacon_management.data.BeaconObject;
import tumcm.droneiot.beacon_management.data.BeaconUnregistered;
import tumcm.droneiot.beacon_management.gui.BeaconListFragment;
import tumcm.droneiot.beacon_management.gui.BeaconRegisterFragment;
import tumcm.droneiot.beacon_management.services.BeaconDataService;
import tumcm.droneiot.beacon_management.services.BeaconRegisterService;
import tumcm.droneiot.beacon_management.services.BeaconScanHandler;
import tumcm.droneiot.beacon_management.services.BeaconSynchronizationService;
import tumcm.droneiot.beacon_management.services.BeaconUpdateService;
import tumcm.droneiot.beacon_management.services.ExecutorService;
import tumcm.droneiot.beacon_management.services.ServiceCallback;
import tumcm.droneiot.beacon_management.utils.general.AppConfig;
import tumcm.droneiot.beacon_management.utils.general.Config;
import tumcm.droneiot.beacon_management.utils.beacon.BeaconUtils;
import tumcm.droneiot.beacon_management.utils.general.GUIUtils;

public abstract class ApplicationController implements BeaconListener {

  public static final int PERMISSION_REQUEST_BLUETOOTH = 1;
  public static boolean START_LOCATION_SENSING = false;

  private final BeaconListFragment beaconListFragment;
  private final BeaconDataService beaconDataService;
  private final BeaconChangeListener beaconChangeListener;
  private final BeaconRestfulService beaconRestfulService;
  private final ExecutorService executorService;
  private final BeaconUpdateService beaconUpdateService;
  private final BeaconSynchronizationService beaconSynchronizationService;
  private final BeaconUtils beaconUtils;
  private final AppConfig appConfig;
  private final BeaconRegisterService beaconRegisterService;
  private final BeaconRegisterFragment beaconRegisterFragment;
  private final ServiceCallback serviceCallback;
  private final ProgressDialog progressDialog;
  private final BeaconScanHandler beaconScanHandler;
  private BeaconManager beaconManager;

  private static boolean runBeaconConfig = false;
  private static final Deque<BeaconObject> configQueue = new ConcurrentLinkedDeque<>();

  public ApplicationController() {
    this.beaconListFragment = new BeaconListFragment();
    this.beaconDataService = new BeaconDataService();
    this.beaconRegisterFragment = new BeaconRegisterFragment();
    this.beaconRestfulService = new BeaconRestfulService();
    this.beaconScanHandler = new BeaconScanHandler(getBeaconDataService(),
            getBeaconListFragment());
    this.progressDialog = new ProgressDialog(MainActivity.getInstance());
    this.appConfig = new AppConfig(Config.getInstance(MainActivity.getConfigResource()));
    this.executorService = new ExecutorService(getAppConfig());
    this.beaconUtils = new BeaconUtils(getBeaconDataService());
    this.beaconChangeListener = new BeaconChangeListener(getBeaconDataService(),
            getBeaconListFragment());
    this.beaconSynchronizationService = new BeaconSynchronizationService(getExecutorService(),
            getBeaconDataService(), getBeaconRestfulService(), getAppConfig());
    this.beaconRegisterService = new BeaconRegisterService(getBeaconListFragment(),
            getBeaconRegisterFragment(), getBeaconDataService(), getProgressDialog(),
            getConfigQueue());
    this.serviceCallback = new ServiceCallback(getBeaconDataService(), getBeaconRegisterService(),
            getProgressDialog(), getBeaconSynchronizationService());
    this.beaconUpdateService = new BeaconUpdateService(getExecutorService(),
            getBeaconDataService(), getServiceCallback(), getAppConfig(), getConfigQueue());
  }

  public abstract void onDestroy();

  public abstract void switchToBeaconRegister(BeaconUnregistered beaconUnregistered);

  public abstract void setTargetBeaconMac(String macAddress);

  public void onCreate() {
    checkBluetoothEnabled();
    this.beaconManager = new BeaconManager(getBeaconListFragment().getContext());
    getBeaconManager().addRuleToIncludeScanByType(BeaconType.S_BEACON);
    getBeaconManager().addBeaconListener(this);
  }

  public static void nextBeacon() {
    if (getConfigQueue().size() > 0 && !isRunBeaconConfig()) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          getConfigQueue().poll().connect();
        }
      }).start();
    }
  }

  @Override
  public void bluetoothIsNotEnabled() {
    Toast.makeText(MainActivity.getInstance(),
            "Please activate your Bluetooth connection", Toast.LENGTH_LONG).show();
  }

  private void checkBluetoothEnabled() {
    BluetoothManager manager = (BluetoothManager) getBeaconListFragment()
            .getActivity().getApplicationContext()
            .getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter btAdapter = manager.getAdapter();
    if (btAdapter == null) {
      GUIUtils.showFinishingAlertDialog(getBeaconListFragment().getActivity(),
              "Bluetooth Error",
              "Bluetooth not detected on device");
    } else if (!btAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      getBeaconListFragment().startActivityForResult(enableBtIntent,
              PERMISSION_REQUEST_BLUETOOTH);
    }
  }

  public BeaconManager getBeaconManager() {
    return this.beaconManager;
  }

  public BeaconListFragment getBeaconListFragment() {
    return this.beaconListFragment;
  }

  public BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  protected BeaconChangeListener getBeaconChangeListener() {
    return this.beaconChangeListener;
  }

  protected BeaconRestfulService getBeaconRestfulService() {
    return this.beaconRestfulService;
  }

  protected BeaconUtils getBeaconUtils() {
    return this.beaconUtils;
  }

  protected AppConfig getAppConfig() {
    return this.appConfig;
  }

  protected BeaconUpdateService getBeaconUpdateService() {
    return this.beaconUpdateService;
  }

  protected ExecutorService getExecutorService() {
    return this.executorService;
  }

  protected BeaconSynchronizationService getBeaconSynchronizationService() {
    return this.beaconSynchronizationService;
  }

  protected BeaconRegisterFragment getBeaconRegisterFragment() {
    return this.beaconRegisterFragment;
  }

  protected BeaconRegisterService getBeaconRegisterService() {
    return this.beaconRegisterService;
  }

  protected ProgressDialog getProgressDialog() {
    return this.progressDialog;
  }

  protected ServiceCallback getServiceCallback() {
    return this.serviceCallback;
  }

  protected BeaconScanHandler getBeaconScanHandler() {
    return this.beaconScanHandler;
  }

  public static Deque<BeaconObject> getConfigQueue() {
    return configQueue;
  }

  public static void setRunBeaconConfig(boolean status) {
    runBeaconConfig = status;
  }

  public static boolean isRunBeaconConfig() {
    return runBeaconConfig;
  }

}
