package de.tum.in.cm.android.eddystonemanager.controller;

import com.bluvision.beeks.sdk.constants.BeaconType;

public class BeaconConfigController {

  private BeaconType lastConfigType;
  private NextAction nextAction;

  public BeaconType getLastConfigType() {
    return lastConfigType;
  }

  public NextAction getNextAction() {
    return nextAction;
  }

  public void setLastConfigType(BeaconType lastConfigType) {
    this.lastConfigType = lastConfigType;
  }

  public void setNextAction(NextAction nextAction) {
    this.nextAction = nextAction;
  }

}