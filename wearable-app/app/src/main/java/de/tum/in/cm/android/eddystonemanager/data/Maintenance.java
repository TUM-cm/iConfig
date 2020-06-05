package de.tum.in.cm.android.eddystonemanager.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Maintenance implements Serializable {

  private static final long serialVersionUID = -3726288847153360500L;

  @SerializedName("battery voltage")
  @Expose
  private float batteryVoltage;

  @SerializedName("firmware")
  @Expose
  private short firmware;

  @SerializedName("packet count")
  @Expose
  private long packetsSent;

  @SerializedName("uptime")
  @Expose
  private long uptime;

  public float getBatteryVoltage() {
    return batteryVoltage;
  }

  public void setBatteryVoltage(float batteryVoltage) {
    this.batteryVoltage = batteryVoltage;
  }

  public short getFirmware() {
    return firmware;
  }

  public void setFirmware(short firmware) {
    this.firmware = firmware;
  }

  public long getPacketsSent() {
    return packetsSent;
  }

  public void setPacketsSent(long packetsSent) {
    this.packetsSent = packetsSent;
  }

  public long getUptime() {
    return uptime;
  }

  public void setUptime(long uptime) {
    this.uptime = uptime;
  }

}
