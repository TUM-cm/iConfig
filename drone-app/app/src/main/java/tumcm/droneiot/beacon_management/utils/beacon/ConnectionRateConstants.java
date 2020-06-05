package tumcm.droneiot.beacon_management.utils.beacon;

public enum ConnectionRateConstants {

    Default((byte) 1);

    private final byte frequency;

    ConnectionRateConstants(byte frequency) {
        this.frequency = frequency;
    }

    public byte getFrequency() {
        return this.frequency;
    }

}