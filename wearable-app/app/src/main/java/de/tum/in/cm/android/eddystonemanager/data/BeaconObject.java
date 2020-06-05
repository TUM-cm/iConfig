package de.tum.in.cm.android.eddystonemanager.data;

import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import com.bluvision.beeks.sdk.domainobjects.ConfigurableBeacon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.tum.in.cm.android.eddystonemanager.configurator.Action;
import de.tum.in.cm.android.eddystonemanager.configurator.BeaconActionListener;
import de.tum.in.cm.android.eddystonemanager.configurator.BeaconActionScalabilityListener;
import de.tum.in.cm.android.eddystonemanager.controller.ApplicationController;
import de.tum.in.cm.android.eddystonemanager.controller.BeaconConfigController;
import de.tum.in.cm.android.eddystonemanager.evaluation.Statistics;
import de.tum.in.cm.android.eddystonemanager.services.BeaconDataService;
import de.tum.in.cm.android.eddystonemanager.services.ServiceCallback;
import de.tum.in.cm.android.eddystonemanager.utils.app.AppConfig;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.EddystoneTlmFrame;
import de.tum.in.cm.android.eddystonemanager.utils.general.Crypt;
import de.tum.in.cm.android.eddystonemanager.utils.general.FileUtils;

public class BeaconObject extends BeaconData {

  private static final String TAG = BeaconData.class.getSimpleName();
  private static final long serialVersionUID = -8470766422624310947L;
  private static final int MAX_LOGON_ATTEMPTS = 3;
  private static final int TLM_WAIT_TIME = 500;
  private static final int MAX_TLM_WAIT_TIME = 3 * TLM_WAIT_TIME;

  private final BeaconDataService beaconDataService;
  private final ConfigurableBeacon configurableBeacon;
  private BeaconActionListener beaconActionListener;
  private BeaconActionScalabilityListener beaconActionScalabilityListener;
  private final AppConfig appConfig;
  private final Crypt crypt;
  private final ServiceCallback callback;
  private final BeaconConfigController configController;
  private final Statistics statistics;
  private int configCounter;
  private int logonCounter;
  private boolean newPasswordAvailable;
  private Action action;
  private String lastPassword;
  private File placeImage;

  public BeaconObject(BeaconUnregistered beaconUnregistered,
                      BeaconDataService beaconDataService,
                      ServiceCallback callback,
                      BeaconConfig config,
                      Action action,
                      AppConfig appConfig) {
    super(beaconUnregistered.getMac(), beaconUnregistered.getSBeaconId(), config);
    if (action == Action.ConfigTest) {
      this.beaconActionScalabilityListener = new BeaconActionScalabilityListener(this);
    } else {
      this.beaconActionListener = new BeaconActionListener(this);
    }
    this.statistics = new Statistics(this);
    this.configController = new BeaconConfigController();
    this.beaconDataService = beaconDataService;
    this.callback = callback;
    this.configurableBeacon = beaconUnregistered.getConfigureable();
    this.action = action;
    this.appConfig = appConfig;
    this.crypt = Crypt.getInstance();
    this.configCounter = 0;
    this.logonCounter = 0;
    this.newPasswordAvailable = false;
  }

  public void setStatus(boolean beaconStatus) {
    Status status = getConfig().getStatus();
    status.setEddystone(beaconStatus);
    status.setIBeacon(beaconStatus);
    status.setSBeacon(beaconStatus);
  }

  public boolean isBroken() {
    return getConfig().getStatus().isEddystoneBroken() &&
            getConfig().getStatus().isIBeaconBroken() &&
            getConfig().getStatus().isSBeaconBroken();
  }

  public void createImage() {
    File file = getPlaceImage();
    if (file != null) {
      Image image = new Image();
      String encodedImage = encodeFileToBase64Binary(file);
      String name = getPlaceImageName();
      image.setFilename(name);
      image.setData(encodedImage);
      setImage(image);
    }
  }

  private String encodeFileToBase64Binary(File file) {
    String encodedfile = null;
    try {
      FileInputStream fileInputStreamReader = new FileInputStream(file);
      byte[] bytes = new byte[(int) file.length()];
      fileInputStreamReader.read(bytes);
      encodedfile = new String(Base64.encode(bytes, 0), "UTF-8");
    } catch (IOException e) {
      Log.d(TAG, "encodeFileToBase64Binary", e);
    }
    return encodedfile;
  }

  private void connectActions(String password) {
    setLastPassword(password);
    getConfigurable().setBeaconConfigurationListener(getBeaconActionListener());
  }

  public void connect(String password) {
    connectActions(password);
    getStatistics().connect(getCrypt().decrypt(password));
  }

  public void connectScalability(String password) {
    getConfigurable().setBeaconConfigurationListener(getBeaconActionScalabilityListener());
    getStatistics().connectScalability(password);
  }

