package de.tum.in.cm.android.eddystonemanager.data;

import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.domainobjects.ConfigurableBeacon;

import de.tum.in.cm.android.eddystonemanager.controller.MainController;
import de.tum.in.cm.android.eddystonemanager.services.BeaconRegisterService;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.ArmaRssiFilter;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.MovingAverageRssiFilter;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.RssiFilter;

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
    switch (MainController.SETTING.getRssi()) {
      case Arma:
        this.rssiFilter = new ArmaRssiFilter();
        break;
      case MovingAverage:
        this.rssiFilter = new MovingAverageRssiFilter();
        break;
      default:
        this.rssiFilter = new MovingAverageRssiFilter();
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
