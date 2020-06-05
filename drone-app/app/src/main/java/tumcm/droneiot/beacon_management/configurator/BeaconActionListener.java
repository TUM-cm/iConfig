package tumcm.droneiot.beacon_management.configurator;

import android.util.Log;

import com.bluvision.beeks.sdk.commands.model.DataLog;
import com.bluvision.beeks.sdk.constants.BeaconType;
import com.bluvision.beeks.sdk.interfaces.BeaconConfigurationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import tumcm.droneiot.beacon_management.controller.ApplicationController;
import tumcm.droneiot.beacon_management.data.BeaconObject;
import tumcm.droneiot.beacon_management.data.ConnectionRate;
import tumcm.droneiot.beacon_management.data.EddystonePackets;
import tumcm.droneiot.beacon_management.data.ExtendedPacket;
import tumcm.droneiot.beacon_management.data.IBeaconConfig;
import tumcm.droneiot.beacon_management.data.Maintenance;
import tumcm.droneiot.beacon_management.data.Packet;
import tumcm.droneiot.beacon_management.data.UidConfig;
import tumcm.droneiot.beacon_management.utils.beacon.AdvertisedBeacons;
import tumcm.droneiot.beacon_management.utils.beacon.AdvertisementRatesConstants;

import static tumcm.droneiot.beacon_management.controller.ApplicationController.nextBeacon;

public class BeaconActionListener extends BeaconAction implements BeaconConfigurationListener {

  public static final String TAG = BeaconActionListener.class.getSimpleName();

  private final BeaconObject beacon;
  private boolean connected;
  private boolean authenticated;

  public BeaconActionListener(BeaconObject beacon) {
    this.beacon = beacon;
  }

  @Override
  public void onConnect(boolean connected, boolean authenticated) {
    this.connected = connected;
    this.authenticated = authenticated;
    Log.d(TAG, "connected: " + connected);
    Log.d(TAG, "authenticated: " + authenticated);
    if (connected && !authenticated) {
      Log.e(TAG, "Authentication failed, make sure you have used the correct password.");
      getBeacon().setStatus(false);
    } else if (connected && authenticated) {
      getBeacon().setLastPassword(null);
      getBeacon().getConfigurable().setConnected(true);
      getBeacon().setStatus(true);
      switch (getBeacon().getAction()) {
        case Identify:
          identifyBeacon(getBeacon());
          break;
        case Register:
          registerWriteBeaconSettings(getBeacon());
          break;
        case Update:
          updateWriteBeaconSettings(getBeacon());
          break;
      }
    } else {
      Log.d(TAG, "Connection to beacon failed.");
    }
  }

  @Override
  public void onDisconnect() {
    ApplicationController.setRunBeaconConfig(false);
    getBeacon().getConfigurable().setConnected(false);
    if (isConnected() && !isAuthenticated()) {
      getBeacon().connect();
    } else if (isConnected() && isAuthenticated()) {
      if (getBeacon().getAction() == Action.Identify) {
        nextBeacon();
        getBeacon().getServiceCallback().identifyAction();
      } else if (getBeacon().getAction() == Action.Update) {
        nextBeacon();
        getBeacon().getServiceCallback().updateAction(getBeacon(), true);
      } else if (getBeacon().getAction() == Action.Register) {
        nextBeacon();
        getBeacon().getServiceCallback().registerAction(getBeacon(), true);
      }
    }
  }

  @Override
  public void onCommandToNotConnectedBeacon() {
    Log.e(TAG, "Command sent to not connected beacon.");
  }

  /* ===================================================*/
  @Override
  public void onSetEddystoneURL(String url) {
    verifyEddystoneUrl(url);
  }

  @Override
  public void onReadEddystoneURL(String url) {
    verifyEddystoneUrl(url);
  }

  @Override
  public void onFailedToSetEddystoneURL() {
    getBeacon().getConfig().getStatus().setEddystone(false);
  }

  @Override
  public void onFailedToReadEddystoneURL() {}

  private void verifyEddystoneUrl(String url) {
    boolean status = getBeacon().getConfig().getEddystone().getUrl().equals(url);
    getBeacon().getConfig().getStatus().setEddystone(status);
  }

  @Override
  public void onSetEddystoneUID(byte[] nameSpace, byte[] instanceId) {
    verifyEddystoneUid(nameSpace, instanceId);
  }

  @Override
  public void onReadEddystoneUID(byte[] nameSpace, byte[] instanceId) {
    verifyEddystoneUid(nameSpace, instanceId);
  }

