package tumcm.droneiot.beacon_management.utils.beacon;

import java.util.Comparator;
import java.util.Map;

public class BeaconComparator<K, V extends Comparable<V>> implements Comparator<K> {

  private final Map<K, V> map;

  public BeaconComparator(Map<K, V> map) {
    this.map = map;
  }

  @Override
  public int compare(K s1, K s2) {
    return map.get(s1).compareTo(map.get(s2));
  }

}
