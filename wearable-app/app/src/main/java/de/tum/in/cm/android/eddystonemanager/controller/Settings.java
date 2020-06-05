package de.tum.in.cm.android.eddystonemanager.controller;

import de.tum.in.cm.android.eddystonemanager.utils.app.SystemPlatform;

public enum Settings {

  PRODUCTION(true, false, true, false),
  USER_STUDY(true, true, false, false),
  TEST(false, false, true, false),
  DEMO(true, false, true, true);

  private final boolean production;
  private final boolean userStudy;
  private final boolean disableConfigVerify;
  private final boolean demo;

  private static final Rssi rssi;
  private static boolean automaticVoiceControl;

  Settings(boolean production, boolean userStudy,
           boolean disableConfigVerify, boolean demo) {
    this.production = production;
    this.userStudy = userStudy;
    this.disableConfigVerify = disableConfigVerify;
    this.demo = demo;
  }

  public boolean isProduction() {
    return this.production;
  }

  public boolean isUserStudy() {
    return this.userStudy;
  }

  public boolean isTest() {
    return !this.production;
  }

  public boolean isDisableConfigVerify() {
    return this.disableConfigVerify;
  }

  public boolean isAutomaticVoiceControl() {
    return automaticVoiceControl;
  }

  public boolean isDemo() {
    return this.demo;
  }

  public void disableAutomaticVoiceControl() {
    automaticVoiceControl = false;
  }

  public Rssi getRssi() {
    return rssi;
  }

  static {
    rssi = Rssi.Arma;
    automaticVoiceControl = SystemPlatform.isWearable();
  }

}