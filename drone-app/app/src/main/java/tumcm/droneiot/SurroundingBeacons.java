package tumcm.droneiot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SurroundingBeacons {
    private static SurroundingBeacons instance;
    private ConcurrentHashMap<String, Integer> beaconSurrounding = new ConcurrentHashMap<>();

    private SurroundingBeacons () {
    }

    public static SurroundingBeacons getInstance () {
        if (SurroundingBeacons.instance == null) {
            SurroundingBeacons.instance = new SurroundingBeacons ();
        }
        return SurroundingBeacons.instance;
    }

    public void put(String mac, Integer rssi){
        beaconSurrounding.put(mac,rssi);
    }

    public Integer get(String mac) {
        return beaconSurrounding.get(mac);
    }

    public boolean isEmpty() {
        return beaconSurrounding.isEmpty();
    }

    public Collection values() {
        return beaconSurrounding.values();
    }

    public Set<Map.Entry<String, Integer>> entrySet() {
        return beaconSurrounding.entrySet();
    }

    public void clear() {
        beaconSurrounding.clear();
    }
}