  public void connect() {
    ApplicationController.setRunBeaconConfig(true);
    boolean continueLogon = true;
    String password = getConfig().getPassword();
    if (password == null) {
      List<String> passwords = getConfig().getConnectPasswords();
      String lastPassword = getLastPassword();
      if (lastPassword != null) {
        for (int i = 0; i < passwords.size(); i++) {
          if (passwords.get(i).equals(lastPassword)) {
            int indexPassword = i+1;
            if (indexPassword < passwords.size()) {
              password = passwords.get(indexPassword);
              break;
            } else {
              continueLogon = false;
            }
          }
        }
      } else {
        int index = 0;
        password = passwords.get(index);
      }
    } else {
      if (getLogonCounter() < MAX_LOGON_ATTEMPTS) {
        this.logonCounter++;
      } else {
        continueLogon = false;
        this.logonCounter = 0;
      }
    }
    if (continueLogon) {
      Log.d(TAG, "continueLogon");
      connectActions(password);
      getStatistics().connect(getCrypt().decrypt(password));
    } else {
      if (getAction() == Action.Identify) {
        getStatistics().endIdentify();
      } else if (getAction() == Action.Update) {
        getStatistics().endUpdate(false);
      } else if (getAction() == Action.Register) {
        getStatistics().endRegister(false);
      } else if (getAction() == Action.VerifyConfig) {
        getStatistics().endVerifyConfig(false);
      }
      setLastPassword(null);
    }
  }

  public boolean nextWrite() {
    if (getConfigCounter() < getAppConfig().getBeaconConfigWriteAttempts(int.class)) {
      this.configCounter++;
      return true;
    } else {
      this.configCounter = 0;
      return false;
    }
  }

  public String getPlaceImageName() {
    String fileExtension = FileUtils.getFileExtension(getPlaceImage().getName());
    String filename = getMac().replaceAll(":", "");
    return filename + fileExtension;
  }

  public void setNewPasswordAvailable() {
    String newPassword = getConfig().getNewPassword();
    if (newPassword != null && newPassword.length() > 0) {
      this.newPasswordAvailable = true;
    } else {
      this.newPasswordAvailable = false;
    }
  }

  public void setMaintenanceTlm() {
    boolean found = false;
    Maintenance maintenance = getConfig().getMaintenance();
    EddystoneTlmFrame tlmFrame = null;
    int waitTime = 0;
    while(!found && waitTime < MAX_TLM_WAIT_TIME) {
      Map<String, EddystoneTlmFrame> tlmFrames = getBeaconDataService().getTlmFrames();
      if (tlmFrames.containsKey(getMac())) {
        tlmFrame = tlmFrames.get(getMac());
        found = true;
      } else {
        waitTime += TLM_WAIT_TIME;
        SystemClock.sleep(TLM_WAIT_TIME);
      }
    }
    if (tlmFrame != null) {
      maintenance.setUptime(tlmFrame.getUptime());
      maintenance.setPacketsSent(tlmFrame.getAdvertisedPackets());
      getStatistics().logOnMaintenanceTlmResult(true);
    } else {
      getStatistics().logOnMaintenanceTlmResult(false);
    }
  }

  public void setMaintenanceTlmScalabilityTest() {
    boolean found = false;
    EddystoneTlmFrame tlmFrame = null;
    int waitTime = 0;
    while(!found && waitTime < MAX_TLM_WAIT_TIME) {
      Map<String, EddystoneTlmFrame> tlmFrames = getBeaconDataService().getTlmFrames();
      if (tlmFrames.containsKey(getMac())) {
        tlmFrame = tlmFrames.get(getMac());
        found = true;
      } else {
        waitTime += TLM_WAIT_TIME;
        SystemClock.sleep(TLM_WAIT_TIME);
      }
    }
    if (tlmFrame != null) {
      getStatistics().getConfigResult().addValue(true);
    } else {
      getStatistics().getConfigResult().addValue(false);
    }
  }

  public BeaconConfigController getConfigController() {
    return this.configController;
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  public ConfigurableBeacon getConfigurable() {
    return this.configurableBeacon;
  }

  public Action getAction() {
    return this.action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public BeaconActionListener getBeaconActionListener() {
    return this.beaconActionListener;
  }

  public BeaconActionScalabilityListener getBeaconActionScalabilityListener() {
    return this.beaconActionScalabilityListener;
  }

  public ServiceCallback getCallback() {
    return this.callback;
  }

  private AppConfig getAppConfig() {
    return this.appConfig;
  }

  public void setLastPassword(String lastPassword) {
    this.lastPassword = lastPassword;
  }

  public String getLastPassword() {
    return this.lastPassword;
  }

  public Statistics getStatistics() {
    return this.statistics;
  }

  public void setPlaceImage(File placeImage) {
    this.placeImage = placeImage;
  }

  public File getPlaceImage() {
    return this.placeImage;
  }

  public boolean isNewPasswordAvailable() {
    return this.newPasswordAvailable;
  }

  private int getLogonCounter() {
    return this.logonCounter;
  }

  public int getConfigCounter() {
    return this.configCounter;
  }

  private Crypt getCrypt() {
    return this.crypt;
  }

}
