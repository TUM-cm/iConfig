package tumcm.droneiot.beacon_collector;

import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tumcm.droneiot.SurroundingBeacons;

import static java.lang.Math.abs;

public class BeaconScanService extends Service {

    private Set<String> macAddresses = new HashSet<>();
    SurroundingBeacons beaconSurrounding = SurroundingBeacons.getInstance();
    private IBinder binder;
    private ScanSettings aggressiveScanSettings;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;

    @Override
    public IBinder onBind(Intent intent) {
        this.binder = new BeaconServiceBinder();
        return this.binder;
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
        this.scanCallback = buildCallback();
    }

    public void startScan() {
        getBluetoothLeScanner().startScan(null,
                getAggressiveScanSettings(), getScanCallback());

    }

    public void stopScan() {
        getBluetoothLeScanner().stopScan(getScanCallback());
    }

    private ScanCallback buildCallback() {
        return new ScanCallback() {
            @Override
            public void onScanResult(final int callbackType, final ScanResult result) {
                Log.wtf("Scan Measurement", result.getDevice().getAddress() + " rssi: " + result.getRssi());
                String mac = result.getDevice().getAddress();

                int rssi = abs(result.getRssi());

                if (macAddresses.add(mac)) {
                    beaconSurrounding.put(mac,rssi);
                    Log.wtf("Beacon", "added");
                } else {
                    beaconSurrounding.put(mac,rssi);
                    Log.wtf("Beacon", "already in");
                }

            }

            @Override
            public void onBatchScanResults(final List<ScanResult> results) {
            }

            @Override
            public void onScanFailed(int errorCode) {
            }
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

    public class BeaconServiceBinder extends Binder {
        public BeaconScanService getService() {
            return BeaconScanService.this;
        }
    }

}
