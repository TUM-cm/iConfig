package de.tum.in.cm.android.eddystonemanager.gui;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import de.tum.in.cm.android.eddystonemanager.R;
import de.tum.in.cm.android.eddystonemanager.controller.ApplicationController;
import de.tum.in.cm.android.eddystonemanager.controller.SpeechController;
import de.tum.in.cm.android.eddystonemanager.data.BeaconObject;
import de.tum.in.cm.android.eddystonemanager.services.BeaconRegisterService;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.BeaconUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.GUIUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.ImageUtils;

import static de.tum.in.cm.android.eddystonemanager.controller.MainController.SETTING;

public class BeaconRegisterFragment extends Fragment {

  private static final String TAG = BeaconRegisterFragment.class.getSimpleName();
  private static final int REQUEST_TAKE_PHOTO = 1;
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int RESULT_OK = -1;

  private BeaconRegisterService beaconRegisterService;
  private ApplicationController applicationController;
  private ImageUtils imageUtils;
  private SpeechController speechController;
  private boolean onStartCalled;
  private Button verifyBeaconConfigButton;
  private Button identifyBeaconButton;
  private Button beaconImageButton;
  private TextView macAddress;
  private TextView sBeaconId;
  private EditText locationDescription;
  private EditText comments;
  private EditText nearbyRoom;
  private Button registerBeaconButton;
  private boolean beaconIdentified;

  public void init(BeaconRegisterService beaconRegisterService,
                   SpeechController speechController) {
    this.beaconRegisterService = beaconRegisterService;
    this.speechController = speechController;
    this.imageUtils = new ImageUtils();
    setBeaconIdentified(false);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.applicationController = MainActivity.ACTIVE_CONTROLLER;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.beacon_register, container, false);
    this.macAddress = (TextView) view.findViewById(R.id.macAddress);
    this.sBeaconId = (TextView) view.findViewById(R.id.sBeaconId);
    this.verifyBeaconConfigButton = (Button) view.findViewById(R.id.verifyBeaconConfig);
    this.identifyBeaconButton = (Button) view.findViewById(R.id.identifyBeacon);
    this.beaconImageButton = (Button) view.findViewById(R.id.beaconImage);
    this.nearbyRoom = (EditText) view.findViewById(R.id.EditRoom);
    this.locationDescription = (EditText) view.findViewById(R.id.EditLocation);
    this.comments = (EditText) view.findViewById(R.id.EditComment);
    this.registerBeaconButton = (Button) view.findViewById(R.id.registerBeacon);
    setOnStartCalled(false);
    if (getSpeechController() != null) {
      getSpeechController().getSpeechTextGoogle().setMainHandler(new Handler());
    }
    getVerifyBeaconConfigButton().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getBeaconRegisterService().verifyConfigAction();
      }
    });
    if (SETTING.isDisableConfigVerify()) {
      getVerifyBeaconConfigButton().setVisibility(View.GONE);
    }
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
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    boolean pictureIntentAvailable = takePictureIntent
            .resolveActivity(getActivity().getPackageManager()) != null;
    boolean speechControlActivated = (getSpeechController() != null &&
            getSpeechController().isBeaconSelected());
    if (pictureIntentAvailable && !speechControlActivated) {
      File photoFile = null;
      try {
        photoFile = getBeaconRegisterService().createImageFile();
      } catch (IOException e) {
        Log.d(TAG, "take picture", e);
      }
      if (photoFile != null) {
        Uri photoURI = FileProvider.getUriForFile(getContext(),
                "de.tum.in.cm.android.eddystonemanager.fileprovider",
                photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        getBeaconRegisterService().getBeaconDataService().getActiveBeacon()
                .setPlaceImage(photoFile);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
      }
    } else {
      getApplicationController().switchToCamera();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      if (data == null || (data != null && data.getExtras() == null)) {
        File image = getBeaconRegisterService().getBeaconDataService()
                .getActiveBeacon().getPlaceImage();
        getImageUtils().inPlaceAdjustImage(image);
        if (getSpeechController() != null && getSpeechController().isBeaconSelected()) {
          getBeaconRegisterService().getSpeechController().getNearestRoom();
        } else {
          Toast toast = Toast.makeText(getActivity(), "Beacon image saved", Toast.LENGTH_LONG);
          toast.show();
        }
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

  private Button getVerifyBeaconConfigButton() {
    return this.verifyBeaconConfigButton;
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

  private SpeechController getSpeechController() {
    return this.speechController;
  }

  private ApplicationController getApplicationController() {
    return this.applicationController;
  }

  private ImageUtils getImageUtils() {
    return this.imageUtils;
  }

  private boolean isBeaconIdentified() {
    return this.beaconIdentified;
  }

}
