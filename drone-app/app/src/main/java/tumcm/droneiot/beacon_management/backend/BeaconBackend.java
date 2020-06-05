package tumcm.droneiot.beacon_management.backend;

import java.util.HashSet;
import tumcm.droneiot.beacon_management.data.BeaconConfig;
import tumcm.droneiot.beacon_management.data.Image;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BeaconBackend {

  @GET("list")
  Call<HashSet<String>> getRegisteredBeacons();

  @POST("image")
  Call<Boolean> uploadBeaconImage(@Body Image image);

  @GET("data")
  Call<BeaconConfig[]> getBeaconConfigsToUpdate();

  @POST("data")
  Call<Boolean> sendBeaconConfig(@Body BeaconConfig beaconConfig);

  @GET("config")
  Call<BeaconConfig> getBeaconConfig(@Query("config_type") String configType);

}
