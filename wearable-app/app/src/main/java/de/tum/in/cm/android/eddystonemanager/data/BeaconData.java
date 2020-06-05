package de.tum.in.cm.android.eddystonemanager.data;

import java.io.Serializable;

public class BeaconData implements Serializable {

  private static final long serialVersionUID = -1243824393744216814L;

  private final String mac;
  private final String sBeaconId;
  private BeaconConfig config;
  private Image image;

  public BeaconData(String mac, String sBeaconId, BeaconConfig config) {
    this.mac = mac;
    this.sBeaconId = sBeaconId;
    this.config = config;
  }

  public BeaconData(BeaconObject beaconObject) {
    this.mac = beaconObject.getMac();
    this.sBeaconId = beaconObject.getSBeaconId();
    this.config = beaconObject.getConfig();
    this.image = beaconObject.getImage();
  }

  public String getSBeaconId() {
    return this.sBeaconId;
  }

  public BeaconConfig getConfig() {
    return this.config;
  }

  public void setConfig(BeaconConfig config) {
    this.config = config;
  }

  public String getMac() {
    return this.mac;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public Image getImage() {
    return this.image;
  }

}
