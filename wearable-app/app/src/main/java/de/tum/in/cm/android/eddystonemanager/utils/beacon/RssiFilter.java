package de.tum.in.cm.android.eddystonemanager.utils.beacon;

public interface RssiFilter {

  void addMeasurement(Integer rssi);
  double getRssi();

}
