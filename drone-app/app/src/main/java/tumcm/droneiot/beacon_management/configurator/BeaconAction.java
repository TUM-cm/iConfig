package tumcm.droneiot.beacon_management.configurator;

import com.bluvision.beeks.sdk.constants.BeaconType;

import tumcm.droneiot.beacon_management.controller.NextAction;
import tumcm.droneiot.beacon_management.data.BeaconObject;
import tumcm.droneiot.beacon_management.data.ExtendedPacket;
import tumcm.droneiot.beacon_management.data.Packet;
import tumcm.droneiot.beacon_management.utils.beacon.AdvertisedBeacons;
import tumcm.droneiot.beacon_management.utils.beacon.AdvertisementRatesConstants;

public class BeaconAction {

  private void alert(BeaconObject beacon) {
    beacon.getConfigurable().alert(true, true);
  }

  public void identifyBeacon(BeaconObject beacon) {
    alert(beacon);
    beacon.getConfigurable().disconnect();
  }

  public void verifyBeaconConfig(BeaconObject beacon) {
    alert(beacon);

    readEddystoneUrl(beacon);
    readEddystoneUid(beacon);
    readIBeacon(beacon);

    beacon.getConfigController().setLastConfigType(BeaconType.EDDYSTONE_URL_BEACON);
    beacon.getConfigController().setNextAction(new ReadUidPacket());
    readEddystoneUrlPacket(beacon);
  }

  public void updateWriteBeaconSettings(BeaconObject beacon) {
    writeBeaconSettings(beacon);
  }

  public void registerWriteBeaconSettings(BeaconObject beacon) {
    writeIBeacon(beacon);
    writeEddystoneUid(beacon);
    writeBeaconSettings(beacon);
  }

  private void writeBeaconSettings(BeaconObject beacon) {
    alert(beacon);
    beacon.setNewPasswordAvailable();

    writeEddystoneUrl(beacon);
    collectMaintenanceData(beacon);

    beacon.getConfigController().setLastConfigType(BeaconType.EDDYSTONE_URL_BEACON);
    beacon.getConfigController().setNextAction(new ConfigUidPacket());
    writeEddystoneUrlPacket(beacon);
  }

  public class ReadUidPacket implements NextAction {
    @Override
    public void execute(BeaconObject beacon) {
      beacon.getConfigController().setLastConfigType(BeaconType.EDDYSTONE_UID_BEACON);
      beacon.getConfigController().setNextAction(new ReadTlmPacket());
      readEddystoneUidPacket(beacon);
    }
  }

  public class ReadTlmPacket implements NextAction {
    @Override
    public void execute(BeaconObject beacon) {
      beacon.getConfigController().setLastConfigType(BeaconType.EDDYSTONE_TLM_BEACON);
      beacon.getConfigController().setNextAction(new ReadIBeaconPacket());
      readEddystoneTlmPacket(beacon);
    }
  }

  public class ReadIBeaconPacket implements NextAction {
    @Override
    public void execute(BeaconObject beacon) {
      beacon.getConfigController().setLastConfigType(BeaconType.I_BEACON);
      beacon.getConfigController().setNextAction(new ReadSBeaconPacket());
      readIBeaconPacket(beacon);
    }
  }

  public class ReadSBeaconPacket implements NextAction {
    @Override
    public void execute(BeaconObject beacon) {
      beacon.getConfigController().setLastConfigType(BeaconType.S_BEACON);
      beacon.getConfigController().setNextAction(new ReadDisconnect());
      readSBeaconPacket(beacon);
    }
  }

  public class ReadDisconnect implements NextAction {
    @Override
    public void execute(BeaconObject beacon) {
      beacon.getConfigurable().disconnect();
    }
  }

  public class ConfigUidPacket implements NextAction {
    public void execute(BeaconObject beacon) {
      beacon.getConfigController().setLastConfigType(BeaconType.EDDYSTONE_UID_BEACON);
      beacon.getConfigController().setNextAction(new ConfigTlmPacket());
      writeEddystoneUidPacket(beacon);
    }
  }

