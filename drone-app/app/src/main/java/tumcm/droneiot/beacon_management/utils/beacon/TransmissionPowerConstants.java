package tumcm.droneiot.beacon_management.utils.beacon;

public enum TransmissionPowerConstants {

    One_Meter((byte) -50),
    Three_Meters((byte) -40),
    Twelve_Meters((byte) -20),
    Eighteen_Meters((byte) -16),
    TwentyFive_Meters((byte) -12),
    ThirtyFive_Meters((byte) -8),
    Fourty_Meters((byte) -4),
    Fifty_Meters((byte) 0),
    Sixty_Meters((byte) 4),
    Eighty_Meters((byte) 5);

    private final byte dBm;

    TransmissionPowerConstants(byte dBm) {
        this.dBm = dBm;
    }

    public byte getdBm() {
        return this.dBm;
    }

}
