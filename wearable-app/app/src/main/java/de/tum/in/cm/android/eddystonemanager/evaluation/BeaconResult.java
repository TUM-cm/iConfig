package de.tum.in.cm.android.eddystonemanager.evaluation;

import java.io.Serializable;
import java.util.HashMap;

public class BeaconResult implements Serializable {

  private static final long serialVersionUID = 8308901780008772566L;

  private final String mac;
  private final HashMap<Field, FieldResult> fieldStatus;
  private String sBeaconId;
  private long durationIdentify;
  private long durationRegister;
  private long durationUpdate;
  private long durationVerifyConfig;
  private long durationTotalConfig;
  private long startAutomaticConfig;
  private long endAutomaticConfig;
  private long durationConnect;
  private long durationPassword;
  private long durationDisconnect;
  private long durationEddystoneUrl;
  private long durationEddystoneUid;
  private long durationEddystoneTlm;
  private long durationSBeacon;
  private long durationIBeacon;
  private long durationMaintenance;
  private float memoryUsageConfigStart;
  private float memoryUsageConfigEnd;

  public BeaconResult(String mac) {
    this.mac = mac;
    this.fieldStatus = new HashMap<>();
    for (Field field : Field.values()) {
      this.fieldStatus.put(field, new FieldResult());
    }
  }

  public BeaconResult(BeaconResult configResult) {
    this.mac = configResult.mac;
    this.fieldStatus = new HashMap<>(configResult.fieldStatus);
    this.sBeaconId = configResult.sBeaconId;
    this.durationIdentify = configResult.durationIdentify;
    this.durationRegister = configResult.durationRegister;
    this.durationUpdate = configResult.durationUpdate;
    this.durationVerifyConfig = configResult.durationVerifyConfig;
    this.durationTotalConfig = configResult.durationTotalConfig;
    this.startAutomaticConfig = configResult.startAutomaticConfig;
    this.endAutomaticConfig = configResult.endAutomaticConfig;
    this.durationConnect = configResult.durationConnect;
    this.durationPassword = configResult.durationPassword;
    this.durationDisconnect = configResult.durationDisconnect;
    this.durationEddystoneUrl = configResult.durationEddystoneUrl;
    this.durationEddystoneUid = configResult.durationEddystoneUid;
    this.durationEddystoneTlm = configResult.durationEddystoneTlm;
    this.durationSBeacon = configResult.durationSBeacon;
    this.durationIBeacon = configResult.durationIBeacon;
    this.durationMaintenance = configResult.durationMaintenance;
    this.memoryUsageConfigStart = configResult.memoryUsageConfigStart;
    this.memoryUsageConfigEnd = configResult.memoryUsageConfigEnd;
  }

  public void setDurationIdentify(long durationIdentify) {
    this.durationIdentify = durationIdentify;
  }

  public void setDurationRegister(long durationRegister) {
    this.durationRegister = durationRegister;
  }

  public void setDurationUpdate(long durationUpdate) {
    this.durationUpdate = durationUpdate;
  }

  public void setDurationVerifyConfig(long durationVerifyConfig) {
    this.durationVerifyConfig = durationVerifyConfig;
  }

  public void setStartAutomaticConfig(long startAutomaticConfig) {
    this.startAutomaticConfig = startAutomaticConfig;
  }

  public void setEndAutomaticConfig(long endAutomaticConfig) {
    this.endAutomaticConfig = endAutomaticConfig;
  }

  public void setSBeaconId(String sBeaconId) {
    this.sBeaconId = sBeaconId;
  }

  public FieldResult getField(Field configField) {
    return this.fieldStatus.get(configField);
  }

  public String getMac() {
    return this.mac;
  }

  public void setDurationTotalConfig(long durationTotalConfig) {
    this.durationTotalConfig = durationTotalConfig;
  }

  public long getDurationTotalConfig() {
    return this.durationTotalConfig;
  }

  public void setDurationEddystoneUrl(long durationEddystoneUrl) {
    this.durationEddystoneUrl = durationEddystoneUrl;
  }

  public void setDurationEddystoneUid(long durationEddystoneUid) {
    this.durationEddystoneUid = durationEddystoneUid;
  }

  public void setDurationEddystoneTlm(long durationEddystoneTlm) {
    this.durationEddystoneTlm = durationEddystoneTlm;
  }

  public void setDurationSBeacon(long durationSBeacon) {
    this.durationSBeacon = durationSBeacon;
  }

  public void setDurationIBeacon(long durationIBeacon) {
    this.durationIBeacon = durationIBeacon;
  }

  public void setDurationMaintenance(long durationMaintenance) {
    this.durationMaintenance = durationMaintenance;
  }

  public void setDurationDisconnect(long durationDisconnect) {
    this.durationDisconnect = durationDisconnect;
  }

  public void setDurationPassword(long durationPassword) {
    this.durationPassword = durationPassword;
  }

  public void setMemoryUsageConfigStart(float memoryUsageConfigStart) {
    this.memoryUsageConfigStart = memoryUsageConfigStart;
  }

  public void setMemoryUsageConfigEnd(float memoryUsageConfigEnd) {
    this.memoryUsageConfigEnd = memoryUsageConfigEnd;
  }

  public void setDurationConnect(long durationConnect) {
    this.durationConnect = durationConnect;
  }

  public long getDurationPassword() {
    return durationPassword;
  }

  public long getDurationDisconnect() {
    return durationDisconnect;
  }

  public long getDurationEddystoneUrl() {
    return durationEddystoneUrl;
  }

  public long getDurationEddystoneUid() {
    return durationEddystoneUid;
  }

  public long getDurationEddystoneTlm() {
    return durationEddystoneTlm;
  }

  public long getDurationSBeacon() {
    return durationSBeacon;
  }

  public long getDurationIBeacon() {
    return durationIBeacon;
  }

  public long getDurationMaintenance() {
    return durationMaintenance;
  }

  public long getDurationConnect() {
    return this.durationConnect;
  }

  public float getMemoryUsageConfigStart() {
    return memoryUsageConfigStart;
  }

  public float getMemoryUsageConfigEnd() {
    return memoryUsageConfigEnd;
  }

}
