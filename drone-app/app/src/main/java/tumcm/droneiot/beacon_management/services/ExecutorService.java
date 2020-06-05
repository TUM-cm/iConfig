package tumcm.droneiot.beacon_management.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import tumcm.droneiot.beacon_management.utils.general.AppConfig;

public class ExecutorService {

  private final AppConfig appConfig;
  private final ScheduledExecutorService scheduledExecutorService;

  public ExecutorService(AppConfig appConfig) {
    this.appConfig = appConfig;
    int numThreads = getAppConfig().getNumThreadsExecutorService(int.class);
    this.scheduledExecutorService = Executors.newScheduledThreadPool(numThreads);
  }

  private AppConfig getAppConfig() {
    return this.appConfig;
  }

  public ScheduledExecutorService getScheduledExecutorService() {
    return this.scheduledExecutorService;
  }

}
