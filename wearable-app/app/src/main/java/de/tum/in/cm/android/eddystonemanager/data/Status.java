package de.tum.in.cm.android.eddystonemanager.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Status implements Serializable {

    private static final long serialVersionUID = 4390416756743439256L;

    @SerializedName("iBeacon")
    @Expose
    private boolean iBeacon;

    @SerializedName("Eddystone")
    @Expose
    private boolean eddystone;

    @SerializedName("sBeacon")
    @Expose
    private boolean sBeacon;

    public boolean isIBeaconBroken() {
        return !this.iBeacon;
    }

    public void setIBeacon(boolean iBeacon) {
        this.iBeacon = iBeacon;
    }

    public boolean isEddystoneBroken() {
        return !this.eddystone;
    }

    public void setEddystone(boolean eddystone) {
        this.eddystone = eddystone;
    }

    public boolean isSBeaconBroken() {
        return !this.sBeacon;
    }

    public void setSBeacon(boolean sBeacon) {
        this.sBeacon = sBeacon;
    }

}