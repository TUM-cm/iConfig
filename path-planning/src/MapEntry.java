public class MapEntry {
    private int classCategory;
    private Double locationx1;
    private Double locationy1;
    private Double locationx2;
    private Double locationy2;
    private String beaconmac;
    private Double range;

    public MapEntry(Integer classCategory, Double locationX1, Double locationY1, Double locationX2, Double locationY2, String macAddress, Double range) {
        super();
        this.classCategory = classCategory;
        this.locationx1 = locationX1;
        this.locationy1 = locationY1;
        this.locationx2 = locationX2;
        this.locationy2 = locationY2;
        this.beaconmac = macAddress;
        this.range = range;
    }

    public Double getRange() {
        return range;
    }

    public int getClassCategory() {
        return classCategory;
    }

    public void setClassCategory(int category) {
        this.classCategory = category;
    }

    public Double getLocationx1() {
        return locationx1;
    }

    public void setLocationx1(Double locationx1) {
        this.locationx1 = locationx1;
    }

    public Double getLocationy1() {
        return locationy1;
    }

    public void setLocationy1(Double locationy1) {
        this.locationy1 = locationy1;
    }

    public Double getLocationx2() {
        return locationx2;
    }

    public void setLocationx2(Double locationx2) {
        this.locationx2 = locationx2;
    }

    public Double getLocationy2() {
        return locationy2;
    }

    public void setLocationy2(Double locationy2) {
        this.locationy2 = locationy2;
    }

    public String getBeaconmac() {
        return beaconmac;
    }

    public void setBeaconmac(String beaconmac) {
        this.beaconmac = beaconmac;
    }


}
