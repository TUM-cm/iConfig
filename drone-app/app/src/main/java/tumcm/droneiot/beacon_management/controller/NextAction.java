package tumcm.droneiot.beacon_management.controller;

import tumcm.droneiot.beacon_management.data.BeaconObject;

public interface NextAction {

  void execute(BeaconObject beacon);

}
