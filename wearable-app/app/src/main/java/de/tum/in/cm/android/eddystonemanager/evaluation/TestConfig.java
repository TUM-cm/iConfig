package de.tum.in.cm.android.eddystonemanager.evaluation;

public enum TestConfig {

  MANUAL("manual config"),
  REALISTIC_SETTINGS("realistic settings config"),
  WORST_SETTINGS("worst settings config");

  private final String config;

  TestConfig(String config) {
    this.config = config;
  }

  public String getStr() {
    return this.config;
  }

}
