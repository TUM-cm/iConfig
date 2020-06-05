package de.tum.in.cm.android.eddystonemanager.configurator;

import android.util.Log;

import com.bluvision.beeks.sdk.commands.model.DataLog;
import com.bluvision.beeks.sdk.interfaces.BeaconConfigurationListener;

import org.apache.commons.math3.stat.Frequency;

import java.util.ArrayList;
import java.util.UUID;

import de.tum.in.cm.android.eddystonemanager.controller.TestController;
import de.tum.in.cm.android.eddystonemanager.data.BeaconObject;

public class BeaconActionScalabilityListener extends BeaconAction implements BeaconConfigurationListener {

  private static final String TAG = BeaconActionScalabilityListener.class.getSimpleName();
  private final BeaconObject beacon;

  public BeaconActionScalabilityListener(BeaconObject beacon) {
    this.beacon = beacon;
  }

  @Override
  public void onConnect(boolean connected, boolean authenticated) {
    TestController.TEST_CONNECT_COUNTER++;
    getBeacon().getStatistics().logOnConnectScalability(connected, authenticated);
    if (connected && authenticated) {
      TestController.TEST_AUTHENTICATE_COUNTER++;
      configScalability(getBeacon());
    }
  }

  @Override
  public void onDisconnect() {
    getBeacon().getStatistics().logOnDisconnectScalability();
    getBeacon().getStatistics().configTest();
  }

  @Override
  public void onCommandToNotConnectedBeacon() {
    Log.e(TAG, "Command sent to not connected beacon.");
  }

  /* ===================================================*/
  @Override
  public void onSetEddystoneURL(String url) {
    getConfigResult().addValue(true);
  }

  @Override
  public void onFailedToSetEddystoneURL() {
    getConfigResult().addValue(false);
  }
  /* ===================================================*/

  /* ===================================================*/
  @Override
  public void onSetEddystoneUID(byte[] nameSpace, byte[] instanceId) {
    getConfigResult().addValue(true);
  }

  @Override
  public void onFailedToSetEddystoneUID() {
    getConfigResult().addValue(false);
  }
  /* ===================================================*/

  /* ===================================================*/
  @Override
  public void onSetFrameTypeIntervalTxPower(byte advertisementType,
                                            byte txEnergySaving,
                                            byte txStandard,
                                            float advEnergySaving,
                                            float advStandard) {
    getConfigResult().addValue(true);
  }

  @Override
  public void onFailedToSetFrameTypeIntervalTxPower() {
    getConfigResult().addValue(false);
  }
  /* ===================================================*/

  /* ===================================================*/
  @Override
  public void onSetFrameTypeConnectionRates(byte advertisementType,
                                            byte connectable,
                                            byte nonConnectable) {
    getConfigResult().addValue(true);
  }

  @Override
  public void onFailedToSetFrameTypeConnectionRates() {
    getConfigResult().addValue(false);
  }
  /* ===================================================*/

  /* ===================================================*/
  @Override
  public void onSetIBeaconUUID(UUID uuid) {
    getConfigResult().addValue(true);
  }

  @Override
  public void onFailedToSetIBeaconUUID() {
    getConfigResult().addValue(false);
  }

  @Override
  public void onSetIBeaconMajorAndMinor(int major, int minor) {
    getConfigResult().addValue(true);
  }

  @Override
  public void onFailedToSetIBeaconMajorAndMinor() {
    getConfigResult().addValue(false);
  }
  /* ===================================================*/

  /* ===================================================*/
  @Override
  public void onReadDeviceStatus(float battery,
                                 float temperature,
                                 short firmware) {
    getConfigResult().addValue(true);
  }

  @Override
  public void onFailedToReadDeviceStatus() {
    getConfigResult().addValue(false);
  }
  /* ===================================================*/

  @Override
  public void onSetPassword(boolean success) {
    getConfigResult().addValue(success);
    getBeacon().getConfigurable().disconnect();
  }

  private BeaconObject getBeacon() {
    return this.beacon;
  }

  private Frequency getConfigResult() {
    return this.beacon.getStatistics().getConfigResult();
  }

  /* =========================== */
  @Override
  public void onReadEddystoneURL(String url) {}

  @Override
  public void onFailedToReadEddystoneURL() {}

  @Override
  public void onReadEddystoneUID(byte[] nameSpace, byte[] instanceId) {}

  @Override
  public void onFailedToReadEddystoneUID() {}

  @Override
  public void onReadFrameTypeIntervalTxPower(byte advertisementType,
                                             byte txEnergySaving,
                                             byte txStandard,
                                             float advEnergySaving,
                                             float advStandard) {}

  @Override
  public void onFailedToReadFrameTypeIntervalTxPower() {}

  @Override
  public void onReadFrameTypeConnectionRates(byte advertisementType,
                                             byte connectable,
                                             byte nonConnectable) {}

  @Override
  public void onFailedToReadFrameTypeConnectionRates() {}

  @Override
  public void onReadIBeaconUUID(UUID uuid) {}

  @Override
  public void onFailedToReadIBeaconUUID() {}

  @Override
  public void onReadIBeaconMajorAndMinor(int major, int minor) {}

  @Override
  public void onFailedToReadIBeaconMajorAndMinor() {}

  @Override
  public void onConnectionExist() {}

  @Override
  public void onReadConnectionSettings(int smallestAcceptableInterval,
                                       int highestAcceptableInterval,
                                       int connectionLatency,
                                       int connectionLostTimeout) {}

  @Override
  public void onSetConnectionSettings(int smallestAcceptableInterval,
                                      int highestAcceptableInterval,
                                      int connectionLatency,
                                      int connectionLostTimeout) {}

  @Override
  public void onFailedToReadConnectionSettings() {}

  @Override
  public void onFailedToSetConnectionSettings() {}

  @Override
  public void onReadTemperature(double temperature) {}

  @Override
  public void onFailedToReadTemperature() {}

  @Override
  public void onUpdateFirmware(double progress) {}

  @Override
  public void onFailedToUpdateFirmware(int status) {}

  @Override
  public void onReadDataLog(ArrayList<DataLog> arrayList) {}

  @Override
  public void onEnabledDataLog() {}

  @Override
  public void onFailedToReadDataLog() {}

  @Override
  public void onReadAdvertisementSettings(float disconnectInterval,
                                          float disconnectTimeout,
                                          float energySavingTimeout) {}

  @Override
  public void onSetAdvertisementSettings(float disconnectInterval,
                                         float disconnectTimeout,
                                         float energySavingTimeout) {}

  @Override
  public void onFailedToReadAdvertisementSettings() {}

  @Override
  public void onFailedToSetAdvertisementSettings() {}

  @Override
  public void onSetAccelerometerConfiguration() {}

  @Override
  public void onFailedToSetAccelerometerConfiguration() {}
  /* =========================== */

}
