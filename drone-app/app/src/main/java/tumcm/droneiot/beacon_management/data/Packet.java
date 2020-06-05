package tumcm.droneiot.beacon_management.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Packet implements Serializable {

  private static final long serialVersionUID = 7292482491021206154L;

  @SerializedName("day mode")
  @Expose
  private Mode dayMode;

  @SerializedName("night mode")
  @Expose
  private Mode nightMode;

  public Mode getDayMode() {
    return this.dayMode;
  }

  public Mode getNightMode() {
    return this.nightMode;
  }

}
