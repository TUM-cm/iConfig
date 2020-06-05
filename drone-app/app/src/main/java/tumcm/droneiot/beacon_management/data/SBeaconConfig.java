package tumcm.droneiot.beacon_management.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SBeaconConfig implements Serializable {

  private static final long serialVersionUID = 3144431870319618331L;

  @SerializedName("id")
  @Expose
  private String id;

  @SerializedName("packets")
  @Expose
  private Packet packet;

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Packet getPacket() {
    return this.packet;
  }

}
