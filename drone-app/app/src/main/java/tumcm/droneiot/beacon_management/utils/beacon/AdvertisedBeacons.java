package tumcm.droneiot.beacon_management.utils.beacon;

import com.bluvision.beeks.sdk.constants.AdvertisementTypes;

public enum AdvertisedBeacons {

    S_BEACON(AdvertisementTypes.BCN_ADV_TYPE_SBEACON_V2),
    I_BEACON(AdvertisementTypes.BCN_ADV_TYPE_IBEACON),
    URL(AdvertisementTypes.BCN_ADV_TYPE_EDDYSTONE_URL),
    UID(AdvertisementTypes.BCN_ADV_TYPE_EDDYSTONE_UID),
    TLM(AdvertisementTypes.BCN_ADV_TYPE_EDDYSTONE_TLM);

    private final byte type;

    AdvertisedBeacons(byte type) {
        this.type = type;
    }

    public byte getType() {
        return this.type;
    }
}
