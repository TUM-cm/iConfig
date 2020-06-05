package de.tum.in.cm.android.eddystonemanager.services;

import android.location.Location;
import android.util.Log;

import com.bluvision.beeks.sdk.domainobjects.Beacon;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.tum.in.cm.android.eddystonemanager.configurator.Action;
import de.tum.in.cm.android.eddystonemanager.data.BeaconConfig;
import de.tum.in.cm.android.eddystonemanager.data.BeaconData;
import de.tum.in.cm.android.eddystonemanager.data.BeaconObject;
import de.tum.in.cm.android.eddystonemanager.data.BeaconUnregistered;
import de.tum.in.cm.android.eddystonemanager.data.BeaconUpdate;
import de.tum.in.cm.android.eddystonemanager.gui.BeaconArrayAdapter;
import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.utils.app.AppConfig;
import de.tum.in.cm.android.eddystonemanager.utils.app.AppStorage;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.EddystoneTlmFrame;
import de.tum.in.cm.android.eddystonemanager.utils.general.FileUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.ObjectSerialization;

public class BeaconDataService {

  private final String TAG = BeaconDataService.class.getSimpleName();
  private final ObjectSerialization beaconToSynchronizeSerialization;
  private final ObjectSerialization beaconDefaultConfigSerialization;
  private List<BeaconData> beaconsToSynchronize;
  private Location currentLocation;
  private float currentAltitude;
  private BeaconConfig defaultBeaconConfig;
  private BeaconConfig manualBeaconConfig;
  private HashSet<String> registeredBeaconsSet;
  private Map<String, BeaconUnregistered> unregisteredBeaconsMap;
  private Map<String, Beacon> registeredBeaconsMap;
  private Map<String, BeaconConfig> beaconConfigsToUpdate;
  private final List<String> beaconsWaitForUpdate;
  private final Map<String, EddystoneTlmFrame> tlmFrames;
  private BeaconObject activeBeacon;
  private final Map<String, BeaconUpdate> availableBeacons;
  private BeaconArrayAdapter beaconArrayAdapter;

  public BeaconDataService() {
    this.unregisteredBeaconsMap = new ConcurrentHashMap<>();
    this.registeredBeaconsMap = new LinkedHashMap<>();
    this.availableBeacons = new ConcurrentHashMap<>();
    this.registeredBeaconsSet = new HashSet<>();
    this.beaconsToSynchronize = new ArrayList<>();
    this.beaconToSynchronizeSerialization = new ObjectSerialization(AppStorage.BEACONS_TO_SYNCHRONIZE_FILENAME);
    this.beaconDefaultConfigSerialization = new ObjectSerialization(AppStorage.DEFAULT_BEACON_CONFIG_FILENAME);
    this.beaconsWaitForUpdate = new LinkedList<>();
    this.tlmFrames = new ConcurrentHashMap<>();
  }

  public void setRegisteredBeacons(HashSet<String> registeredBeacons) {
    this.registeredBeaconsSet = registeredBeacons;
    addBeaconsToRegister();
    for(String mac : getRegisteredBeaconsSet()) {
      if (getUnregisteredBeaconsMap().containsKey(mac)) {
        BeaconUnregistered unregisteredBeacon = getUnregisteredBeaconsMap().get(mac);
        getRegisteredBeaconsMap().put(mac, unregisteredBeacon.getBeacon());
        getUnregisteredBeaconsMap().remove(mac);
      }
    }
  }

  public void addBeacon(Beacon newBeacon) {
    String mac = newBeacon.getDevice().getAddress();
    boolean registered = getRegisteredBeaconsSet().contains(mac);
    if (registered) {
      getRegisteredBeaconsMap().put(mac, newBeacon);
    } else {
      getUnregisteredBeaconsMap().put(mac, new BeaconUnregistered(newBeacon));
    }
  }

  public void addRegisteredBeacon(Beacon newBeacon) {
    String mac = newBeacon.getDevice().getAddress();
    boolean registered = getRegisteredBeaconsMap().containsKey(mac);
    boolean unregistered = getUnregisteredBeaconsMap().containsKey(mac);
    if (!registered && unregistered) {
      getUnregisteredBeaconsMap().remove(mac);
      getRegisteredBeaconsMap().put(newBeacon.getDevice().getAddress(), newBeacon);
    }
  }

  public void setBeaconConfigsToUpdate(BeaconConfig[] beaconConfigsToUpdate) {
    this.beaconConfigsToUpdate = new HashMap<>();
    for (BeaconConfig beaconConfig : beaconConfigsToUpdate) {
      getBeaconConfigsToUpdate().put(beaconConfig.getMac(), beaconConfig);
    }
  }

  public void addRssi(Beacon beacon, String mac, int rssi) {
    BeaconUpdate entry;
    if (getAvailableBeacons().containsKey(mac)) {
      entry = getAvailableBeacons().get(mac);
    } else {
      entry = new BeaconUpdate(beacon);
      getAvailableBeacons().put(mac, entry);
    }
    entry.addRssi(Math.abs(rssi));
  }