  @Override
  public void onFailedToSetEddystoneUID() {
    getBeacon().getConfig().getStatus().setEddystone(false);
  }

  @Override
  public void onFailedToReadEddystoneUID() {}

  private void verifyEddystoneUid(byte[] nameSpace, byte[] instanceId) {
    UidConfig uidConfig = getBeacon().getConfig().getEddystone().getUid();
    boolean statusNamespace = Arrays.equals(uidConfig.getNamespace(), nameSpace);
    boolean statusInstance = Arrays.equals(uidConfig.getInstance(), instanceId);
    boolean totalStatus = statusNamespace && statusInstance;
    getBeacon().getConfig().getStatus().setEddystone(totalStatus);
  }

  @Override
  public void onSetFrameTypeIntervalTxPower(byte advertisementType,
                                            byte txEnergySaving,
                                            byte txStandard,
                                            float advEnergySaving,
                                            float advStandard) {
    verifyFrameTypeIntervalTxPower(advertisementType, txEnergySaving, txStandard,
            advEnergySaving, advStandard);
  }

  @Override
  public void onReadFrameTypeIntervalTxPower(byte advertisementType,
                                             byte txEnergySaving,
                                             byte txStandard,
                                             float advEnergySaving,
                                             float advStandard) {
    verifyFrameTypeIntervalTxPower(advertisementType, txEnergySaving, txStandard,
            advEnergySaving, advStandard);
  }

