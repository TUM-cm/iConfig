package de.tum.in.cm.android.eddystonemanager.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.tum.in.cm.android.eddystonemanager.utils.app.AppConfig;

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
