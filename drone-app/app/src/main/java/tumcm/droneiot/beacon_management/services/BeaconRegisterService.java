package tumcm.droneiot.beacon_management.services;

import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.bluvision.beeks.sdk.domainobjects.Beacon;

import java.io.File;
import java.util.Deque;
import java.util.UUID;
import java.util.regex.Pattern;

import tumcm.droneiot.MainActivity;
import tumcm.droneiot.beacon_management.configurator.Action;
import tumcm.droneiot.beacon_management.data.BeaconConfig;
import tumcm.droneiot.beacon_management.data.BeaconObject;
import tumcm.droneiot.beacon_management.data.EddystoneConfig;
import tumcm.droneiot.beacon_management.data.EddystonePackets;
import tumcm.droneiot.beacon_management.data.IBeaconConfig;
import tumcm.droneiot.beacon_management.data.Position;
import tumcm.droneiot.beacon_management.data.SBeaconConfig;
import tumcm.droneiot.beacon_management.data.UidConfig;
import tumcm.droneiot.beacon_management.gui.BeaconListFragment;
import tumcm.droneiot.beacon_management.gui.BeaconRegisterFragment;
import tumcm.droneiot.beacon_management.utils.beacon.BeaconUtils;
import tumcm.droneiot.beacon_management.utils.beacon.EddystoneUuid;
import tumcm.droneiot.beacon_management.utils.general.GUIUtils;

import static tumcm.droneiot.beacon_management.controller.ApplicationController.nextBeacon;

public class BeaconRegisterService {

  private static final String TAG = BeaconRegisterService.class.getSimpleName();
  private static final String IMAGE_SUFFIX = ".jpg";
  private static final String BEACON_IMAGE_FILENAME = "beacon_image";
  private static final int MAX_TIME_IDENTIFY_REGISTER = 25000;
  private static final String PATTERN_VALID_S_BEACON_ID = "[A-Za-z0-9]{16}";
  private static final Pattern sBeaconIdPattern = Pattern.compile(PATTERN_VALID_S_BEACON_ID);
  private static final String OFFICIAL_PATTERN_VALID_MAC_ADDRESS = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
  private static final Pattern officialMacAddressPattern = Pattern.compile(OFFICIAL_PATTERN_VALID_MAC_ADDRESS);
  private static final String PATTERN_VALID_MAC_ADDRESS = "[A-Za-z0-9]{12}";
  private static final Pattern macAddressPattern = Pattern.compile(PATTERN_VALID_MAC_ADDRESS);

  private final BeaconListFragment beaconListFragment;
  private final BeaconRegisterFragment beaconRegisterfragment;
  private final BeaconDataService beaconDataService;
  private final ProgressDialog progressDialog;
  private final Deque<BeaconObject> configQueue;

  public BeaconRegisterService(BeaconListFragment beaconListFragment,
                               BeaconRegisterFragment beaconRegisterfragment,
                               BeaconDataService beaconDataService,
                               ProgressDialog progressDialog,
                               Deque<BeaconObject> configQueue) {
    this.beaconListFragment = beaconListFragment;
    this.beaconRegisterfragment = beaconRegisterfragment;
    this.beaconDataService = beaconDataService;
    this.progressDialog = progressDialog;
    this.configQueue = configQueue;
  }

  public static boolean isValidRoom(String room) {
    if (room.trim().length() == 0) {
      return false;
    }
    String[] parts = room.split("\\.");
    boolean checkInt = true;
    for (String part : parts) {
      if (checkInt) {
        checkInt = isInteger(part);
      }
    }
    if (parts.length != 3 || !checkInt) {
      return false;
    }
    return true;
  }

  public static boolean isValidLocationDescription(String text) {
    return text.trim().length() != 0;
  }

  private static boolean isInteger(String integerString) {
    try {
      Integer.parseInt(integerString);
      return true;
    } catch (NumberFormatException nfe) {
      return false;
    }
  }

  public static boolean isValidSBeaconId(String sBeaconId) {
    if (sBeaconId != null) {
      return getsBeaconIdPattern().matcher(sBeaconId).matches();
    } else {
      return false;
    }
  }

  public static boolean isValidMacAddress(String macAddress) {
    if (macAddress.contains(":")) {
      return getOfficialMacAddressPattern().matcher(macAddress).matches();
    } else {
      return getMacAddressPattern().matcher(macAddress).matches();
    }
  }

