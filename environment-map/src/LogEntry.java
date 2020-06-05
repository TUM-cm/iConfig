import java.util.Calendar;
import java.util.Date;

public class LogEntry {
    private int id;
    private Double locationx;
    private Double locationy;
    private int obstacle;
    private int rssi;
    private String beaconmac;
    private Calendar timestamp;

    public LogEntry(){}

    public LogEntry(Double x, Double y){
        this.locationx = x;
        this.locationy = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getLocationx() {
        return locationx;
    }

    public void setLocationx(Double locationx) {
        this.locationx = locationx;
    }

    public Double getLocationy() {
        return locationy;
    }

    public void setLocationy(Double locationy) {
        this.locationy = locationy;
    }

    public int getObstacle() {
        return obstacle;
    }

    public void setObstacle(int obstacle) {
        this.obstacle = obstacle;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getBeaconmac() {
        return beaconmac;
    }

    public void setBeaconmac(String beaconmac) {
        this.beaconmac = beaconmac;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LogEntry [id=" + Integer.toString(id) + ", x=" + Double.toString(locationx) + ", y="
                + Double.toString(locationy) + ", obstacle=" + Integer.toString(obstacle) + ", rssi="
                + Integer.toString(rssi) + ", mac=" + beaconmac + " "
                + "]";
    }

}

