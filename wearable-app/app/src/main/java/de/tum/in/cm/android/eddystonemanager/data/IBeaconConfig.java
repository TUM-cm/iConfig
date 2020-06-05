package de.tum.in.cm.android.eddystonemanager.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

public class IBeaconConfig implements Serializable {

  private static final long serialVersionUID = -2366143939607545752L;

  @SerializedName("uuid")
  @Expose
  private String uuid;

  @SerializedName("major")
  @Expose
  private String major;

  @SerializedName("minor")
  @Expose
  private String minor;

  @SerializedName("packets")
  @Expose
  private Packet packet;

  public UUID getUuid() {
    if (this.uuid != null) {
      return UUID.fromString(this.uuid);
    } else {
      return null;
    }
  }

  public void setUuid(UUID uuid) {
    if (uuid != null) {
      this.uuid = uuid.toString();
    } else {
      this.uuid = null;
    }
  }

  public int getMajor() {
    return Integer.parseInt(this.major);
  }

  public void setMajor(int major) {
    this.major = String.valueOf(major);
  }

  public int getMinor() {
    return Integer.parseInt(this.minor);
  }

  public void setMinor(int minor) {
    this.minor = String.valueOf(minor);
  }

  public Packet getPacket() {
    return this.packet;
  }

}