  private void verifyFrameTypeIntervalTxPower(byte advertisementType,
                                              byte txEnergySaving,
                                              byte txStandard,
                                              float advEnergySaving,
                                              float advStandard) {
    byte expectedTxEnergySaving = Byte.MAX_VALUE;
    byte expectedTxStandard = Byte.MAX_VALUE;
    float expectedAdvEnergySaving = Float.MAX_VALUE;
    float expectedAdvStandard = Float.MAX_VALUE;

    advEnergySaving = AdvertisementRatesConstants.convertToFromDevice(advEnergySaving);
    advStandard = AdvertisementRatesConstants.convertToFromDevice(advStandard);

    if (advertisementType == AdvertisedBeacons.URL.getType()) {
      ExtendedPacket urlPacket = getBeacon().getConfig().getEddystone().getPackets().getUrl();
      expectedTxStandard = urlPacket.getDayMode().getTransmissionPower();
      expectedTxEnergySaving = urlPacket.getNightMode().getTransmissionPower();
      expectedAdvStandard = urlPacket.getDayMode().getAdvertisementRate();
      expectedAdvEnergySaving = urlPacket.getNightMode().getAdvertisementRate();
    } else if (advertisementType == AdvertisedBeacons.TLM.getType()) {
      Packet tlmPacket = getBeacon().getConfig().getEddystone().getPackets().getTlm();
      expectedTxStandard = tlmPacket.getDayMode().getTransmissionPower();
      expectedTxEnergySaving = tlmPacket.getNightMode().getTransmissionPower();
      expectedAdvStandard = tlmPacket.getDayMode().getAdvertisementRate();
      expectedAdvEnergySaving = tlmPacket.getNightMode().getAdvertisementRate();
    } else if (advertisementType == AdvertisedBeacons.UID.getType()) {
      ExtendedPacket uidPacket = getBeacon().getConfig().getEddystone().getPackets().getUid();
      expectedTxStandard = uidPacket.getDayMode().getTransmissionPower();
      expectedTxEnergySaving = uidPacket.getNightMode().getTransmissionPower();
      expectedAdvStandard = uidPacket.getDayMode().getAdvertisementRate();
      expectedAdvEnergySaving = uidPacket.getNightMode().getAdvertisementRate();
    } else if (advertisementType == AdvertisedBeacons.S_BEACON.getType()) {
      Packet sBeaconPacket = getBeacon().getConfig().getSBeacon().getPacket();
      expectedTxStandard = sBeaconPacket.getDayMode().getTransmissionPower();
      expectedTxEnergySaving = sBeaconPacket.getNightMode().getTransmissionPower();
      expectedAdvStandard = sBeaconPacket.getDayMode().getAdvertisementRate();
      expectedAdvEnergySaving = sBeaconPacket.getNightMode().getAdvertisementRate();
    } else if (advertisementType == AdvertisedBeacons.I_BEACON.getType()) {
      Packet iBeaconPacket = getBeacon().getConfig().getIBeacon().getPacket();
      expectedTxStandard = iBeaconPacket.getDayMode().getTransmissionPower();
      expectedTxEnergySaving = iBeaconPacket.getNightMode().getTransmissionPower();
      expectedAdvStandard = iBeaconPacket.getDayMode().getAdvertisementRate();
      expectedAdvEnergySaving = iBeaconPacket.getNightMode().getAdvertisementRate();
    }

    boolean statusTxEnergy = expectedTxEnergySaving == txEnergySaving;
    boolean statusTxStandard = expectedTxStandard == txStandard;
    boolean statusAdvEnergy = expectedAdvEnergySaving == advEnergySaving;
    boolean statusAdvStandard = expectedAdvStandard == advStandard;
    boolean totalStatus = (statusTxEnergy && statusTxStandard) && (statusAdvEnergy && statusAdvStandard);

    if (advertisementType == AdvertisedBeacons.URL.getType()) {
      getBeacon().getConfig().getStatus().setEddystone(totalStatus);
    } else if (advertisementType == AdvertisedBeacons.TLM.getType()) {
      getBeacon().getConfig().getStatus().setEddystone(totalStatus);
    } else if (advertisementType == AdvertisedBeacons.I_BEACON.getType()) {
      getBeacon().getConfig().getStatus().setIBeacon(totalStatus);
    }
    if (getBeacon().getConfigController().getNextAction() != null &&
            (advertisementType == AdvertisedBeacons.TLM.getType() ||
            advertisementType == AdvertisedBeacons.S_BEACON.getType() ||
            advertisementType == AdvertisedBeacons.I_BEACON.getType())) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onFailedToSetFrameTypeIntervalTxPower() {
    BeaconType lastConfigType = getBeacon().getConfigController().getLastConfigType();
    if (getBeacon().getConfigController().getNextAction() != null &&
            (lastConfigType == BeaconType.EDDYSTONE_TLM_BEACON ||
            lastConfigType == BeaconType.S_BEACON ||
            lastConfigType == BeaconType.I_BEACON)) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onFailedToReadFrameTypeIntervalTxPower() {
    BeaconType lastConfigType = getBeacon().getConfigController().getLastConfigType();
    if (getBeacon().getConfigController().getNextAction() != null &&
            (lastConfigType == BeaconType.EDDYSTONE_TLM_BEACON ||
            lastConfigType == BeaconType.S_BEACON ||
            lastConfigType == BeaconType.I_BEACON)) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onSetFrameTypeConnectionRates(byte advertisementType,
                                            byte connectable,
                                            byte nonConnectable) {
    verifyFrameTypeConnectionRates(advertisementType, connectable, nonConnectable);
  }

  @Override
  public void onReadFrameTypeConnectionRates(byte advertisementType,
                                             byte connectable,
                                             byte nonConnectable) {
    verifyFrameTypeConnectionRates(advertisementType, connectable, nonConnectable);
  }

  private void verifyFrameTypeConnectionRates(byte advertisementType,
                                              byte connectable,
                                              byte nonConnectable) {
    EddystonePackets eddystonePackets = getBeacon().getConfig().getEddystone().getPackets();
    byte expectedConnectable = Byte.MAX_VALUE;
    byte expectedNonConnectable = Byte.MAX_VALUE;
    if (advertisementType == AdvertisedBeacons.URL.getType()) {
      ConnectionRate connectionRate = eddystonePackets.getUrl().getConnectionRate();
      expectedConnectable = connectionRate.getConnectableRate();
      expectedNonConnectable = connectionRate.getNonConnectableRate();
    } else if (advertisementType == AdvertisedBeacons.UID.getType()) {
      ConnectionRate connectionRate = eddystonePackets.getUid().getConnectionRate();
      expectedConnectable = connectionRate.getConnectableRate();
      expectedNonConnectable = connectionRate.getNonConnectableRate();
    }
    boolean statusConnectable = expectedConnectable == connectable;
    boolean statusNonConnectable = expectedNonConnectable == nonConnectable;
    boolean totalStatus = statusConnectable && statusNonConnectable;
    getBeacon().getConfig().getStatus().setEddystone(totalStatus);
    if (getBeacon().getConfigController().getNextAction() != null) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onFailedToSetFrameTypeConnectionRates() {
    if (getBeacon().getConfigController().getNextAction() != null) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onFailedToReadFrameTypeConnectionRates() {
    if (getBeacon().getConfigController().getNextAction() != null) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onSetIBeaconUUID(UUID uuid) {
    verifyIBeaconUuid(uuid);
  }

  @Override
  public void onReadIBeaconUUID(UUID uuid) {
    verifyIBeaconUuid(uuid);
  }

  private void verifyIBeaconUuid(UUID uuid) {
    IBeaconConfig iBeaconConfig = getBeacon().getConfig().getIBeacon();
    boolean status = iBeaconConfig.getUuid().toString().equals(uuid.toString());
    getBeacon().getConfig().getStatus().setIBeacon(status);
  }

  @Override
  public void onFailedToSetIBeaconUUID() {
    getBeacon().getConfig().getStatus().setIBeacon(false);
  }

  @Override
  public void onFailedToReadIBeaconUUID() {}

  @Override
  public void onSetIBeaconMajorAndMinor(int major, int minor) {
    verifyIBeaconMajorMinor(major, minor);
  }

  @Override
  public void onReadIBeaconMajorAndMinor(int major, int minor) {
    verifyIBeaconMajorMinor(major, minor);
  }

  private void verifyIBeaconMajorMinor(int major, int minor) {
    IBeaconConfig iBeaconConfig = getBeacon().getConfig().getIBeacon();
    boolean statusMajor = iBeaconConfig.getMajor() == major;
    boolean statusMinor = iBeaconConfig.getMinor() == minor;
    boolean totalStatus = statusMajor && statusMinor;
    getBeacon().getConfig().getStatus().setIBeacon(totalStatus);
  }

  @Override
  public void onFailedToSetIBeaconMajorAndMinor() {
    getBeacon().getConfig().getStatus().setIBeacon(false);
  }

  @Override
  public void onFailedToReadIBeaconMajorAndMinor() {}

  @Override
  public void onReadDeviceStatus(float battery,
                                 float temperature,
                                 short firmware) {
    Maintenance maintenance = getBeacon().getConfig().getMaintenance();
    maintenance.setBatteryVoltage(battery);
    maintenance.setFirmware(firmware);
  }

  @Override
  public void onFailedToReadDeviceStatus() {}

  @Override
  public void onSetPassword(boolean success) {
    if (success) {
      // Update beacon config for later sync with database config
      String newPassword = getBeacon().getConfig().getNewPassword();
      if (newPassword != null && newPassword.length() > 0) {
        getBeacon().getConfig().setPassword(newPassword);
        getBeacon().getConfig().setNewPassword("");
      }
    } else {
      getBeacon().getConfig().getStatus().setSBeacon(false);
    }
    checkBeaconStatus();
  }

  public void checkBeaconStatus() {
    if (getBeacon().isBroken() && getBeacon().nextWrite()) {
      if (getBeacon().getAction() == Action.Update) {
        updateWriteBeaconSettings(getBeacon());
      } else if (getBeacon().getAction() == Action.Register) {
        registerWriteBeaconSettings(getBeacon());
      }
    } else {
      getBeacon().getConfigurable().disconnect();
    }
  }

  public BeaconObject getBeacon() {
    return this.beacon;
  }

  private boolean isConnected() {
    return this.connected;
  }

  private boolean isAuthenticated() {
    return this.authenticated;
  }

  // Not Used
  @Override
  public void onConnectionExist() {
  }

  @Override
  public void onReadConnectionSettings(int smallestAcceptableInterval,
                                       int highestAcceptableInterval,
                                       int connectionLatency,
                                       int connectionLostTimeout) {
  }

  @Override
  public void onSetConnectionSettings(int smallestAcceptableInterval,
                                      int highestAcceptableInterval,
                                      int connectionLatency,
                                      int connectionLostTimeout) {
  }

  @Override
  public void onFailedToReadConnectionSettings() {
  }

  @Override
  public void onFailedToSetConnectionSettings() {
  }

  @Override
  public void onReadTemperature(double temperature) {
  }

  @Override
  public void onFailedToReadTemperature() {
  }

  @Override
  public void onUpdateFirmware(double progress) {
  }

  @Override
  public void onFailedToUpdateFirmware(int status) {
  }

  @Override
  public void onReadDataLog(ArrayList<DataLog> arrayList) {
  }

  @Override
  public void onEnabledDataLog() {
  }

  @Override
  public void onFailedToReadDataLog() {
  }

  @Override
  public void onReadAdvertisementSettings(float disconnectInterval,
                                          float disconnectTimeout,
                                          float energySavingTimeout) {
  }

  @Override
  public void onSetAdvertisementSettings(float disconnectInterval,
                                         float disconnectTimeout,
                                         float energySavingTimeout) {
  }

  @Override
  public void onFailedToReadAdvertisementSettings() {
  }

  @Override
  public void onFailedToSetAdvertisementSettings() {
  }

  @Override
  public void onSetAccelerometerConfiguration() {
  }

  @Override
  public void onFailedToSetAccelerometerConfiguration() {
  }

}