  public class ConfigTlmPacket implements NextAction {
    public void execute(BeaconObject beacon) {
      beacon.getConfigController().setLastConfigType(BeaconType.EDDYSTONE_TLM_BEACON);
      beacon.getConfigController().setNextAction(new ConfigIBeaconPacket());
      writeEddystoneTlmPacket(beacon);
    }
  }

  public class ConfigIBeaconPacket implements NextAction {
    public void execute(BeaconObject beacon) {
      beacon.getConfigController().setLastConfigType(BeaconType.I_BEACON);
      beacon.getConfigController().setNextAction(new ConfigSBeaconPacket());
      writeIBeaconPacket(beacon);
    }
  }

  public class ConfigSBeaconPacket implements NextAction {
    public void execute(BeaconObject beacon) {
      beacon.getConfigController().setLastConfigType(BeaconType.S_BEACON);
      beacon.getConfigController().setNextAction(new SetPassword());
      writeSBeaconPacket(beacon);
    }
  }

  public class SetPassword implements NextAction {
    public void execute(BeaconObject beacon) {
      if (beacon.isNewPasswordAvailable()) {
        beacon.getConfigurable().setPassword(beacon.getConfig().getNewPassword());
      } else {
        beacon.getBeaconActionListener().checkBeaconStatus();
      }
    }
  }

  private void writeEddystoneUrl(BeaconObject beacon) {
    beacon.getConfigurable().setEddystoneUrl(beacon.getConfig().getEddystone().getUrl());
  }

  private void readEddystoneUrl(BeaconObject beacon) {
    beacon.getConfigurable().readEddystoneURL();
  }

  private void writeEddystoneUrlPacket(BeaconObject beacon) {
    ExtendedPacket urlPacket = beacon.getConfig().getEddystone().getPackets().getUrl();
    beacon.getConfigurable().setFrameTypeIntervalTxPower(
            AdvertisedBeacons.URL.getType(),
            urlPacket.getNightMode().getTransmissionPower(),
            urlPacket.getDayMode().getTransmissionPower(),
            AdvertisementRatesConstants.convertToDevice(urlPacket.getNightMode().getAdvertisementRate()),
            AdvertisementRatesConstants.convertToDevice(urlPacket.getDayMode().getAdvertisementRate()));
    beacon.getConfigurable().setFrameTypeConnectionRates(
            AdvertisedBeacons.URL.getType(),
            urlPacket.getConnectionRate().getConnectableRate(),
            urlPacket.getConnectionRate().getNonConnectableRate());
  }

  private void readEddystoneUrlPacket(BeaconObject beacon) {
    beacon.getConfigurable().readFrameTypeIntervalTxPower(AdvertisedBeacons.URL.getType());
    beacon.getConfigurable().readFrameTypeConnectionRates(AdvertisedBeacons.URL.getType());
  }

  private void writeEddystoneUid(BeaconObject beacon) {
    beacon.getConfigurable().setEddystoneUID(beacon.getConfig().getEddystone().getUid().getNamespace(),
            beacon.getConfig().getEddystone().getUid().getInstance());
  }

  private void readEddystoneUid(BeaconObject beacon) {
    beacon.getConfigurable().readEddystoneUID();
  }

  private void writeEddystoneUidPacket(BeaconObject beacon) {
    ExtendedPacket uidPacket = beacon.getConfig().getEddystone().getPackets().getUid();
    beacon.getConfigurable().setFrameTypeIntervalTxPower(
            AdvertisedBeacons.UID.getType(),
            uidPacket.getNightMode().getTransmissionPower(),
            uidPacket.getDayMode().getTransmissionPower(),
            AdvertisementRatesConstants.convertToDevice(uidPacket.getNightMode().getAdvertisementRate()),
            AdvertisementRatesConstants.convertToDevice(uidPacket.getDayMode().getAdvertisementRate()));
    beacon.getConfigurable().setFrameTypeConnectionRates(
            AdvertisedBeacons.UID.getType(),
            uidPacket.getConnectionRate().getConnectableRate(),
            uidPacket.getConnectionRate().getNonConnectableRate());
  }

