package tumcm.droneiot.beacon_management.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ConnectionRate implements Serializable {

  private static final long serialVersionUID = 9154123252825287636L;

  @SerializedName("connectable rate")
  @Expose
  private int connectableRate;

  @SerializedName("non connectable rate")
  @Expose
  private int nonConnectableRate;

  public byte getConnectableRate() {
    return (byte) this.connectableRate;
  }

  public byte getNonConnectableRate() {
    return (byte) this.nonConnectableRate;
  }

  public void setConnectableRate(byte connectable) {
    this.connectableRate = connectable;
  }

  public void setNonConnectableRate(byte nonConnectable) {
    this.nonConnectableRate = nonConnectable;
  }

}