package de.tum.in.cm.android.eddystonemanager.utils.app;

public class AppConfig {

  private final Config config;

  public AppConfig(Config config) {
    this.config = config;
  }
  
  public <T> T getUpdateBeaconScanTime(Class<T> type) {
    return getConfig().get("UpdateBeaconScanTime", Config.APP_SECTION, type);
  }

  public <T> T getBackendSynchronizationInterval(Class<T> type) {
    return getConfig().get("BackendSynchronizationInterval", Config.APP_SECTION, type);
  }

  public <T> T getNumThreadsExecutorService(Class<T> type) {
    return getConfig().get("ExecutorServiceNumThreads", Config.APP_SECTION, type);
  }

  public <T> T getBeaconConfigWriteAttempts(Class<T> type) {
    return getConfig().get("BeaconConfigWriteAttempts", Config.APP_SECTION, type);
  }

  private Config getConfig() {
    return this.config;
  }

}
