package de.tum.in.cm.android.eddystonemanager.evaluation;

import org.apache.commons.math3.stat.Frequency;

public class ConfigStatistics {

  private final Frequency result;
  private final Frequency failureRead;
  private final Frequency failureSet;
  private final StringBuilder fieldsFailedToRead;
  private final StringBuilder fieldsFailedToSet;
  private final StringBuilder fieldsUserFailedToSet;
  private boolean failedToRead;
  private boolean failedToSet;
  private boolean userFailedToSet;

  public ConfigStatistics() {
    this.result = new Frequency();
    this.failureRead = new Frequency();
    this.failureSet = new Frequency();
    this.fieldsFailedToRead = new StringBuilder();
    this.fieldsFailedToSet = new StringBuilder();
    this.fieldsUserFailedToSet = new StringBuilder();
    this.failedToRead = false;
    this.failedToSet = false;
    this.userFailedToSet = false;
  }

  private Frequency getResult() {
    return this.result;
  }

  private Frequency getFailureRead() {
    return this.failureRead;
  }

  private Frequency getFailureSet() {
    return this.failureSet;
  }

  private boolean isFailedToRead() {
    return this.failedToRead;
  }

  private boolean isFailedToSet() {
    return this.failedToSet;
  }

  private boolean isUserFailedToSet() {
    return this.userFailedToSet;
  }

  private StringBuilder getFieldsFailedToRead() {
    return this.fieldsFailedToRead;
  }

  private StringBuilder getFieldsFailedToSet() {
    return this.fieldsFailedToSet;
  }

  private StringBuilder getFieldsUserFailedToSet() {
    return this.fieldsUserFailedToSet;
  }

  public void addResult(boolean result, Field field) {
    getResult().addValue(result);
    if (!result) {
      if (isUserFailedToSet()) {
        getFieldsUserFailedToSet().append(", ");
      } else {
        this.userFailedToSet = true;
      }
      getFieldsUserFailedToSet().append(field);
    }
  }

  public void addFailureSet(boolean failureSet, Field field) {
    getFailureSet().addValue(failureSet);
    if (failureSet) {
      if (isFailedToSet()) {
        getFieldsFailedToSet().append(", ");
      } else {
        this.failedToSet = true;
      }
      getFieldsFailedToSet().append(field);
    }
  }

  public void addFailureRead(boolean failureRead, Field field) {
    getFailureRead().addValue(failureRead);
    if (failureRead) {
      if (isFailedToRead()) {
        getFieldsFailedToRead().append(", ");
      } else {
        this.failedToRead = true;
      }
      getFieldsFailedToRead().append(field);
    }
  }

  public double getErrorRate() {
    return getResult().getPct(false);
  }

  public double getSuccessRate() {
    return getResult().getPct(true);
  }

  public double getFailureReadRate() {
    return getFailureRead().getPct(true);
  }

  public double getFailureSetRate() {
    return getFailureSet().getPct(true);
  }

  public String getFieldsFailedToReadStr() {
    return "fields failure read: " + getFieldsFailedToRead().toString();
  }

  public String getFieldsFailedToSetStr() {
    return "fields failure set: " + getFieldsFailedToSet().toString();
  }

  public String getFieldsUserFailedToSetStr() {
    return "fields user failed to set: " +  getFieldsUserFailedToSet().toString();
  }

  public String toStringOverview() {
    StringBuilder str = new StringBuilder("fields: ");
    str.append(getResult().getSumFreq());
    str.append(", resultError: ");
    str.append(getResult().getCount(false));
    str.append(", resultSuccess: ");
    str.append(getResult().getCount(true));
    str.append(", failureRead: ");
    str.append(getFailureRead().getCount(true));
    str.append(", failureSet: ");
    str.append(getFailureSet().getCount(true));
    return str.toString();
  }

  public String toStringDetailed() {
    StringBuilder str = new StringBuilder("error rate: ");
    str.append(getErrorRate());
    str.append(", success rate: ");
    str.append(getSuccessRate());
    str.append( ", failure read rate: ");
    str.append(getFailureReadRate());
    str.append(", failure set rate: ");
    str.append(getFailureSetRate());
    return str.toString();
  }

}
