package de.tum.in.cm.android.eddystonemanager.gui;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.cm.android.eddystonemanager.R;
import de.tum.in.cm.android.eddystonemanager.controller.ApplicationController;
import de.tum.in.cm.android.eddystonemanager.controller.MainController;
import de.tum.in.cm.android.eddystonemanager.data.BeaconUnregistered;
import de.tum.in.cm.android.eddystonemanager.services.BeaconDataService;
import de.tum.in.cm.android.eddystonemanager.services.BeaconRegisterService;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.BeaconUtils;

public class BeaconListFragment extends Fragment {

  private static final String TAG = BeaconListFragment.class.getSimpleName();
  private ApplicationController applicationController;
  private BeaconArrayAdapter beaconArrayAdapter;
  private Button startScanButton;
  private Button stopScanButton;
  private TextView statusText;
  private EditText beaconFilter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.beaconArrayAdapter = new BeaconArrayAdapter(getActivity(),
            R.layout.beacon_list_item, new ArrayList<BeaconUnregistered>());
    this.applicationController = MainActivity.ACTIVE_CONTROLLER;
    getApplicationController().getBeaconDataService()
            .setBeaconArrayAdapter(getBeaconArrayAdapter());
    getApplicationController().onCreate();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    getApplicationController().onDestroy();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.beacon_list, container, false);
    ListView listView = (ListView) view.findViewById(R.id.listView);
    listView.setAdapter(getBeaconArrayAdapter());
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BeaconUnregistered beaconUnregistered = (BeaconUnregistered) parent
                .getItemAtPosition(position);
        getApplicationController().switchToBeaconRegister(beaconUnregistered);
      }
    });
    this.startScanButton = (Button) view.findViewById(R.id.startButton);
    this.stopScanButton = (Button) view.findViewById(R.id.stopButton);
    this.statusText = (TextView) view.findViewById(R.id.statusTextView);
    this.beaconFilter = (EditText) view.findViewById(R.id.beaconFilter);
    // Activate scan by default
    if (!MainController.SETTING.isTest()) {
      getStartScanButton().post(new Runnable() {
        @Override
        public void run() {
          getStartScanButton().performClick();
        }
      });
    }
    getStartScanButton().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        startButtonAction();
      }
    });
    getStopScanButton().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        stopButtonAction();
      }
    });
    getBeaconFilter().addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      public void afterTextChanged(Editable s) {
        String value = s.toString();
        if (BeaconRegisterService.isValidMacAddress(value)) {
          getApplicationController().setTargetBeaconMac(value);
        }
      }
    });
    return view;
  }

  private void startButtonAction() {
    if (getApplicationController().getBeaconManager() != null) {
      getApplicationController().getBeaconManager().startScan();
      getStartScanButton().setEnabled(false);
      getStopScanButton().setEnabled(true);
      getStatusTextView().setText("Scanning...");
    } else {
      Log.e(TAG, "Tried to start scanning with no connected Beacon Service.");
    }
  }

  private void stopButtonAction() {
    if (getApplicationController().getBeaconManager() != null) {
      getApplicationController().getBeaconManager().stopScan();
      getStartScanButton().setEnabled(true);
      getStopScanButton().setEnabled(false);
      getStatusTextView().setText("Idle");
    } else {
      Log.e(TAG, "Tried to stop scanning without connected Beacon Service.");
    }
  }

  public void updateBeaconList() {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        getBeaconArrayAdapter().clear();
        List<BeaconUnregistered> unregisteredBeacons = getApplicationController()
                .getBeaconDataService().getUnregisteredBeaconsAsList();
        for(BeaconUnregistered unregisteredBeacon : unregisteredBeacons) {
          if (unregisteredBeacon.getSBeaconId() != null) {
            getBeaconArrayAdapter().add(unregisteredBeacon);
          }
        }
        getBeaconArrayAdapter().sort(BeaconUtils.RSSI_COMPARATOR);
        getBeaconArrayAdapter().notifyDataSetChanged();
      }
    });
  }
  
  public void updateBeaconList(String mac, int rssi) {
    BeaconDataService beaconDataService = getApplicationController().getBeaconDataService();
    if (!beaconDataService.getRegisteredBeaconsMap().containsKey(mac)) {
      BeaconUnregistered beaconUnregistered = beaconDataService.getUnregisteredBeaconsMap().get(mac);
      if (beaconUnregistered != null) {
        beaconUnregistered.getRssiFilter().addMeasurement(rssi);
        updateBeaconList();
      }
    }
  }

  private TextView getStatusTextView() {
    return this.statusText;
  }

  private Button getStopScanButton() {
    return this.stopScanButton;
  }

  private Button getStartScanButton() {
    return this.startScanButton;
  }

  private EditText getBeaconFilter() {
    return this.beaconFilter;
  }

  private BeaconArrayAdapter getBeaconArrayAdapter() {
    return this.beaconArrayAdapter;
  }

  private ApplicationController getApplicationController() {
    return this.applicationController;
  }

}
