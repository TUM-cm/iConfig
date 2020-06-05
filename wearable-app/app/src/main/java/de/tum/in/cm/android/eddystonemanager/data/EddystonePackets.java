package de.tum.in.cm.android.eddystonemanager.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EddystonePackets implements Serializable {

  private static final long serialVersionUID = 2407782948440179109L;

  @SerializedName("eddystone TLM")
  @Expose
  private Packet eddystoneTlm;

  @SerializedName("eddystone URL")
  @Expose
  private ExtendedPacket eddystoneUrl;

  @SerializedName("eddystone UID")
  @Expose
  private ExtendedPacket eddystoneUid;

  public Packet getTlm() {
    return this.eddystoneTlm;
  }

  public ExtendedPacket getUrl() {
    return this.eddystoneUrl;
  }

  public ExtendedPacket getUid() {
    return this.eddystoneUid;
  }

}