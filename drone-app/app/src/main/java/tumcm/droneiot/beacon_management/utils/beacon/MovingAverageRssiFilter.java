package tumcm.droneiot.beacon_management.utils.beacon;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovingAverageRssiFilter implements RssiFilter {

  public static final long EXPIRATION_MILLISECONDS = 10000;
  private double lastRssi;
  private List<Measurement> measurements;

  public MovingAverageRssiFilter() {
    this.measurements = new ArrayList();
  }

  @Override
  public void addMeasurement(Integer rssi) {
    Measurement measurement = new Measurement(rssi, SystemClock.elapsedRealtime());
    getMeasurements().add(measurement);
  }

  @Override
  public double getRssi() {
    refreshMeasurements();
    int size = getMeasurements().size();
    int startIndex = 0;
    int endIndex = size -1;
    if (size > 2) {
      startIndex = size/10+1;
      endIndex = size-size/10-2;
    }
    double sum = 0;
    for (int i = startIndex; i <= endIndex; i++) {
      sum += getMeasurements().get(i).getRssi();
    }
    double value = sum / (endIndex-startIndex+1);
    if (Double.isNaN(value)) {
      return this.lastRssi;
    } else {
      this.lastRssi = value;
    }
    return lastRssi;
  }

  private void refreshMeasurements() {
    List<Measurement> newMeasurements = new ArrayList();
    for(Measurement measurement : getMeasurements()) {
      if ((SystemClock.elapsedRealtime() - measurement.getTimestamp()) < EXPIRATION_MILLISECONDS) {
        newMeasurements.add(measurement);
      }
    }
    this.measurements = newMeasurements;
    Collections.sort(getMeasurements());
  }

  private List<Measurement> getMeasurements() {
    return this.measurements;
  }

  private class Measurement implements Comparable<Measurement> {

    private final Integer rssi;
    private final long timestamp;

    public Measurement(int rssi, long timestamp) {
      this.rssi = rssi;
      this.timestamp = timestamp;
    }

    public Integer getRssi() {
      return this.rssi;
    }

    public long getTimestamp() {
      return this.timestamp;
    }

    @Override
    public int compareTo(Measurement measurement) {
      return getRssi().compareTo(measurement.getRssi());
    }
  }

}
