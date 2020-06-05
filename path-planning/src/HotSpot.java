import java.util.ArrayList;

public class HotSpot {

    private int devicesInReach;
    private Double locationx1;
    private Double locationy1;
    private ArrayList<Integer> parentDevices;

    public HotSpot(Integer devicesInReach, Double locationX1, Double locationY1) {
        super();
        this.devicesInReach = devicesInReach;
        this.locationx1 = locationX1;
        this.locationy1 = locationY1;
        this.parentDevices = new ArrayList<Integer>();
    }

    public Double getLocationx1() {
        return locationx1;
    }

    public Double getLocationy1() {
        return locationy1;
    }

    public int getDevicesInReach() {
        return devicesInReach;
    }

    public void addParent(int i) {
        if (!parentDevices.contains(i)) {
        parentDevices.add(i);
        }
    }

    public ArrayList<Integer> getParentDevices() {
        return parentDevices;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
