package de.tum.in.cm.android.eddystonemanager.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExtendedPacket extends Packet implements Serializable {

  private static final long serialVersionUID = -3981089838312181735L;

  @SerializedName("connection rates")
  @Expose
  private ConnectionRate connectionRates;

  public ConnectionRate getConnectionRate() {
    return this.connectionRates;
  }

}