  public File createImageFile() {
    File storageDir = getBeaconRegisterFragment().getActivity()
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File image = new File(storageDir, BEACON_IMAGE_FILENAME + IMAGE_SUFFIX);
    return image;
  }

  private void startProgressDialog() {
    getProgressDialog().setProgressStyle(ProgressDialog.STYLE_SPINNER);
    getProgressDialog().setIndeterminate(true);
    getProgressDialog().show();
  }

  public void identifyAction() {
    getProgressDialog().setTitle("Identify Beacon");
    getProgressDialog().setMessage("Try to identify beacon with multiple passwords ...");
    startProgressDialog();
    final Thread identifyThread = new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "Identify Action");
        BeaconObject activeBeacon = getBeaconDataService().getActiveBeacon();
        activeBeacon.setAction(Action.Identify);
        getConfigQueue().addFirst(activeBeacon);
        nextBeacon();
      }
    });
    identifyThread.start();

    // In case of error, limit max wait time for user
    Runnable progressRunnable = new Runnable() {
      @Override
      public void run() {
        if (identifyThread.isAlive()) {
          identifyThread.interrupt();
        }
        if (getProgressDialog().isShowing()) {
          getProgressDialog().dismiss();
          GUIUtils.showIdentifyDialog(getBeaconRegisterFragment().getActivity(),
                  BeaconRegisterService.this);
        }
      }
    };
    Handler handler = new Handler();
    handler.postDelayed(progressRunnable, MAX_TIME_IDENTIFY_REGISTER);
  }

  public void registerAction() {
    getProgressDialog().setTitle("Register Beacon");
    getProgressDialog().setMessage("Try to register beacon ...");
    startProgressDialog();
    final Thread registerThread = new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "registerAction");

        BeaconObject activeBeacon = getBeaconDataService().getActiveBeacon();
        BeaconConfig beaconConfig = activeBeacon.getConfig();

        // Set initial config
        beaconConfig.setMac(activeBeacon.getMac());
        beaconConfig.getSBeacon().setId(activeBeacon.getSBeaconId());

        beaconConfig.setNearestRoom(getBeaconRegisterFragment().getNearbyRoom());
        beaconConfig.setLocationDescription(getBeaconRegisterFragment().getLocationDescription());
        beaconConfig.setComments(getBeaconRegisterFragment().getComments());

        // Eddystone
        EddystoneUuid eddystoneUuid = BeaconUtils.generateRandomUid();
        UidConfig uidConfig = new UidConfig();
        uidConfig.setInstance(eddystoneUuid.getInstanceIdStr());
        uidConfig.setNamespace(eddystoneUuid.getNameSpaceStr());
        beaconConfig.getEddystone().setUidConfig(uidConfig);

        // iBeacon
        UUID iBeaconUuid = UUID.randomUUID();
        beaconConfig.getIBeacon().setUuid(iBeaconUuid);

        Position position = new Position();
        position.setLocation(getBeaconDataService().getCurrentLocation());
        beaconConfig.setPosition(position);
        beaconConfig.setAltitude(getBeaconDataService().getCurrentAltitude());

        activeBeacon.setAction(Action.Register);
        getConfigQueue().addFirst(activeBeacon);
        nextBeacon();
      }
    });
    registerThread.start();

    // In case of error, limit max wait time for user
    Runnable progressRunnable = new Runnable() {
      @Override
      public void run() {
        if (registerThread.isAlive()) {
          registerThread.interrupt();
        }
        if (getProgressDialog().isShowing()) {
          getProgressDialog().dismiss();
          String title = "Register Beacon";
          String message = "The register action went wrong. Please try again.";
          GUIUtils.showOKAlertDialog(getBeaconRegisterFragment().getActivity(), title, message);
          switchToBeaconList(getBeaconDataService().getActiveBeacon().getConfigurable());
        }
      }
    };
    Handler handler = new Handler();
    handler.postDelayed(progressRunnable, MAX_TIME_IDENTIFY_REGISTER);
  }

  public void registerBrokenBeacon() {
    BeaconObject activeBeacon = getBeaconDataService().getActiveBeacon();
    BeaconConfig beaconConfig = activeBeacon.getConfig();

    beaconConfig.setMac(activeBeacon.getMac());
    beaconConfig.getSBeacon().setId(activeBeacon.getSBeaconId());
    beaconConfig.setNewPassword(null);

    EddystoneConfig eddystoneConfig = beaconConfig.getEddystone();
    eddystoneConfig.setUrl(null);
    eddystoneConfig.setUidConfig(null);
    EddystonePackets packets = eddystoneConfig.getPackets();
    packets.getTlm().getDayMode().setAdvertisementRate(-1);
    packets.getTlm().getDayMode().setTransmissionPower((byte) -1);
    packets.getTlm().getNightMode().setAdvertisementRate(-1);
    packets.getTlm().getNightMode().setTransmissionPower((byte) -1);

    packets.getUid().getDayMode().setAdvertisementRate(-1);
    packets.getUid().getDayMode().setTransmissionPower((byte) -1);
    packets.getUid().getNightMode().setAdvertisementRate(-1);
    packets.getUid().getNightMode().setTransmissionPower((byte) -1);
    packets.getUid().getConnectionRate().setConnectableRate((byte) -1);
    packets.getUid().getConnectionRate().setNonConnectableRate((byte) -1);

    packets.getUrl().getDayMode().setAdvertisementRate(-1);
    packets.getUrl().getDayMode().setTransmissionPower((byte) -1);
    packets.getUrl().getNightMode().setAdvertisementRate(-1);
    packets.getUrl().getNightMode().setTransmissionPower((byte) -1);
    packets.getUrl().getConnectionRate().setConnectableRate((byte) -1);
    packets.getUrl().getConnectionRate().setNonConnectableRate((byte) -1);

    IBeaconConfig iBeaconConfig = beaconConfig.getIBeacon();
    iBeaconConfig.setMajor(-1);
    iBeaconConfig.setMinor(-1);
    iBeaconConfig.setUuid(null);
    iBeaconConfig.getPacket().getDayMode().setAdvertisementRate(-1);
    iBeaconConfig.getPacket().getDayMode().setTransmissionPower((byte) -1);
    iBeaconConfig.getPacket().getNightMode().setAdvertisementRate(-1);
    iBeaconConfig.getPacket().getNightMode().setTransmissionPower((byte) -1);

    SBeaconConfig sBeaconConfig = beaconConfig.getSBeacon();
    sBeaconConfig.setId(null);
    sBeaconConfig.getPacket().getDayMode().setAdvertisementRate(-1);
    sBeaconConfig.getPacket().getDayMode().setTransmissionPower((byte) -1);
    sBeaconConfig.getPacket().getNightMode().setAdvertisementRate(-1);
    sBeaconConfig.getPacket().getNightMode().setTransmissionPower((byte) -1);

    getBeaconDataService().addBeaconsToSynchronize(activeBeacon);
    guiActionAfterRegisterBeacon(false);
  }

  public void switchToBeaconList(final Beacon newBeacon) {
    if (newBeacon != null) {
      getBeaconDataService().addRegisteredBeacon(newBeacon);
    }
    getBeaconDataService().resetActiveBeacon();
    getBeaconListFragment().updateBeaconList();
    MainActivity.swapFragment(getBeaconListFragment().getFragmentManager(),
            getBeaconListFragment());
  }

  public void guiActionAfterRegisterBeacon(boolean result) {
    String title = "Register Beacon";
    String message;
    if (!result) {
      message = "The broken beacon is successfully registered.";
    } else {
      message = "The beacon is successfully registered.";
    }
    GUIUtils.showOKAlertDialog(getBeaconRegisterFragment().getActivity(), title, message);
    switchToBeaconList(getBeaconDataService().getActiveBeacon().getConfigurable());
  }

  public String getCurrentMac() {
    return getBeaconDataService().getActiveBeacon().getMac();
  }

  public String getCurrentSBeaconId() {
    return getBeaconDataService().getActiveBeacon().getSBeaconId();
  }

  public BeaconRegisterFragment getBeaconRegisterFragment() {
    return this.beaconRegisterfragment;
  }

  public BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private BeaconListFragment getBeaconListFragment() {
    return this.beaconListFragment;
  }

  private static Pattern getsBeaconIdPattern() {
    return sBeaconIdPattern;
  }

  private static Pattern getMacAddressPattern() {
    return macAddressPattern;
  }

  private static Pattern getOfficialMacAddressPattern() {
    return officialMacAddressPattern;
  }

  private ProgressDialog getProgressDialog() {
    return this.progressDialog;
  }

  private Deque<BeaconObject> getConfigQueue() {
    return this.configQueue;
  }

}
