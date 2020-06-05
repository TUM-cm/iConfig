package de.tum.in.cm.android.eddystonemanager.utils.beacon;

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