  private void readEddystoneUidPacket(BeaconObject beacon) {
    beacon.getConfigurable().readFrameTypeIntervalTxPower(AdvertisedBeacons.UID.getType());
    beacon.getConfigurable().readFrameTypeConnectionRates(AdvertisedBeacons.UID.getType());
  }

  private void writeEddystoneTlmPacket(BeaconObject beacon) {
    Packet tlmPacket = beacon.getConfig().getEddystone().getPackets().getTlm();
    beacon.getConfigurable().setFrameTypeIntervalTxPower(
            AdvertisedBeacons.TLM.getType(),
            tlmPacket.getNightMode().getTransmissionPower(),
            tlmPacket.getDayMode().getTransmissionPower(),
            AdvertisementRatesConstants.convertToDevice(tlmPacket.getNightMode().getAdvertisementRate()),
            AdvertisementRatesConstants.convertToDevice(tlmPacket.getDayMode().getAdvertisementRate()));
  }

  private void readEddystoneTlmPacket(BeaconObject beacon) {
    beacon.getConfigurable().readFrameTypeIntervalTxPower(AdvertisedBeacons.TLM.getType());
  }

  private void writeSBeaconPacket(BeaconObject beacon) {
    Packet sBeaconPacket = beacon.getConfig().getSBeacon().getPacket();
    beacon.getConfigurable().setFrameTypeIntervalTxPower(
            AdvertisedBeacons.S_BEACON.getType(),
            sBeaconPacket.getNightMode().getTransmissionPower(),
            sBeaconPacket.getDayMode().getTransmissionPower(),
            AdvertisementRatesConstants.convertToDevice(sBeaconPacket.getNightMode().getAdvertisementRate()),
            AdvertisementRatesConstants.convertToDevice(sBeaconPacket.getDayMode().getAdvertisementRate()));
  }

  private void readSBeaconPacket(BeaconObject beacon) {
    beacon.getConfigurable().readFrameTypeIntervalTxPower(AdvertisedBeacons.S_BEACON.getType());
  }

  private void writeIBeacon(BeaconObject beacon) {
    beacon.getConfigurable().setIBeaconMajorMinor(beacon.getConfig().getIBeacon().getMajor(),
            beacon.getConfig().getIBeacon().getMinor());
    beacon.getConfigurable().setIBeaconUUID(beacon.getConfig().getIBeacon().getUuid());
  }

  private void readIBeacon(BeaconObject beacon) {
    beacon.getConfigurable().readIBeaconMajorMinor();
    beacon.getConfigurable().readIBeaconUUID();
  }

  private void writeIBeaconPacket(BeaconObject beacon) {
    Packet iBeaconPacket = beacon.getConfig().getIBeacon().getPacket();
    beacon.getConfigurable().setFrameTypeIntervalTxPower(
            AdvertisedBeacons.I_BEACON.getType(),
            iBeaconPacket.getNightMode().getTransmissionPower(),
            iBeaconPacket.getDayMode().getTransmissionPower(),
            AdvertisementRatesConstants.convertToDevice(iBeaconPacket.getNightMode().getAdvertisementRate()),
            AdvertisementRatesConstants.convertToDevice(iBeaconPacket.getDayMode().getAdvertisementRate()));
  }

  private void readIBeaconPacket(BeaconObject beacon) {
    beacon.getConfigurable().readFrameTypeIntervalTxPower(AdvertisedBeacons.I_BEACON.getType());
  }

  private void collectMaintenanceData(BeaconObject beacon) {
    beacon.setMaintenanceTlm();
    beacon.getConfigurable().readDeviceStatus();
  }

}
