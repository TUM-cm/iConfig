package de.tum.in.cm.android.eddystonemanager.controller;

public class MainController {

  public static final Settings SETTING = Settings.DEMO;
  private static ApplicationController applicationController;

  public static ApplicationController getInstance() {
    if (applicationController == null) {
      if (SETTING.isProduction()) {
        applicationController = new ProductionController();
      } else {
        applicationController = new TestController();
      }
    }
    return applicationController;
  }

}
