package tumcm.droneiot.beacon_management.data;

import com.bluvision.beeks.sdk.domainobjects.Beacon;

public class BeaconUpdate implements Comparable<BeaconUpdate> {

  private final Beacon beacon;
  private int rssiSum;

  public BeaconUpdate(Beacon beacon) {
    this.beacon = beacon;
    this.rssiSum = 0;
  }

  public int getRssiSum() {
    return this.rssiSum;
  }

  public void addRssi(int rssi) {
    this.rssiSum += rssi;
  }

  public Beacon getBeacon() {
    return this.beacon;
  }

  @Override
  public int compareTo(BeaconUpdate other) {
    return Integer.compare(getRssiSum(), other.getRssiSum());
  }

}
