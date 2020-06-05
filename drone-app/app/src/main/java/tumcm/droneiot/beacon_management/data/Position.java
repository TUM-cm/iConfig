package tumcm.droneiot.beacon_management.data;

import android.location.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Position implements Serializable {

  private static final long serialVersionUID = 5306400250909853163L;

  @SerializedName("latitude")
  @Expose
  private String latitude;

  @SerializedName("longitude")
  @Expose
  private String longitude;

  public String getLatitude() {
    return this.latitude;
  }

  public String getLongitude() {
    return this.longitude;
  }

  public void setLocation(Location location) {
    if (location != null) {
      this.latitude = String.valueOf(location.getLatitude());
      this.longitude = String.valueOf(location.getLongitude());
    }
  }

}