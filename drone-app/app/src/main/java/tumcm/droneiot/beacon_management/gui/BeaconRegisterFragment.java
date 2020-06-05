package tumcm.droneiot.beacon_management.gui;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import tumcm.droneiot.R;
import tumcm.droneiot.beacon_management.data.BeaconObject;
import tumcm.droneiot.beacon_management.services.BeaconRegisterService;
import tumcm.droneiot.beacon_management.utils.beacon.BeaconUtils;
import tumcm.droneiot.beacon_management.utils.general.GUIUtils;

public class BeaconRegisterFragment extends Fragment {

  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int RESULT_OK = -1;

  private BeaconRegisterService beaconRegisterService;
  private boolean onStartCalled;
  private Button identifyBeaconButton;
  private Button beaconImageButton;
  private TextView macAddress;
  private TextView sBeaconId;
  private EditText locationDescription;
  private EditText comments;
  private EditText nearbyRoom;
  private Button registerBeaconButton;
  private boolean beaconIdentified;

  public void init(BeaconRegisterService beaconRegisterService) {
    this.beaconRegisterService = beaconRegisterService;
    setBeaconIdentified(false);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.beacon_register, container, false);
    this.macAddress = view.findViewById(R.id.macAddress);
    this.sBeaconId = view.findViewById(R.id.sBeaconId);
    this.identifyBeaconButton = view.findViewById(R.id.identifyBeacon);
    this.beaconImageButton = view.findViewById(R.id.beaconImage);
    this.nearbyRoom = view.findViewById(R.id.EditRoom);
    this.locationDescription = view.findViewById(R.id.EditLocation);
    this.comments = view.findViewById(R.id.EditComment);
    this.registerBeaconButton = view.findViewById(R.id.registerBeacon);
    setOnStartCalled(false);
    getIdentifyBeaconButton().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getBeaconRegisterService().identifyAction();
      }
    });
    getBeaconImageButton().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dispatchTakePictureIntent();
      }
    });
    getRegisterBeaconButton().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        registerAction();
      }
    });
    if (isBeaconIdentified()) {
      enableRegisterAction();
    }
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    if (!isOnStartCalled()) {
      getMacAddressField().setText(getBeaconRegisterService().getCurrentMac());
      getSBeaconIdField().setText(getBeaconRegisterService().getCurrentSBeaconId());
      getLocationDescriptionField().getText().clear();
      getCommentsField().getText().clear();
      getNearbyRoomField().getText().clear();
      setOnStartCalled(true);
    }
  }

  public void dispatchTakePictureIntent() {
    // Todo include picture from drone camera
      File photoFile = getBeaconRegisterService().createImageFile();
      if (photoFile != null) {
        Uri photoURI = FileProvider.getUriForFile(getContext(),
                "tumcm.droneiot.beacon_management.fileprovider",
                photoFile);
        getBeaconRegisterService().getBeaconDataService().getActiveBeacon()
                .setPlaceImage(photoFile);
      }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Todo include picture from drone camera
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      if (data == null || (data != null && data.getExtras() == null)) {
        File image = getBeaconRegisterService().getBeaconDataService()
                .getActiveBeacon().getPlaceImage();
      }
    }
  }

  private void registerAction() {
    boolean registerBeacon = true;
    String title = "Register Beacon";
    StringBuilder message = new StringBuilder();
    // Ensure required data available
    BeaconObject beacon = getBeaconRegisterService().getBeaconDataService().getActiveBeacon();
    if (beacon.getPlaceImage() == null) {
      message.append("Please take a picture of the beacon placement.");
      message.append(BeaconUtils.LINE_SEPARATOR);
      registerBeacon = false;
    }
    if (! BeaconRegisterService.isValidLocationDescription(
            getLocationDescriptionField().getText().toString())) {
      message.append("Please enter a location description for easier localization.");
      message.append(BeaconUtils.LINE_SEPARATOR);
      registerBeacon = false;
    }
    if (!BeaconRegisterService.isValidRoom(getNearbyRoom())) {
      message.append("Please enter a valid room number, format: floor.building.room");
      message.append(BeaconUtils.LINE_SEPARATOR);
      registerBeacon = false;
    }
    if (registerBeacon) {
      getBeaconRegisterService().registerAction();
    } else {
      GUIUtils.showOKAlertDialog(getActivity(), title, message.toString());
    }
  }

  public void enableRegisterAction() {
    setBeaconIdentified(true);
    getBeaconImageButton().setEnabled(true);
    getNearbyRoomField().setEnabled(true);
    getLocationDescriptionField().setEnabled(true);
    getCommentsField().setEnabled(true);
    getRegisterBeaconButton().setEnabled(true);
  }

  public void setBeaconIdentified(boolean beaconIdentified) {
    this.beaconIdentified = beaconIdentified;
  }

  private Button getIdentifyBeaconButton() {
    return this.identifyBeaconButton;
  }

  public EditText getLocationDescriptionField() {
    return this.locationDescription;
  }

  public String getLocationDescription() {
    return getLocationDescriptionField().getText().toString().trim();
  }

  public EditText getCommentsField() {
    return this.comments;
  }

  public String getComments() {
    return getCommentsField().getText().toString().trim();
  }

  private Button getRegisterBeaconButton() {
    return this.registerBeaconButton;
  }

  private Button getBeaconImageButton() {
    return this.beaconImageButton;
  }

  public EditText getNearbyRoomField() {
    return this.nearbyRoom;
  }

  private TextView getMacAddressField() {
    return this.macAddress;
  }

  public String getNearbyRoom() {
    return getNearbyRoomField().getText().toString().replaceAll("\\s+", "");
  }

  public TextView getSBeaconIdField() {
    return this.sBeaconId;
  }

  private BeaconRegisterService getBeaconRegisterService() {
    return this.beaconRegisterService;
  }

  private void setOnStartCalled(boolean onStartCalled) {
    this.onStartCalled = onStartCalled;
  }

  private boolean isOnStartCalled() {
    return this.onStartCalled;
  }

  private boolean isBeaconIdentified() {
    return this.beaconIdentified;
  }

}
