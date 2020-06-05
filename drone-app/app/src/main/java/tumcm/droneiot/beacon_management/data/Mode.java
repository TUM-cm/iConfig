package tumcm.droneiot.beacon_management.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Mode implements Serializable {

  private static final long serialVersionUID = -8411343651316775616L;

  @SerializedName("advertisement rate")
  @Expose
  private float advertisementRate;

  @SerializedName("transmission power")
  @Expose
  private int transmissionPower;

  public float getAdvertisementRate() {
    return this.advertisementRate;
  }

  public byte getTransmissionPower() {
    return (byte) this.transmissionPower;
  }

  public void setAdvertisementRate(float advertisementRate) {
    this.advertisementRate = advertisementRate;
  }

  public void setTransmissionPower(byte transmissionPower) {
    this.transmissionPower = transmissionPower;
  }

}