  public void loadBeaconsToSynchronize() {
    if (getBeaconToSynchronizeSerialization().checkFileExists()) {
      Object object = getBeaconToSynchronizeSerialization().deserialize();
      if (object != null && object instanceof List<?>) {
        setBeaconsToSynchronize((List<BeaconData>) object);
        addBeaconsToRegister();
      }
    }
  }

  public void loadDefaultBeaconConfig() {
    if (getBeaconDefaultConfigSerialization().checkFileExists()) {
      Object object = getBeaconDefaultConfigSerialization().deserialize();
      if (object != null && object instanceof BeaconConfig) {
        setDefaultBeaconConfig((BeaconConfig) object);
      }
    } else {
      try {
        // Internal fallback
        InputStream is = FileUtils.getInputStream(MainActivity.getDefaultBeaconConfig());
        ObjectInputStream ois = new ObjectInputStream(is);
        Object object = ois.readObject();
        if (object != null && object instanceof BeaconConfig) {
          setDefaultBeaconConfig((BeaconConfig) object);
        }
        ois.close();
        is.close();
      } catch (IOException e) {
        Log.e(TAG, "deserialize Object", e);
      } catch (ClassNotFoundException e) {
        Log.e(TAG, "deserialize Object", e);
      }
    }
  }

  public void setActiveBeacon(BeaconUnregistered beaconUnregistered, BeaconConfig config,
                              Action action, ServiceCallback callback, AppConfig appConfig) {
    this.activeBeacon = new BeaconObject(beaconUnregistered, this,
            callback, config, action, appConfig);
  }

  public void resetActiveBeacon() {
    this.activeBeacon = null;
  }

  private void setBeaconsToSynchronize(List<BeaconData> beaconsToSynchronize) {
    this.beaconsToSynchronize = beaconsToSynchronize;
  }

  public Map<String, BeaconConfig> getBeaconConfigsToUpdate() {
    return this.beaconConfigsToUpdate;
  }

  public Map<String, BeaconUpdate> getAvailableBeacons() {
    return this.availableBeacons;
  }

  private HashSet<String> getRegisteredBeaconsSet() {
    return this.registeredBeaconsSet;
  }

  public List<BeaconUnregistered> getUnregisteredBeaconsAsList() {
    return new ArrayList<>(this.unregisteredBeaconsMap.values());
  }

  public Map<String, BeaconUnregistered> getUnregisteredBeaconsMap() {
    return this.unregisteredBeaconsMap;
  }

  public List<BeaconData> getBeaconsToSynchronize() {
    return this.beaconsToSynchronize;
  }

  public void addBeaconsToSynchronize(BeaconObject beaconObject) {
    BeaconData beaconData = new BeaconData(beaconObject);
    getBeaconsToSynchronize().add(beaconData);
    saveBeaconsToSynchronize();
  }

  public ObjectSerialization getBeaconToSynchronizeSerialization() {
    return this.beaconToSynchronizeSerialization;
  }

  public void saveBeaconsToSynchronize() {
    getBeaconToSynchronizeSerialization().serialize(getBeaconsToSynchronize());
  }

  public ObjectSerialization getBeaconDefaultConfigSerialization() {
    return this.beaconDefaultConfigSerialization;
  }

  public void saveDefaultBeaconConfig(BeaconConfig defaultBeaconConfig) {
    getBeaconDefaultConfigSerialization().serialize(defaultBeaconConfig);
  }

  public void addBeaconsToRegister() {
    for (BeaconData beaconToSynchronize : getBeaconsToSynchronize()) {
      getRegisteredBeaconsSet().add(beaconToSynchronize.getMac());
    }
  }

  public void setCurrentLocation(Location location) {
    this.currentLocation = location;
  }

  public Location getCurrentLocation() {
    return this.currentLocation;
  }

  public void setCurrentAltitude(float altitude) {
    this.currentAltitude = altitude;
  }

  public float getCurrentAltitude() {
    return this.currentAltitude;
  }

  public void setDefaultBeaconConfig(BeaconConfig defaultBeaconConfig) {
    this.defaultBeaconConfig = defaultBeaconConfig;
  }

  public BeaconConfig getDefaultBeaconConfig() {
    if (this.defaultBeaconConfig == null) {
      loadDefaultBeaconConfig();
      this.defaultBeaconConfig.initialize();
    }
    return this.defaultBeaconConfig;
  }

  public BeaconObject getActiveBeacon() {
    return this.activeBeacon;
  }

  public List<String> getBeaconsWaitForUpdate() {
    return this.beaconsWaitForUpdate;
  }

  public Map<String, EddystoneTlmFrame> getTlmFrames() {
    return this.tlmFrames;
  }

  public Map<String, Beacon> getRegisteredBeaconsMap() {
    return this.registeredBeaconsMap;
  }

  public void setManualBeaconConfig(BeaconConfig manualBeaconConfig) {
    this.manualBeaconConfig = manualBeaconConfig;
  }

  public BeaconConfig getManualBeaconConfig() {
    return this.manualBeaconConfig;
  }

  public void setBeaconArrayAdapter(BeaconArrayAdapter beaconArrayAdapter) {
    this.beaconArrayAdapter = beaconArrayAdapter;
  }

  public BeaconArrayAdapter getBeaconArrayAdapter() {
    return this.beaconArrayAdapter;
  }

}
