package tumcm.droneiot;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tumcm.droneiot.beacon_collector.BeaconScanHandler;
import tumcm.droneiot.beacon_management.controller.ApplicationController;
import tumcm.droneiot.beacon_management.controller.ProductionController;
import tumcm.droneiot.beacon_management.utils.general.GUIUtils;

public class MainActivity extends AppCompatActivity {

    public static ApplicationController ACTIVE_CONTROLLER;
    private static Activity ACTIVITY;
    private static final int PERMISSION_MULTIPLE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_BLUETOOTH = 1;

    private ObstacleDistance obstacleDistanceController;
    private FlightControllerClass flightControllerClass;
    public TextView sensorSection1;
    public TextView sensorSection2;
    public TextView indoorPositionSection;
    public Button startButton;

    private BeaconScanHandler beaconScanHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Swapping fragments
        setContentView(R.layout.activity_main);

        ACTIVITY = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sensorSection1 = findViewById(R.id.textView);
        sensorSection2 = findViewById(R.id.textView2);
        indoorPositionSection = findViewById(R.id.textView4);
        startButton = findViewById(R.id.start_button);

        obstacleDistanceController = new ObstacleDistance();
        obstacleDistanceController.printObstacleDistance(sensorSection1,sensorSection2);

        flightControllerClass = new FlightControllerClass();
        flightControllerClass.init(startButton, this);

        IndoorLocation indoorLocation = IndoorLocation.getInstance();
        indoorLocation.initializeUI(indoorPositionSection);



        /*boolean proceedFurtherProcessing = true;
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
        }*/

        boolean proceed = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> requiredPermissions = new ArrayList<>();
            requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            //requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            String[] permissionsToRequest = getPermissionsToRequest(requiredPermissions);
            if (permissionsToRequest.length > 0) {
                proceed = false;
                requestPermissions(permissionsToRequest, PERMISSION_MULTIPLE_REQUEST);
            }
        }
        if (proceed) {
            startApplication();
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

    private void checkBluetoothEnabled(Activity activity) {
        BluetoothManager manager = (BluetoothManager) activity
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = manager.getAdapter();
        if (btAdapter == null) {
            GUIUtils.showFinishingAlertDialog(activity,
                    "Bluetooth Error",
                    "Bluetooth not detected on device");
        } else if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, PERMISSION_REQUEST_BLUETOOTH);
        }
    }

    private void startApplication() {
        checkBluetoothEnabled(this);
        this.beaconScanHandler = new BeaconScanHandler(this);
        getBeaconScanHandler().doBindService();
        //getBeaconScanHandler().doStartService();

    }

    // EddystoneManager
    /*private void startApplication() {
        AppStorage.createDataFolder();
        ACTIVE_CONTROLLER = new ProductionController();
        setContentView(R.layout.activity_main);
        swapFragment(getFragmentManager(), ACTIVE_CONTROLLER.getBeaconListFragment());
    }*/

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

    private BeaconScanHandler getBeaconScanHandler() {
        return this.beaconScanHandler;
    }

    public static Activity getInstance() {
        return ACTIVITY;
    }

    public static int getConfigResource() {
        return R.raw.config;
    }

    public static int getCertificate() {
        return R.raw.server;
    }

    public static int getDefaultBeaconConfig() {
        return R.raw.default_beacon_config;
    }

}
