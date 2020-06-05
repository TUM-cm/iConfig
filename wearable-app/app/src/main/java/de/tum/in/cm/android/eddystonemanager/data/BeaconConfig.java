package de.tum.in.cm.android.eddystonemanager.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import de.tum.in.cm.android.eddystonemanager.utils.general.Crypt;

public class BeaconConfig implements Serializable {

  private static final long serialVersionUID = -7328531742910338655L;

  public BeaconConfig() {
    initialize();
  }

  @SerializedName("mac")
  @Expose
  private String mac;

  @SerializedName("password")
  @Expose
  private String password;

  @SerializedName("new password")
  @Expose
  private String newPassword;

  @SerializedName("connect passwords")
  @Expose(serialize = false)
  private List<String> connectPasswords;

  @SerializedName("nearest room")
  @Expose
  private String nearestRoom;

  @SerializedName("location description")
  @Expose
  private String locationDescription;

  @SerializedName("comments")
  @Expose
  private String comments;

  @SerializedName("position")
  @Expose
  private Position position;

  @SerializedName("altitude")
  @Expose
  private String altitude;

  @SerializedName("sBeacon")
  @Expose
  private SBeaconConfig sBeacon;

  @SerializedName("eddystone")
  @Expose
  private EddystoneConfig eddystone;

  @SerializedName("iBeacon")
  @Expose
  private IBeaconConfig iBeacon;

  @SerializedName("status")
  @Expose
  private Status status;

  // Important: Only send maintenance data, and not receive, otherwise
  // change type to List<Maintenance> because in MongoDB stored as array
  @SerializedName("maintenance")
  @Expose(deserialize = false)
  private Maintenance maintenance;

  public void initialize() {
    if (getStatus() == null) {
      this.status = new Status();
    } if (getMaintenance() == null) {
      this.maintenance = new Maintenance();
    }
  }

  public Status getStatus() {
    return this.status;
  }

  public Maintenance getMaintenance() {
    return this.maintenance;
  }

  public String getMac() {
    return this.mac;
  }

  public void setMac(String mac) {
    this.mac = mac;
  }

  public String getPassword() {
    return this.password;
  }

  public String getNewPassword() {
    return Crypt.getInstance().decrypt(this.newPassword);
  }

  public String getRawNewPassword() {
    return this.newPassword;
  }

  public List<String> getConnectPasswords() {
    return this.connectPasswords;
  }

  public void setNearestRoom(String nearestRoom) {
    this.nearestRoom = nearestRoom;
  }

  public void setLocationDescription(String locationDescription) {
    this.locationDescription = locationDescription;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public void setPassword(String password) {
    this.password = Crypt.getInstance().encrypt(password);
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public void setAltitude(float altitude) {
    this.altitude = String.valueOf(altitude);
  }

  public SBeaconConfig getSBeacon() {
    return this.sBeacon;
  }

  public EddystoneConfig getEddystone() {
    return this.eddystone;
  }

  public IBeaconConfig getIBeacon() {
    return this.iBeacon;
  }

}
