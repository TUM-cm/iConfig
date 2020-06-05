package de.tum.in.cm.android.eddystonemanager.evaluation;

import java.io.Serializable;

public class FieldResult implements Serializable {

  private static final long serialVersionUID = 426467154529848428L;

  private boolean result;
  private boolean failureRead;
  private boolean failureSet;

  public void setResult(boolean result) {
    this.result = result;
  }

  public void setFailureRead(boolean failureRead) {
    this.failureRead = failureRead;
  }

  public void setFailureSet(boolean failureSet) {
    this.failureSet = failureSet;
  }

  public boolean isResult() {
    return result;
  }

  public boolean isFailureRead() {
    return failureRead;
  }

  public boolean isFailureSet() {
    return failureSet;
  }

}
