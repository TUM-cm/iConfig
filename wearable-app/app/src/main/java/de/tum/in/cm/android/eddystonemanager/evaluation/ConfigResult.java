package de.tum.in.cm.android.eddystonemanager.evaluation;

import java.io.Serializable;

public class ConfigResult implements Serializable {

  private static final long serialVersionUID = 9044127110130146966L;

  private final long durationTotalConfig;
  private final double connectSuccess;
  private final double configSuccessRate;

  public ConfigResult(long durationTotalConfig, double connectSuccess, double configSuccessRate) {
    this.durationTotalConfig = durationTotalConfig;
    this.connectSuccess = connectSuccess;
    this.configSuccessRate = configSuccessRate;
  }

}
