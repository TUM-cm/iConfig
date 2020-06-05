package tumcm.droneiot.beacon_management.utils.beacon;

public class ArmaRssiFilter implements RssiFilter {
  
  private static double ARMA_SPEED = 0.1;
  private int armaMeasurement;
  private boolean initialized;

  public ArmaRssiFilter() {
    setInitialized(false);
  }

  @Override
  public void addMeasurement(Integer rssi) {
    if (!isInitialized()) {
      setArmaMeasurement(rssi);
      setInitialized(true);
    }
    setArmaMeasurement(Double.valueOf(getArmaMeasurement() - ARMA_SPEED *
            (getArmaMeasurement() - rssi)).intValue());
  }

  @Override
  public double getRssi() {
    return getArmaMeasurement();
  }

  private void setArmaMeasurement(int armaMeasurement) {
    this.armaMeasurement = armaMeasurement;
  }

  private int getArmaMeasurement() {
    return this.armaMeasurement;
  }

  private boolean isInitialized() {
    return this.initialized;
  }

  private void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

}
