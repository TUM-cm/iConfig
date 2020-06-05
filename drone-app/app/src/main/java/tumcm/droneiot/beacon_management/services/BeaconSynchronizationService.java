package tumcm.droneiot.beacon_management.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import tumcm.droneiot.beacon_management.backend.BeaconRestfulService;
import tumcm.droneiot.beacon_management.data.BeaconConfig;
import tumcm.droneiot.beacon_management.data.BeaconData;
import tumcm.droneiot.beacon_management.utils.general.AppConfig;

public class BeaconSynchronizationService {

  private final ExecutorService executorService;
  private final BeaconDataService beaconDataService;
  private final BeaconRestfulService beaconRestfulService;
  private final AppConfig appConfig;
  private final TimeUnit timeUnit;
  private ScheduledFuture<?> repeatHandle;

  public BeaconSynchronizationService(ExecutorService executorService,
                                      BeaconDataService beaconDataService,
                                      BeaconRestfulService beaconRestfulService,
                                      AppConfig appConfig) {
    this.executorService = executorService;
    this.beaconDataService = beaconDataService;
    this.beaconRestfulService = beaconRestfulService;
    this.appConfig = appConfig;
    this.timeUnit = TimeUnit.SECONDS;
  }

  public void start() {
    repeatHandle = getExecutorService().scheduleAtFixedRate(new Runnable() {
      public void run() {
        if (BeaconRestfulService.isAvailable()) {
          sendBeaconData();
          requestBeaconData();
        }
      }
    }, 0, getAppConfig().getBackendSynchronizationInterval(int.class), getTimeUnit());
  }

  private void sendBeaconData() {
    Iterator<BeaconData> it = getBeaconDataService().getBeaconsToSynchronize().iterator();
    while(it.hasNext()) {
      BeaconData beacon = it.next();
      getBeaconRestfulService().sendBeaconConfig(beacon.getConfig());
      if (beacon.getImage() != null) {
        getBeaconRestfulService().uploadBeaconImage(beacon.getImage());
      }
      it.remove();
    }
    getBeaconDataService().saveBeaconsToSynchronize();
  }

  private void requestBeaconData() {
    BeaconConfig[] beaconConfigsToUpdate = getBeaconRestfulService().getBeaconConfigsToUpdate();
    if (beaconConfigsToUpdate != null) {
      getBeaconDataService().setBeaconConfigsToUpdate(beaconConfigsToUpdate);
    }
    BeaconConfig defaultBeaconConfig = getBeaconRestfulService().getBeaconConfig("default config");
    if (defaultBeaconConfig != null) {
      // Save as backup for offline mode, every time when connection available to have latest version
      getBeaconDataService().saveDefaultBeaconConfig(defaultBeaconConfig);
      getBeaconDataService().setDefaultBeaconConfig(defaultBeaconConfig);
    }
    HashSet<String> registeredBeacons = getBeaconRestfulService().getRegisteredBeacons();
    if (registeredBeacons != null) {
      getBeaconDataService().setRegisteredBeacons(registeredBeacons);
    }
  }

  public void stop() {
    if (getRepeatHandle() != null) {
      getRepeatHandle().cancel(true);
    }
  }

  private ScheduledFuture<?> getRepeatHandle() {
    return this.repeatHandle;
  }

  private ScheduledExecutorService getExecutorService() {
    return this.executorService.getScheduledExecutorService();
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private BeaconRestfulService getBeaconRestfulService() {
    return this.beaconRestfulService;
  }

  private AppConfig getAppConfig() {
    return this.appConfig;
  }

  private TimeUnit getTimeUnit() {
    return this.timeUnit;
  }

}
