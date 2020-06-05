package tumcm.droneiot.beacon_management.services;

import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tumcm.droneiot.beacon_management.data.BeaconUnregistered;
import tumcm.droneiot.beacon_management.gui.BeaconListFragment;
import tumcm.droneiot.beacon_management.utils.beacon.EddystoneTlmFrame;
import tumcm.droneiot.beacon_management.utils.beacon.SBeaconFrame;

public class BeaconScanService extends Service {

  private static final String TAG = BeaconScanService.class.getSimpleName();
  private static final String EDDYSTONE_SERVICE_UUID_STR = "0000feaa-0000-1000-8000-00805f9b34fb";
  private static final ParcelUuid EDDYSTONE_SERVICE_UUID = ParcelUuid.fromString(EDDYSTONE_SERVICE_UUID_STR);
  private static final String S_BEACON_SERVICE_UUID_STR =  "00001804-0000-1000-8000-00805f9b34fb";
  private static final ParcelUuid S_BEACON_SERVICE_UUID = ParcelUuid.fromString(S_BEACON_SERVICE_UUID_STR);
  private static final byte[] TLM_FILTER = {
          0x20, //Frame type
          0x00, //Protocol version = 0
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00
  };
  private static final byte[] TLM_FILTER_MASK = {
          (byte)0xFF,
          (byte)0xFF,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00
  };

  private IBinder binder;
  private BeaconDataService beaconDataService;
  private BeaconListFragment beaconListFragment;
  private ScanSettings aggressiveScanSettings;
  private BluetoothLeScanner bluetoothLeScanner;
  private ScanCallback scanCallback;
  private List<ScanFilter> scanFilters;
  private ScanFilter telemetryFilter;
  private ScanFilter beaconIdFilter;

  @Override
  public IBinder onBind(Intent intent) {
    this.binder = new BeaconServiceBinder();
    return this.binder;
  }

  public class BeaconServiceBinder extends Binder {
    public BeaconScanService getService() {
      return BeaconScanService.this;
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
    this.bluetoothLeScanner = manager.getAdapter().getBluetoothLeScanner();
    this.aggressiveScanSettings = new ScanSettings.Builder()
            .setReportDelay(0)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();
    this.telemetryFilter = new ScanFilter.Builder()
            .setServiceUuid(EDDYSTONE_SERVICE_UUID)
            .setServiceData(EDDYSTONE_SERVICE_UUID, TLM_FILTER, TLM_FILTER_MASK)
            .build();
    this.scanFilters = new ArrayList<>();
    getScanFilters().add(getTelemetryFilter());
    this.beaconIdFilter = new ScanFilter.Builder()
            .setServiceUuid(S_BEACON_SERVICE_UUID)
            .build();
    getScanFilters().add(getBeaconIdFilter());
    this.scanCallback = buildCallback();
  }

  public void init(BeaconDataService beaconDataService,
                   BeaconListFragment beaconListFragment) {
    this.beaconDataService = beaconDataService;
    this.beaconListFragment = beaconListFragment;
  }

  public void startScan() {
    getBluetoothLeScanner().startScan(getScanFilters(),
            getAggressiveScanSettings(), getScanCallback());
  }

  public void stopScan() {
    getBluetoothLeScanner().stopScan(getScanCallback());
  }

  private ScanCallback buildCallback() {
    return new ScanCallback() {
      @Override
      public void onScanResult(final int callbackType, final ScanResult result) {
        String mac = result.getDevice().getAddress();

        result.getRssi();

        ScanRecord scanRecord = result.getScanRecord();
        byte[] serviceData = scanRecord.getServiceData(EDDYSTONE_SERVICE_UUID);
        if(serviceData != null) {
          if (EddystoneTlmFrame.isTlmFrame(serviceData)) {
            EddystoneTlmFrame tlmFrame = EddystoneTlmFrame.parse(serviceData);
            if (tlmFrame != null) {
              getBeaconDataService().getTlmFrames().put(mac, tlmFrame);
            } else {
              Log.d(TAG, "Failed to parse a TLM frame");
            }
          }
        } else if (getBeaconDataService().getUnregisteredBeaconsMap().containsKey(mac)) { // beacon id
          BeaconUnregistered beaconUnregistered = getBeaconDataService()
                  .getUnregisteredBeaconsMap().get(mac);
          if (beaconUnregistered.getSBeaconId() == null) {
            SBeaconFrame sBeaconFrame = SBeaconFrame.parseData(scanRecord.getBytes());
            boolean setBeaconId = beaconUnregistered.setSBeaconId(sBeaconFrame.getId());
            if (setBeaconId) {
              getBeaconListFragment().updateBeaconList();
            }
          }
        }
      }

      @Override
      public void onBatchScanResults(final List<ScanResult> results) {}

      @Override
      public void onScanFailed(int errorCode) {}
    };
  }

  private ScanSettings getAggressiveScanSettings() {
    return this.aggressiveScanSettings;
  }

  private BluetoothLeScanner getBluetoothLeScanner() {
    return this.bluetoothLeScanner;
  }

  private ScanCallback getScanCallback() {
    return this.scanCallback;
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private List<ScanFilter> getScanFilters() {
    return this.scanFilters;
  }

  private ScanFilter getTelemetryFilter() {
    return this.telemetryFilter;
  }

  private ScanFilter getBeaconIdFilter() {
    return this.beaconIdFilter;
  }

  private BeaconListFragment getBeaconListFragment() {
    return this.beaconListFragment;
  }

}
