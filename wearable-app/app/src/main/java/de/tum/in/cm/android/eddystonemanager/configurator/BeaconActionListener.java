package de.tum.in.cm.android.eddystonemanager.configurator;

import android.util.Log;

import com.bluvision.beeks.sdk.commands.model.DataLog;
import com.bluvision.beeks.sdk.constants.BeaconType;
import com.bluvision.beeks.sdk.interfaces.BeaconConfigurationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import de.tum.in.cm.android.eddystonemanager.controller.ApplicationController;
import de.tum.in.cm.android.eddystonemanager.controller.MainController;
import de.tum.in.cm.android.eddystonemanager.data.BeaconObject;
import de.tum.in.cm.android.eddystonemanager.data.ConnectionRate;
import de.tum.in.cm.android.eddystonemanager.data.EddystonePackets;
import de.tum.in.cm.android.eddystonemanager.data.ExtendedPacket;
import de.tum.in.cm.android.eddystonemanager.data.IBeaconConfig;
import de.tum.in.cm.android.eddystonemanager.data.Maintenance;
import de.tum.in.cm.android.eddystonemanager.data.Packet;
import de.tum.in.cm.android.eddystonemanager.data.UidConfig;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.AdvertisedBeacons;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.AdvertisementRatesConstants;

import static de.tum.in.cm.android.eddystonemanager.controller.ApplicationController.nextBeacon;

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
    getBeacon().getStatistics().logOnConnect(connected, authenticated);

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
          getBeacon().getStatistics().startIdentify();
          break;
        case Register:
          getBeacon().getStatistics().startRegister();
          break;
        case Update:
          getBeacon().getStatistics().startUpdate();
          break;
        case ConnectTest:
          testConnect(getBeacon());
          break;
        case ConfigTest:
          getBeacon().getStatistics().startRegister();
          break;
        case VerifyConfig:
          getBeacon().getStatistics().startVerifyConfig();
          break;
        case ConfigPartsTest:
          getBeacon().getStatistics().startConfigPartsTest();
          break;
        case PreConfigure:
          getBeacon().getStatistics().startRegister();
          break;
        case MemoryTest:
          getBeacon().getStatistics().startConfigPartsTest();
          break;
      }
    } else {
      Log.d(TAG, "Connection to beacon failed.");
    }
  }

  @Override
  public void onDisconnect() {
    ApplicationController.setRunBeaconConfig(false);
    getBeacon().getStatistics().logOnDisconnect();
    getBeacon().getConfigurable().setConnected(false);

    if (getBeacon().getAction() == Action.ConnectTest) {
      getBeacon().getStatistics().connectTest();
    } else if (getBeacon().getAction() == Action.ConfigTest) {
      getBeacon().getStatistics().configTest();
    } else if (getBeacon().getAction() == Action.ConfigPartsTest) {
      getBeacon().getStatistics().configPartsTest();
    } else if (getBeacon().getAction() == Action.MemoryTest) {
      getBeacon().getStatistics().configMemoryTest();
    } else if (getBeacon().getAction() == Action.PreConfigure) {
      getBeacon().getStatistics().preConfigureBeacon();
    } else if (getBeacon().getAction() == Action.VerifyConfig) {
      nextBeacon();
      getBeacon().getStatistics().calculateResult();
      getBeacon().getStatistics().endVerifyConfig(true);
    } else if (isConnected() && !isAuthenticated()) {
      getBeacon().connect();
    } else if (isConnected() && isAuthenticated()) {
      if (getBeacon().getAction() == Action.Identify) {
        nextBeacon();
        getBeacon().getStatistics().endIdentify();
      } else if (getBeacon().getAction() == Action.Update) {
        nextBeacon();
        getBeacon().getStatistics().endUpdate(true);
      } else if (getBeacon().getAction() == Action.Register) {
        nextBeacon();
        getBeacon().getStatistics().endRegister(true);
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
    getBeacon().getStatistics().logOnFailedToSetEddystoneUrl(true);
  }

  @Override
  public void onFailedToReadEddystoneURL() {
    getBeacon().getStatistics().logOnFailedToReadEddystoneUrl(true);
  }

  private void verifyEddystoneUrl(String url) {
    boolean status = getBeacon().getConfig().getEddystone().getUrl().equals(url);
    getBeacon().getConfig().getStatus().setEddystone(status);
    getBeacon().getStatistics().logOnSetEddystoneUrl(status);
  }
  /* ===================================================*/

  /* ===================================================*/
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
    getBeacon().getStatistics().logOnFailedToSetEddystoneUid(true, true);
  }

  @Override
  public void onFailedToReadEddystoneUID() {
    getBeacon().getStatistics().logOnFailedToReadEddystoneUid(true, true);
  }

  private void verifyEddystoneUid(byte[] nameSpace, byte[] instanceId) {
    UidConfig uidConfig = getBeacon().getConfig().getEddystone().getUid();
    boolean statusNamespace = Arrays.equals(uidConfig.getNamespace(), nameSpace);
    boolean statusInstance = Arrays.equals(uidConfig.getInstance(), instanceId);
    boolean totalStatus = statusNamespace && statusInstance;
    getBeacon().getConfig().getStatus().setEddystone(totalStatus);
    getBeacon().getStatistics().logOnSetEddystoneUid(statusInstance, statusNamespace);
  }
  /* ===================================================*/

  /* ===================================================*/
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
      getBeacon().getStatistics().logOnSetFrameTypeIntervalTxPowerUrl(statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    } else if (advertisementType == AdvertisedBeacons.TLM.getType()) {
      getBeacon().getStatistics().logEndEddystoneTlm();
      getBeacon().getConfig().getStatus().setEddystone(totalStatus);
      getBeacon().getStatistics().logOnSetFrameTypeIntervalTxPowerTlm(statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    } else if (advertisementType == AdvertisedBeacons.UID.getType()) {
      getBeacon().getStatistics().logOnSetFrameTypeIntervalTxPowerUid(statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    } else if (advertisementType == AdvertisedBeacons.S_BEACON.getType()) {
      getBeacon().getStatistics().logEndSBeacon();
      getBeacon().getStatistics().logOnSetFrameTypeIntervalTxPowerSBeacon(statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    } else if (advertisementType == AdvertisedBeacons.I_BEACON.getType()) {
      getBeacon().getStatistics().logEndIBeacon();
      getBeacon().getConfig().getStatus().setIBeacon(totalStatus);
      getBeacon().getStatistics().logOnSetFrameTypeIntervalTxPowerIBeacon(statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
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
    if (lastConfigType == BeaconType.EDDYSTONE_URL_BEACON) {
      getBeacon().getStatistics().logOnFailedToSetFrameTypeIntervalTxPowerUrl(true, true, true, true);
    } else if (lastConfigType == BeaconType.EDDYSTONE_TLM_BEACON) {
      getBeacon().getStatistics().logOnFailedToSetFrameTypeIntervalTxPowerTlm(true, true, true, true);
    } else if (lastConfigType == BeaconType.EDDYSTONE_UID_BEACON) {
      getBeacon().getStatistics().logOnFailedToSetFrameTypeIntervalTxPowerUid(true, true, true, true);
    } else if (lastConfigType == BeaconType.S_BEACON) {
      getBeacon().getStatistics().logOnFailedToSetFrameTypeIntervalTxPowerSBeacon(true, true, true, true);
    } else if (lastConfigType == BeaconType.I_BEACON) {
      getBeacon().getStatistics().logOnFailedToSetFrameTypeIntervalTxPowerIBeacon(true, true, true, true);
    }
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
    if (lastConfigType == BeaconType.EDDYSTONE_URL_BEACON) {
      getBeacon().getStatistics().logOnFailedToReadFrameTypeIntervalTxPowerUrl(true, true, true, true);
    } else if (lastConfigType == BeaconType.EDDYSTONE_TLM_BEACON) {
      getBeacon().getStatistics().logOnFailedToReadFrameTypeIntervalTxPowerTlm(true, true, true, true);
    } else if (lastConfigType == BeaconType.EDDYSTONE_UID_BEACON) {
      getBeacon().getStatistics().logOnFailedToReadFrameTypeIntervalTxPowerUid(true, true, true, true);
    } else if (lastConfigType == BeaconType.S_BEACON) {
      getBeacon().getStatistics().logOnFailedToReadFrameTypeIntervalTxPowerSBeacon(true, true, true, true);
    } else if (lastConfigType == BeaconType.I_BEACON) {
      getBeacon().getStatistics().logOnFailedToReadFrameTypeIntervalTxPowerIBeacon(true, true, true, true);
    }
    if (getBeacon().getConfigController().getNextAction() != null &&
            (lastConfigType == BeaconType.EDDYSTONE_TLM_BEACON ||
            lastConfigType == BeaconType.S_BEACON ||
            lastConfigType == BeaconType.I_BEACON)) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }
  /* ===================================================*/

  /* ===================================================*/
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
    if (advertisementType == AdvertisedBeacons.URL.getType()) {
      getBeacon().getStatistics().logEndEddystoneUrl();
      getBeacon().getStatistics().logOnSetFrameTypeConnectionRatesUrl(statusConnectable, statusNonConnectable);
    } else if (advertisementType == AdvertisedBeacons.UID.getType()) {
      getBeacon().getStatistics().logEndEddystoneUid();
      getBeacon().getStatistics().logOnSetFrameTypeConnectionRatesUid(statusConnectable, statusNonConnectable);
    }
    if (getBeacon().getConfigController().getNextAction() != null) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onFailedToSetFrameTypeConnectionRates() {
    if (getBeacon().getConfigController().getLastConfigType() == BeaconType.EDDYSTONE_URL_BEACON) {
      getBeacon().getStatistics().logOnFailedToSetFrameTypeConnectionRatesUrl(true, true);
    } else if (getBeacon().getConfigController().getLastConfigType() == BeaconType.EDDYSTONE_UID_BEACON) {
      getBeacon().getStatistics().logOnFailedToSetFrameTypeConnectionRatesUid(true, true);
    }
    if (getBeacon().getConfigController().getNextAction() != null) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onFailedToReadFrameTypeConnectionRates() {
    if (getBeacon().getConfigController().getLastConfigType() == BeaconType.EDDYSTONE_URL_BEACON) {
      getBeacon().getStatistics().logOnFailedToReadFrameTypeConnectionRatesUrl(true, true);
    } else if (getBeacon().getConfigController().getLastConfigType() == BeaconType.EDDYSTONE_UID_BEACON) {
      getBeacon().getStatistics().logOnFailedToReadFrameTypeConnectionRatesUid(true, true);
    }
    if (getBeacon().getConfigController().getNextAction() != null) {
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }
  /* ===================================================*/

  /* ===================================================*/
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
    getBeacon().getStatistics().logOnIBeaconUuid(status);
  }

  @Override
  public void onFailedToSetIBeaconUUID() {
    getBeacon().getConfig().getStatus().setIBeacon(false);
    getBeacon().getStatistics().logOnFailedtoSetIBeaconUuid(true);
  }

  @Override
  public void onFailedToReadIBeaconUUID() {
    getBeacon().getStatistics().logOnFailedToReadIBeaconUuid(true);
  }

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
    getBeacon().getStatistics().logOnIBeaconMajorMinor(statusMajor, statusMinor);
  }

  @Override
  public void onFailedToSetIBeaconMajorAndMinor() {
    getBeacon().getConfig().getStatus().setIBeacon(false);
    getBeacon().getStatistics().logOnFailedToSetIBeaconMajorMinor(true, true);
  }

  @Override
  public void onFailedToReadIBeaconMajorAndMinor() {
    getBeacon().getStatistics().logOnFailedToReadIBeaconMajorMinor(true, true);
  }
  /* ===================================================*/

  /* ===================================================*/
  @Override
  public void onReadDeviceStatus(float battery,
                                 float temperature,
                                 short firmware) {
    Maintenance maintenance = getBeacon().getConfig().getMaintenance();
    maintenance.setBatteryVoltage(battery);
    maintenance.setFirmware(firmware);
    getBeacon().getStatistics().logOnReadMaintenanceDeviceStatus(true);

    if (getBeacon().getAction() == Action.ConfigPartsTest ||
            getBeacon().getAction() == Action.MemoryTest) {
      getBeacon().getStatistics().logEndMaintenance();
      getBeacon().getConfigController().getNextAction().execute(getBeacon());
    }
  }

  @Override
  public void onFailedToReadDeviceStatus() {
    getBeacon().getStatistics().logOnFailedToReadMaintenanceDeviceStatus(true);
  }
  /* ===================================================*/

  /* ===================================================*/
  @Override
  public void onSetPassword(boolean success) {
    getBeacon().getStatistics().logOnSetPassword(success);
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
    if (MainController.SETTING.isUserStudy()) {
      getBeacon().getStatistics().calculateResult();
    }
    if (getBeacon().isBroken() && getBeacon().nextWrite()) {
      if (getBeacon().getAction() == Action.Update) {
        getBeacon().getStatistics().startUpdate();
      } else if (getBeacon().getAction() == Action.Register) {
        getBeacon().getStatistics().startRegister();
      }
    } else {
      getBeacon().getStatistics().disconnect();
    }
  }
  /* ===================================================*/

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
