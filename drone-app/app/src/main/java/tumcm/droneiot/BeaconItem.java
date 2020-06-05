package tumcm.droneiot;

public class BeaconItem {
    public double xPosition;
    public double yPosition;
    public String beaconMacAddress;
    public Integer beaconRssi;

    public BeaconItem(double xPosition, double yPosition, String beaconMacAddress, Integer beaconRssi) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.beaconMacAddress = beaconMacAddress;
        this.beaconRssi = beaconRssi;
    }


}
