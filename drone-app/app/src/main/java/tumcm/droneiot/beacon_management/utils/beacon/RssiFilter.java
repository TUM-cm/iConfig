package tumcm.droneiot.beacon_management.utils.beacon;

public interface RssiFilter {

  void addMeasurement(Integer rssi);
  double getRssi();

}
