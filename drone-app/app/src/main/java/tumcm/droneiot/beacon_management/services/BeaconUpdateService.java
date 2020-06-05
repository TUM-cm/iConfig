package tumcm.droneiot.beacon_management.services;

import android.util.Log;

import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import tumcm.droneiot.beacon_management.configurator.Action;
import tumcm.droneiot.beacon_management.controller.ApplicationController;
import tumcm.droneiot.beacon_management.data.BeaconConfig;
import tumcm.droneiot.beacon_management.data.BeaconObject;
import tumcm.droneiot.beacon_management.data.BeaconUnregistered;
import tumcm.droneiot.beacon_management.data.BeaconUpdate;
import tumcm.droneiot.beacon_management.utils.general.AppConfig;
import tumcm.droneiot.beacon_management.utils.beacon.BeaconComparator;

public class BeaconUpdateService {

  private static final String TAG = BeaconUpdateService.class.getSimpleName();

  private final ExecutorService executorService;
  private final BeaconDataService beaconDataService;
  private final AppConfig appConfig;
  private final ServiceCallback serviceCallback;
  private final Deque<BeaconObject> configQueue;
  private ScheduledFuture<?> repeatHandle;
  private int scanTime;

  private final Comparator<String> comparator;
  private final Map<String, BeaconUpdate> sortedBeaconsForUpdate;

  public BeaconUpdateService(ExecutorService executorService,
                             BeaconDataService beaconDataService,
                             ServiceCallback serviceCallback,
                             AppConfig appConfig,
                             Deque<BeaconObject> configQueue) {
    this.executorService = executorService;
    this.beaconDataService = beaconDataService;
    this.appConfig = appConfig;
    this.serviceCallback = serviceCallback;
    this.configQueue = configQueue;
    this.comparator = new BeaconComparator<>(getBeaconDataService().getAvailableBeacons());
    this.sortedBeaconsForUpdate = new TreeMap<>(getComparator());
  }

  public void start(int scanTime) {
    int defaultScanTime = getAppConfig().getUpdateBeaconScanTime(int.class);
    if (scanTime < defaultScanTime) {
      setScanTime(defaultScanTime);
    } else {
      setScanTime(scanTime);
    }
    repeatHandle = getExecutorService().scheduleAtFixedRate(new Runnable() {
      public void run() {
        Log.d(TAG, "Run Beacon Update");
        for(BeaconObject beacon : getConfigQueue()) {
          if (beacon.getAction() == Action.Update) {
            getConfigQueue().remove(beacon);
          }
        }
        getSortedBeaconsForUpdate().clear();
        getSortedBeaconsForUpdate().putAll(getBeaconDataService().getAvailableBeacons());
        getBeaconDataService().getAvailableBeacons().clear();
        if(getBeaconDataService().getBeaconConfigsToUpdate().size() > 0) {
          for (Map.Entry<String, BeaconUpdate> entry : getBeaconDataService().getAvailableBeacons().entrySet()) {
            String mac = entry.getKey();
            BeaconUpdate beaconUpdate = entry.getValue();
            if (getBeaconDataService().getBeaconConfigsToUpdate().containsKey(mac) &&
                    !getBeaconDataService().getBeaconsWaitForUpdate().contains(mac)) {
              getBeaconDataService().getBeaconsWaitForUpdate().add(mac);
              BeaconConfig beaconConfig = getBeaconDataService().getBeaconConfigsToUpdate().get(mac);
              BeaconUnregistered beaconUnregistered = new BeaconUnregistered(beaconUpdate.getBeacon());
              final BeaconObject beaconToConfigure = new BeaconObject(beaconUnregistered,
                      getBeaconDataService(), getServiceCallback(), beaconConfig,
                      Action.Update, getAppConfig());
              if (getConfigQueue().size() == 0 && !ApplicationController.isRunBeaconConfig()) {
                new Thread(new Runnable() {
                  @Override
                  public void run() {
                    beaconToConfigure.connect();
                  }
                }).start();
              } else {
                getConfigQueue().addLast(beaconToConfigure);
              }
            }
          }
        }
      }
    }, 0, getScanTime(), TimeUnit.SECONDS);
  }

  public void stop() {
    if (getRepeatHandle() != null) {
      getRepeatHandle().cancel(true);
    }
  }

  private int getScanTime() {
    return this.scanTime;
  }

  private void setScanTime(int scanTime) {
    this.scanTime = scanTime;
  }

  private ScheduledExecutorService getExecutorService() {
    return this.executorService.getScheduledExecutorService();
  }

  private ScheduledFuture<?> getRepeatHandle() {
    return this.repeatHandle;
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private AppConfig getAppConfig() {
    return this.appConfig;
  }

  private Deque<BeaconObject> getConfigQueue() {
    return this.configQueue;
  }

  private Comparator<String> getComparator() {
    return this.comparator;
  }

  private Map<String, BeaconUpdate> getSortedBeaconsForUpdate() {
    return this.sortedBeaconsForUpdate;
  }

  private ServiceCallback getServiceCallback() {
    return this.serviceCallback;
  }

}
