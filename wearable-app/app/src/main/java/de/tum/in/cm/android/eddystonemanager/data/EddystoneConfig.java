package de.tum.in.cm.android.eddystonemanager.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EddystoneConfig implements Serializable {

  private static final long serialVersionUID = 5276458414482440041L;

  @SerializedName("url")
  @Expose
  private String url;

  @SerializedName("uid")
  @Expose
  private UidConfig uid;

  @SerializedName("packets")
  @Expose
  private EddystonePackets eddystonePackets;

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public EddystonePackets getPackets() {
    return this.eddystonePackets;
  }

  public UidConfig getUid() {
    return this.uid;
  }

  public void setUidConfig(UidConfig uid) {
    this.uid = uid;
  }

}