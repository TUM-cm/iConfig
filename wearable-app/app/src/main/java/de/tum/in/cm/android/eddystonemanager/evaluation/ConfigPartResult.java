package de.tum.in.cm.android.eddystonemanager.evaluation;

import java.io.Serializable;

public class ConfigPartResult implements Serializable {

  private static final long serialVersionUID = 1204730569040068740L;

  private final long duration;
  private final double errorRate;

  public ConfigPartResult(long duration, double errorRate) {
    this.duration = duration;
    this.errorRate = errorRate;
  }

}