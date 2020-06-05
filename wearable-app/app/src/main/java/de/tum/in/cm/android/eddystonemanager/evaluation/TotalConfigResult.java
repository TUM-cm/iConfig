package de.tum.in.cm.android.eddystonemanager.evaluation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TotalConfigResult implements Serializable {

  private static final long serialVersionUID = -4331353551176512555L;

  private final Map<String, ConfigPartResult> groupResults;
  private long totalConfigDuration;
  private float memoryConfigStart;
  private float memoryConfigEnd;

  public TotalConfigResult() {
    this.groupResults = new HashMap<>();
  }

  public Map<String, ConfigPartResult> getGroups() {
    return this.groupResults;
  }

  public void setTotalConfigDuration(long totalConfigDuration) {
    this.totalConfigDuration = totalConfigDuration;
  }

  public void setMemoryConfigStart(float memoryConfigStart) {
    this.memoryConfigStart = memoryConfigStart;
  }

  public void setMemoryConfigEnd(float memoryConfigEnd) {
    this.memoryConfigEnd = memoryConfigEnd;
  }

}
