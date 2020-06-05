package tumcm.droneiot.beacon_management.data;

import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.domainobjects.ConfigurableBeacon;

import tumcm.droneiot.beacon_management.services.BeaconRegisterService;
import tumcm.droneiot.beacon_management.utils.beacon.ArmaRssiFilter;
import tumcm.droneiot.beacon_management.utils.beacon.MovingAverageRssiFilter;
import tumcm.droneiot.beacon_management.utils.beacon.RssiFilter;
import tumcm.droneiot.beacon_management.utils.general.Config;

public class BeaconUnregistered {

  private final Beacon beacon;
  private final ConfigurableBeacon configurableBeacon;
  private final RssiFilter rssiFilter;
  private final String mac;
  private String sBeaconId;

  public BeaconUnregistered(Beacon beacon) {
    this.beacon = beacon;
    this.configurableBeacon = (ConfigurableBeacon) beacon;
    this.mac = getConfigureable().getDevice().getAddress();
    String rssiFilter = Config.getDefaultConfig().get("RssiFilter",
            Config.APP_SECTION, String.class);
    switch(rssiFilter) {
      case "Arma":
        this.rssiFilter = new ArmaRssiFilter();
        break;
      case "Moving Average":
        this.rssiFilter = new MovingAverageRssiFilter();
        break;
      default:
        this.rssiFilter = new ArmaRssiFilter();
        break;
    }
  }

  public ConfigurableBeacon getConfigureable() {
    return this.configurableBeacon;
  }

  public Beacon getBeacon() {
    return this.beacon;
  }

  public RssiFilter getRssiFilter() {
    return this.rssiFilter;
  }

  public boolean setSBeaconId(String sBeaconId) {
    if (BeaconRegisterService.isValidSBeaconId(sBeaconId)) {
      this.sBeaconId = sBeaconId.toUpperCase();
      return true;
    }
    return false;
  }

  public String getSBeaconId() {
    return this.sBeaconId;
  }

  public String getMac() {
    return this.mac;
  }

}
