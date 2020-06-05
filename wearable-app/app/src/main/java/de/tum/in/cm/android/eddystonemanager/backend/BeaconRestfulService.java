package de.tum.in.cm.android.eddystonemanager.backend;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashSet;

import de.tum.in.cm.android.eddystonemanager.controller.MainController;
import de.tum.in.cm.android.eddystonemanager.data.BeaconConfig;
import de.tum.in.cm.android.eddystonemanager.data.Image;
import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.utils.app.Config;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BeaconRestfulService {

  private static final Config CONFIG = Config.getInstance(MainActivity.getConfigResource());
  private static final boolean ENABLE_SSL = true;
  private static final String TAG = BeaconRestfulService.class.getSimpleName();

  private final BeaconBackend beaconBackend;

  public BeaconRestfulService() {
    Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .create();
    if (MainController.SETTING.isDemo()) {
      this.beaconBackend = createStandardBeaconBackend(gson);
    } else {
      this.beaconBackend = createSecureBeaconBackend(gson);
    }
  }

  private String createBeaconBaseUrl() {
    StringBuilder uriBuilder = new StringBuilder();
    uriBuilder.append(getValue("Scheme"));
    uriBuilder.append("://");
    uriBuilder.append(getValue("Host"));
    uriBuilder.append(":");
    uriBuilder.append(getValue("Port"));
    uriBuilder.append(getValue("ServicePath"));
    return uriBuilder.toString();
  }

  private BeaconBackend createStandardBeaconBackend(Gson gson) {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(createBeaconBaseUrl())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    return retrofit.create(BeaconBackend.class);
  }

  private BeaconBackend createSecureBeaconBackend(Gson gson) {
    Retrofit.Builder builder = ServiceGenerator.createBuilder(
            createBeaconBaseUrl(),
            GsonConverterFactory.create(gson));
    return ServiceGenerator.createService(BeaconBackend.class,
            builder,
            getValue("Username"),
            getValue("Password"),
            ENABLE_SSL);
  }

  public static boolean isAvailable() {
    String address = getValue("Host");
    int port = Integer.parseInt(getValue("Port"));
    Socket socket = new Socket();
    SocketAddress socketAddress = new InetSocketAddress(address, port);
    try {
      int timeout = 500;
      socket.connect(socketAddress, timeout);
    } catch (IOException e) {
      return false;
    } finally {
      if (socket.isConnected()) {
        try {
          socket.close();
          return true;
        } catch (IOException e) {
          return false;
        }
      }
    }
    return false;
  }

  //==========================================================================//
  // API
  //==========================================================================//
  public HashSet<String> getRegisteredBeacons() {
    try {
      Call<HashSet<String>> call = getBeaconBackend().getRegisteredBeacons();
      Response<HashSet<String>> response = call.execute();
      if (response.isSuccessful()) {
        return response.body();
      } else {
        return null;
      }
    } catch (IOException e) {
      Log.e(TAG, "Failure get registered beacons", e);
    }
    return null;
  }

  public BeaconConfig[] getBeaconConfigsToUpdate() {
    try {
      Call<BeaconConfig[]> call = getBeaconBackend().getBeaconConfigsToUpdate();
      Response<BeaconConfig[]> response = call.execute();
      if (response.isSuccessful()) {
        return response.body();
      } else {
        return null;
      }
    } catch (IOException e) {
      Log.e(TAG, "Failure get registered beacons", e);
    }
    return null;
  }

  public boolean uploadBeaconImage(Image image) {
    try {
      Call<Boolean> call = getBeaconBackend().uploadBeaconImage(image);
      Response<Boolean> response = call.execute();
      if (response.isSuccessful()) {
        return response.body();
      } else {
        return false;
      }
    } catch (IOException e) {
      Log.e(TAG, "Failure when register beacon", e);
    }
    return false;
  }

  public boolean sendBeaconConfig(BeaconConfig beaconConfig) {
    try {
      Call<Boolean> call = getBeaconBackend().sendBeaconConfig(beaconConfig);
      Response<Boolean> response = call.execute();
      if (response.isSuccessful()) {
        return response.body();
      } else {
        return false;
      }
    } catch (IOException e) {
      Log.e(TAG, "Failure when register beacon", e);
    }
    return false;
  }

  public BeaconConfig getBeaconConfig(String configType) {
    try {
      Call<BeaconConfig> call = getBeaconBackend().getBeaconConfig(configType);
      Response<BeaconConfig> response = call.execute();
      if (response.isSuccessful()) {
        return response.body();
      } else {
        return null;
      }
    } catch (IOException e) {
      Log.e(TAG, "Failure get test beacon config", e);
    }
    return null;
  }

  private BeaconBackend getBeaconBackend() {
    return this.beaconBackend;
  }

  private static String getValue(String key) {
    return getConfig().get(key, Config.REST_BACKEND_SECTION, String.class);
  }

  private static Config getConfig() {
    return CONFIG;
  }

}