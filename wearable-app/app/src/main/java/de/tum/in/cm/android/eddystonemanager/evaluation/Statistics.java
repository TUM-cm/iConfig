package de.tum.in.cm.android.eddystonemanager.evaluation;

import android.os.SystemClock;
import android.util.Log;

import org.apache.commons.math3.stat.Frequency;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.cm.android.eddystonemanager.configurator.Action;
import de.tum.in.cm.android.eddystonemanager.configurator.BeaconActionListener;
import de.tum.in.cm.android.eddystonemanager.controller.MainController;
import de.tum.in.cm.android.eddystonemanager.controller.TestController;
import de.tum.in.cm.android.eddystonemanager.data.BeaconObject;
import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.utils.app.AppStorage;
import de.tum.in.cm.android.eddystonemanager.utils.general.ObjectSerialization;
import de.tum.in.cm.android.eddystonemanager.utils.general.TestSuite;

import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getBeaconObjects;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getBeaconResultList;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getConfigPartsOutput;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getConfigWorkers;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getConnectStatistics;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getMemoryConfigEndOutput;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getMemoryConfigStartOutput;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getSerializationConfigParts;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.getSerializationConnect;
import static de.tum.in.cm.android.eddystonemanager.controller.TestController.writeConfigResult;

public class Statistics {

  private final static String TAG = Statistics.class.getSimpleName();

  private final BeaconObject beacon;
  private Output output;
  private final BeaconResult beaconResult;
  private final StopWatch totalConfigWatch;
  private final StopWatch connectWatch;
  private final StopWatch disconnectWatch;
  private final StopWatch passwordWatch;
  private final StopWatch verifyConfigWatch;
  private final StopWatch identifyWatch;
  private final StopWatch registerWatch;
  private final StopWatch updateWatch;
  private final StopWatch eddystoneUrlWatch;
  private final StopWatch eddystoneUidWatch;
  private final StopWatch eddystoneTlmWatch;
  private final StopWatch sBeaconWatch;
  private final StopWatch iBeaconWatch;
  private final StopWatch maintenanceWatch;
  private final ObjectSerialization serializationConfigResult;
  private final Frequency configResult;
  private final Frequency connectResult;

  public Statistics(BeaconObject beacon) {
    this.beacon = beacon;
    this.beaconResult = new BeaconResult(beacon.getMac());
    if (MainController.SETTING.isTest() && TestController.ACTION == Action.ConfigTest) {
      this.output = new Output(beacon.getMac() + "_" + TestSuite.EVALUATION_FILENAME);
    }
    this.connectWatch = new StopWatch("connect");
    this.disconnectWatch = new StopWatch("disconnect");
    this.totalConfigWatch = new StopWatch("total config");
    this.verifyConfigWatch = new StopWatch("verify config");
    this.identifyWatch = new StopWatch("identify");
    this.registerWatch = new StopWatch("register");
    this.updateWatch = new StopWatch("update");
    this.passwordWatch = new StopWatch("password");
    this.eddystoneUrlWatch = new StopWatch("eddystone url");
    this.eddystoneUidWatch = new StopWatch("eddystone uid");
    this.eddystoneTlmWatch = new StopWatch("eddystone tlm");
    this.sBeaconWatch = new StopWatch("s beacon");
    this.iBeaconWatch = new StopWatch("i beacon");
    this.maintenanceWatch  = new StopWatch("maintenance");
    long timestamp = TestSuite.getDateTime().getUnixTimestamp();
    this.serializationConfigResult = new ObjectSerialization(beacon.getMac() + "_" +
            getBeacon().getAction() + "_" +
            timestamp + "_" + TestSuite.SERIALIZATION_FILENAME);
    this.configResult = new Frequency();
    this.connectResult = new Frequency();
  }

  public void connectTest() {
    if (TestController.TEST_ROUND_COUNTER < TestController.TEST_ROUNDS) {
      TestController.TEST_ROUND_COUNTER++;
      SystemClock.sleep(TestController.WAIT_CONNECT);
      getBeacon().connect(TestController.PASSWORD);
    } else {
      String data = "connection attempts: " + getConnectStatistics().getConnectAttempts() +
              ", mean connect time (ms): " + getConnectStatistics().getDuration().getMean() +
              ", min connect time (ms): " + getConnectStatistics().getDuration().getMin() +
              ", max connect time (ms): " + getConnectStatistics().getDuration().getMax() +
              ", std connect time (ms): " + getConnectStatistics().getDuration().getStandardDeviation() +
              ", median connect time (ms): " + getConnectStatistics().getDuration().getPercentile(50) +
              ", connect rate: " + getConnectStatistics().getConnectRate() +
              ", authenticated rate: " + getConnectStatistics().getAuthenticatedRate();
      Log.d(TAG, data);
      getOutput().write(getSeparator("Start Connect Test Statistics"));
      getOutput().write(data);
      getOutput().write(getSeparator("End Connect Test Statistics"));
      getOutput().write(TestController.END_CONNECTION_TEST);
      getOutput().close();
      getSerializationConnect().serialize(getConnectStatistics());
    }
  }

  public void startConfigRound() {
    TestController.TEST_ROUND_COUNTER++;
    for(int i = 0; i < getBeaconObjects().size(); i++) {
      final BeaconObject beacon = getBeaconObjects().get(i);
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          beacon.connectScalability(TestController.PASSWORD);
        }
      });
      if (i == 0) {
        thread.start();
      } else {
        getConfigWorkers().add(thread);
      }
    }
  }

  public void configTest() {
    long durationTotalConfig = getTotalConfigWatch().getElapsedTime();
    double connectSuccessRate = getConnectResult().getPct(true);
    double configSuccessRate = getConfigResult().getPct(true);
    if (Double.isNaN(configSuccessRate)) {
      configSuccessRate = 0.0;
    }
    ConfigResult configResult = new ConfigResult(durationTotalConfig, connectSuccessRate, configSuccessRate);
    getConnectResult().clear();
    getConfigResult().clear();
    TestController.addBeaconToResultMap(getBeacon().getMac(), configResult);
    if (getConfigWorkers().size() > 0) {
      getConfigWorkers().poll().start();
    } else {
      if (TestController.TEST_ROUND_COUNTER < TestController.TEST_ROUNDS) {
        SystemClock.sleep(TestController.WAIT_CONNECT);
        startConfigRound();
      } else {
        writeConfigResult();
      }
    }
  }

  public void preConfigureBeacon() {
    if (getConfigWorkers().size() > 0) {
      getConfigWorkers().poll().start();
    }
  }

  public void configMemoryTest() {
    if (TestController.TEST_ROUND_COUNTER < TestController.TEST_ROUNDS) {
      restartConfigTest();
    } else {
      getMemoryConfigStartOutput().close();
      getMemoryConfigEndOutput().close();
    }
  }

  private void restartConfigTest() {
    SystemClock.sleep(TestController.WAIT_CONNECT);
    TestController.TEST_ROUND_COUNTER++;
    getBeacon().getConfig().setNewPassword(TestController.getRawNewPassword());
    getBeacon().connect(TestController.PASSWORD);
  }

  public void configPartsTest() {
    getBeaconResultList().add(new BeaconResult(getBeaconResult()));
    if (TestController.TEST_ROUND_COUNTER < TestController.TEST_ROUNDS) {
      restartConfigTest();
    } else {
      writeConfigParts(getSeparator("Start Single Config Test"));
      writeConfigParts("mac: " + getBeacon().getMac());
      List<TotalConfigResult> configResults = new ArrayList<>();
      for(int i = 0; i < getBeaconResultList().size(); i++) {
        BeaconResult beaconResult = getBeaconResultList().get(i);
        writeConfigParts("Round: " + i);
        TotalConfigResult configResult = new TotalConfigResult();
        configResult.setTotalConfigDuration(beaconResult.getDurationTotalConfig());
        writeConfigParts("total config time: " + beaconResult.getDurationTotalConfig());
        writeConfigParts("memory");
        configResult.setMemoryConfigStart(beaconResult.getMemoryUsageConfigStart());
        configResult.setMemoryConfigEnd(beaconResult.getMemoryUsageConfigEnd());
        writeConfigParts("start: " + beaconResult.getMemoryUsageConfigStart());
        writeConfigParts("end: " + beaconResult.getMemoryUsageConfigEnd());
        long sumConfigDuration = 0;
        for (Field.Group group : Field.Group.values()) {
          long configDuration = getConfigDuration(beaconResult, group);
          double errorRate;
          List<Field> groupFields = Field.GROUPS.get(group);
          Frequency results = new Frequency();
          for(Field groupField : groupFields) {
            results.addValue(beaconResult.getField(groupField).isResult());
          }
          errorRate = results.getPct(false);
          writeConfigParts("group: " + group.toString() +
                  ", duration: " + configDuration +
                  ", errorRate: " + errorRate);
          sumConfigDuration += configDuration;
          configResult.getGroups().put(group.toString(), new ConfigPartResult(configDuration, errorRate));
        }
        writeConfigParts("sum config duration: " + sumConfigDuration);
        configResults.add(configResult);
        writeConfigParts(getSeparator("----------------------"));
      }
      writeConfigParts(getSeparator("End Single Config Test"));
      getSerializationConfigParts().serialize(configResults);
    }
  }

  private long getConfigDuration(BeaconResult beaconResult, Field.Group group) {
    switch (group) {
      case EDDYSTONE_TLM:
        return beaconResult.getDurationEddystoneTlm();
      case EDDYSTONE_UID:
        return beaconResult.getDurationEddystoneUid();
      case EDDYSTONE_URL:
        return beaconResult.getDurationEddystoneUrl();
      case I_BEACON:
        return beaconResult.getDurationIBeacon();
      case S_BEACON:
        return beaconResult.getDurationSBeacon();
      case MAINTENANCE:
        return beaconResult.getDurationMaintenance();
      case PASSWORD:
        return beaconResult.getDurationPassword();
      case CONNECT:
        return beaconResult.getDurationConnect();
      case DISCONNECT:
        return beaconResult.getDurationDisconnect();
      default:
        return 0;
    }
  }

  public void calculateResult() {
    write(getSeparator("Start Statistics"));
    ConfigStatistics configStatistics = new ConfigStatistics();
    for(Field field : Field.values()) {
      if (MainController.SETTING.isUserStudy()) {
        if (field == Field.MAINTENANCE_DEVICE_STATUS || field == Field.MAINTENANCE_TLM) {
          continue;
        }
      }
      configStatistics.addResult(getBeaconResult().getField(field).isResult(), field);
      configStatistics.addFailureRead(getBeaconResult().getField(field).isFailureRead(), field);
      configStatistics.addFailureSet(getBeaconResult().getField(field).isFailureSet(), field);
    }
    write("connect");
    String connect = "connect time: " + getBeaconResult().getDurationConnect() +
            ", connected & authenticated: " + getBeaconResult().getField(Field.CONNECT).isResult();
    write(connect);
    Log.d(TAG, connect);

    write("config");
    String configOverview = configStatistics.toStringOverview();
    write(configOverview);
    Log.d(TAG, configOverview);
    String configDetailed = configStatistics.toStringDetailed();
    write(configDetailed);
    Log.d(TAG, configDetailed);

    String strFailureRead = configStatistics.getFieldsFailedToReadStr();
    write(strFailureRead);
    Log.d(TAG, strFailureRead);
    String strFailureSet = configStatistics.getFieldsFailedToSetStr();
    write(strFailureSet);
    Log.d(TAG, strFailureSet);
    String strUserFailure = configStatistics.getFieldsUserFailedToSetStr();
    write(strUserFailure);
    Log.d(TAG, strUserFailure);

    write(getSeparator("End Statistics"));
  }

  public void connect(String password) {
    write(getSeparator("Start Connect"));
    Log.d(TAG, "try password: '" + password + "'");
    if (getBeacon().getAction() == Action.ConfigPartsTest) {
      getBeaconResult().setMemoryUsageConfigStart(AppStorage.getAppMemoryUsage());
    } else if (getBeacon().getAction() == Action.MemoryTest) {
      getMemoryConfigStartOutput().writeCsv(AppStorage.getAppMemoryUsage());
    }
    getTotalConfigWatch().start();
    getConnectWatch().start();
    getBeacon().getConfigurable().connect(MainActivity.getInstance(), password);
  }

  public void startVerifyConfig() {
    write(getSeparator("Start Verify Config"));
    getVerifyConfigWatch().start();
    getBeaconActionListener().verifyBeaconConfig(getBeacon());
  }

  public void endVerifyConfig(boolean result) {
    getVerifyConfigWatch().stop();
    getBeaconResult().setDurationVerifyConfig(getVerifyConfigWatch().getElapsedTime());
    write(getSeparator("End Check Config"));
    save();
    getBeacon().getCallback().verifyConfigAction(result);
  }

  public void startIdentify() {
    write(getSeparator("Start Identify"));
    getIdentifyWatch().start();
    long timestamp = TestSuite.getDateTime().getUnixTimestamp();
    write("start automatic config: " + timestamp);
    getBeaconResult().setStartAutomaticConfig(timestamp);
    getBeaconActionListener().identifyBeacon(getBeacon());
  }

  public void endIdentify() {
    getIdentifyWatch().stop();
    write(getSeparator("End Identify"));
    writeIdentifier();
    getBeaconResult().setSBeaconId(getBeacon().getSBeaconId());
    getBeaconResult().setDurationIdentify(getIdentifyWatch().getElapsedTime());
    save();
    getBeacon().getCallback().identifyAction();
  }

  public void startRegister() {
    write(getSeparator("Start Register"));
    getRegisterWatch().start();
    getBeaconActionListener().registerWriteBeaconSettings(getBeacon());
  }

  public void startConfigPartsTest() {
    getBeaconActionListener().configParts(getBeacon());
  }

  public void endRegister(boolean result) {
    getRegisterWatch().stop();
    getTotalConfigWatch().stop();
    long timestamp = TestSuite.getDateTime().getUnixTimestamp();
    write("end automatic config: " + timestamp);
    getBeaconResult().setEndAutomaticConfig(timestamp);
    write(getSeparator("End Register"));
    getBeaconResult().setDurationRegister(getRegisterWatch().getElapsedTime());
    getBeaconResult().setDurationTotalConfig(getTotalConfigWatch().getElapsedTime());
    save();
    getBeacon().getCallback().registerAction(getBeacon(), result);
  }

  public void startUpdate() {
    write(getSeparator("Start Update"));
    getUpdateWatch().start();
    getBeaconActionListener().updateWriteBeaconSettings(getBeacon());
  }

  public void endUpdate(boolean result) {
    getUpdateWatch().stop();
    write(getSeparator("End Update"));
    getBeaconResult().setDurationUpdate(getUpdateWatch().getElapsedTime());
    save();
    getBeacon().getCallback().updateAction(getBeacon(), result);
  }

  public void logOnSetEddystoneUrl(boolean result) {
    logEddystoneUrl("url set", result);
    getBeaconResult().getField(Field.EDDYSTONE_URL).setResult(result);
  }

  public void logOnFailedToSetEddystoneUrl(boolean result) {
    logEddystoneUrl("url set failed", result);
    getBeaconResult().getField(Field.EDDYSTONE_URL).setFailureSet(result);
  }

  public void logOnFailedToReadEddystoneUrl(boolean result) {
    logEddystoneUrl("url read failed", result);
    getBeaconResult().getField(Field.EDDYSTONE_URL).setFailureRead(result);
  }

  private void logEddystoneUrl(String title, boolean result) {
    write(title + " result: " + result);
  }

  public void logOnSetFrameTypeConnectionRatesUrl(boolean resultConnectable, boolean resultNonConnectable) {
    logFrameTypeConnectionRatesUrl("url connection rate set", resultConnectable, resultNonConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_URL_CONNECTABLE_RATE).setResult(resultConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NON_CONNECTABLE_RATE).setResult(resultNonConnectable);
  }

  public void logOnFailedToSetFrameTypeConnectionRatesUrl(boolean resultConnectable, boolean resultNonConnectable) {
    logFrameTypeConnectionRatesUrl("url connection rate set failed", resultConnectable, resultNonConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_URL_CONNECTABLE_RATE).setFailureSet(resultConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NON_CONNECTABLE_RATE).setFailureSet(resultNonConnectable);
  }

  public void logOnFailedToReadFrameTypeConnectionRatesUrl(boolean resultConnectable, boolean resultNonConnectable) {
    logFrameTypeConnectionRatesUrl("url connection rate read failed", resultConnectable, resultNonConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_URL_CONNECTABLE_RATE).setFailureRead(resultConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NON_CONNECTABLE_RATE).setFailureRead(resultNonConnectable);
  }

  private void logFrameTypeConnectionRatesUrl(String title, boolean resultConnectable, boolean resultNonConnectable) {
    writeFrameTypeConnectionRates(title, resultConnectable, resultNonConnectable);
  }

  public void logOnSetFrameTypeIntervalTxPowerUrl(boolean statusTxEnergy, boolean statusTxStandard,
                                                   boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerUrl("url interval tx power set", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_DAY_ADVERTISEMENT_RATE).setResult(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_DAY_TRANSMISSION_POWER).setResult(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NIGHT_ADVERTISEMENT_RATE).setResult(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NIGHT_TRANSMISSION_POWER).setResult(statusTxEnergy);
  }

  public void logOnFailedToSetFrameTypeIntervalTxPowerUrl(boolean statusTxEnergy, boolean statusTxStandard,
                                                          boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerUrl("url interval tx power set failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_DAY_ADVERTISEMENT_RATE).setFailureSet(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_DAY_TRANSMISSION_POWER).setFailureSet(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NIGHT_ADVERTISEMENT_RATE).setFailureSet(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NIGHT_TRANSMISSION_POWER).setFailureSet(statusTxEnergy);
  }

  public void logOnFailedToReadFrameTypeIntervalTxPowerUrl(boolean statusTxEnergy, boolean statusTxStandard,
                                                          boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerUrl("url interval tx power read failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_DAY_ADVERTISEMENT_RATE).setFailureRead(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_DAY_TRANSMISSION_POWER).setFailureRead(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NIGHT_ADVERTISEMENT_RATE).setFailureRead(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_URL_NIGHT_TRANSMISSION_POWER).setFailureRead(statusTxEnergy);
  }

  private void logFrameTypeIntervalTxPowerUrl(String title, boolean statusTxEnergy,
                                                      boolean statusTxStandard, boolean statusAdvEnergy,
                                                      boolean statusAdvStandard) {
    writeFrameTypeIntervalTxPower(title, statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
  }

  public void logOnSetEddystoneUid(boolean statusInstance, boolean statusNamespace) {
    logEddystoneUid("uid set", statusInstance, statusNamespace);
    getBeaconResult().getField(Field.EDDYSTONE_UID_INSTANCE).setResult(statusInstance);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NAMESPACE).setResult(statusNamespace);
  }

  public void logOnFailedToSetEddystoneUid(boolean statusInstance, boolean statusNamespace) {
    logEddystoneUid("uid set failed", statusInstance, statusNamespace);
    getBeaconResult().getField(Field.EDDYSTONE_UID_INSTANCE).setFailureSet(statusInstance);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NAMESPACE).setFailureSet(statusNamespace);
  }

  public void logOnFailedToReadEddystoneUid(boolean statusInstance, boolean statusNamespace) {
    logEddystoneUid("uid read failed", statusInstance, statusNamespace);
    getBeaconResult().getField(Field.EDDYSTONE_UID_INSTANCE).setFailureRead(statusInstance);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NAMESPACE).setFailureRead(statusNamespace);
  }

  private void logEddystoneUid(String title, boolean statusInstance, boolean statusNamespace) {
    write(title + ", result instance: " + statusInstance +
            ", result namespace: " + statusNamespace);
  }

  public void logOnSetFrameTypeConnectionRatesUid(boolean resultConnectable, boolean resultNonConnectable) {
    logFrameTypeConnectionRatesUid("uid connection rate set", resultConnectable, resultNonConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_UID_CONNECTABLE_RATE).setResult(resultConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NON_CONNECTABLE_RATE).setResult(resultNonConnectable);
  }

  public void logOnFailedToSetFrameTypeConnectionRatesUid(boolean resultConnectable, boolean resultNonConnectable) {
    logFrameTypeConnectionRatesUid("uid connection rate set failed", resultConnectable, resultNonConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_UID_CONNECTABLE_RATE).setFailureSet(resultConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NON_CONNECTABLE_RATE).setFailureSet(resultNonConnectable);
  }

  public void logOnFailedToReadFrameTypeConnectionRatesUid(boolean resultConnectable, boolean resultNonConnectable) {
    logFrameTypeConnectionRatesUid("uid connection rate read failed", resultConnectable, resultNonConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_UID_CONNECTABLE_RATE).setFailureRead(resultConnectable);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NON_CONNECTABLE_RATE).setFailureRead(resultNonConnectable);
  }

  private void logFrameTypeConnectionRatesUid(String title, boolean resultConnectable, boolean resultNonConnectable) {
    writeFrameTypeConnectionRates(title, resultConnectable, resultNonConnectable);
  }

  public void logOnSetFrameTypeIntervalTxPowerUid(boolean statusTxEnergy, boolean statusTxStandard,
                                                  boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerUid("uid interval tx power set", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_DAY_ADVERTISEMENT_RATE).setResult(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_DAY_TRANSMISSION_POWER).setResult(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NIGHT_ADVERTISEMENT_RATE).setResult(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER).setResult(statusTxEnergy);
  }

  public void logOnFailedToSetFrameTypeIntervalTxPowerUid(boolean statusTxEnergy, boolean statusTxStandard,
                                                          boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerUid("uid interval tx power set failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_DAY_ADVERTISEMENT_RATE).setFailureSet(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_DAY_TRANSMISSION_POWER).setFailureSet(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NIGHT_ADVERTISEMENT_RATE).setFailureSet(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER).setFailureSet(statusTxEnergy);
  }

  public void logOnFailedToReadFrameTypeIntervalTxPowerUid(boolean statusTxEnergy, boolean statusTxStandard,
                                                          boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerUid("uid interval tx power read failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_DAY_ADVERTISEMENT_RATE).setFailureRead(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_DAY_TRANSMISSION_POWER).setFailureRead(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NIGHT_ADVERTISEMENT_RATE).setFailureRead(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER).setFailureRead(statusTxEnergy);
  }

  private void logFrameTypeIntervalTxPowerUid(String title,boolean statusTxEnergy, boolean statusTxStandard,
                                              boolean statusAdvEnergy, boolean statusAdvStandard) {
    writeFrameTypeIntervalTxPower(title, statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
  }

  public void logOnSetFrameTypeIntervalTxPowerTlm(boolean statusTxEnergy, boolean statusTxStandard,
                                                  boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerTlm("tlm interval tx power set", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_DAY_ADVERTISEMENT_RATE).setResult(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_DAY_TRANSMISSION_POWER).setResult(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_NIGHT_ADVERTISEMENT_RATE).setResult(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_NIGHT_TRANSMISSION_POWER).setResult(statusTxEnergy);
  }

  public void logOnFailedToSetFrameTypeIntervalTxPowerTlm(boolean statusTxEnergy, boolean statusTxStandard,
                                                          boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerTlm("tlm interval tx power set failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_DAY_ADVERTISEMENT_RATE).setFailureSet(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_DAY_TRANSMISSION_POWER).setFailureSet(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_NIGHT_ADVERTISEMENT_RATE).setFailureSet(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_NIGHT_TRANSMISSION_POWER).setFailureSet(statusTxEnergy);
  }

  public void logOnFailedToReadFrameTypeIntervalTxPowerTlm(boolean statusTxEnergy, boolean statusTxStandard,
                                                          boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerTlm("tlm interval tx power read failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_DAY_ADVERTISEMENT_RATE).setFailureRead(statusAdvStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_DAY_TRANSMISSION_POWER).setFailureRead(statusTxStandard);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_NIGHT_ADVERTISEMENT_RATE).setFailureRead(statusAdvEnergy);
    getBeaconResult().getField(Field.EDDYSTONE_TLM_NIGHT_TRANSMISSION_POWER).setFailureRead(statusTxEnergy);
  }

  private void logFrameTypeIntervalTxPowerTlm(String title, boolean statusTxEnergy, boolean statusTxStandard,
                                              boolean statusAdvEnergy, boolean statusAdvStandard) {
    writeFrameTypeIntervalTxPower(title, statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
  }

  public void logOnSetFrameTypeIntervalTxPowerSBeacon(boolean statusTxEnergy, boolean statusTxStandard,
                                                  boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerSBeacon("s beacon interval tx power set", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.S_BEACON_DAY_ADVERTISEMENT_RATE).setResult(statusAdvStandard);
    getBeaconResult().getField(Field.S_BEACON_DAY_TRANSMISSION_POWER).setResult(statusTxStandard);
    getBeaconResult().getField(Field.S_BEACON_NIGHT_ADVERTISEMENT_RATE).setResult(statusAdvEnergy);
    getBeaconResult().getField(Field.S_BEACON_NIGHT_TRANSMISSION_POWER).setResult(statusTxEnergy);
  }

  public void logOnFailedToSetFrameTypeIntervalTxPowerSBeacon(boolean statusTxEnergy, boolean statusTxStandard,
                                                          boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerSBeacon("s beacon interval tx power set failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.S_BEACON_DAY_ADVERTISEMENT_RATE).setFailureSet(statusAdvStandard);
    getBeaconResult().getField(Field.S_BEACON_DAY_TRANSMISSION_POWER).setFailureSet(statusTxStandard);
    getBeaconResult().getField(Field.S_BEACON_NIGHT_ADVERTISEMENT_RATE).setFailureSet(statusAdvEnergy);
    getBeaconResult().getField(Field.S_BEACON_NIGHT_TRANSMISSION_POWER).setFailureSet(statusTxEnergy);
  }

  public void logOnFailedToReadFrameTypeIntervalTxPowerSBeacon(boolean statusTxEnergy, boolean statusTxStandard,
                                                              boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerSBeacon("s beacon interval tx power read failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.S_BEACON_DAY_ADVERTISEMENT_RATE).setFailureRead(statusAdvStandard);
    getBeaconResult().getField(Field.S_BEACON_DAY_TRANSMISSION_POWER).setFailureRead(statusTxStandard);
    getBeaconResult().getField(Field.S_BEACON_NIGHT_ADVERTISEMENT_RATE).setFailureRead(statusAdvEnergy);
    getBeaconResult().getField(Field.S_BEACON_NIGHT_TRANSMISSION_POWER).setFailureRead(statusTxEnergy);
  }

  private void logFrameTypeIntervalTxPowerSBeacon(String title, boolean statusTxEnergy,
                                                  boolean statusTxStandard, boolean statusAdvEnergy,
                                                  boolean statusAdvStandard) {
    writeFrameTypeIntervalTxPower(title, statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
  }

  private void writeFrameTypeIntervalTxPower(String title, boolean statusTxEnergy,
                                             boolean statusTxStandard, boolean statusAdvEnergy,
                                             boolean statusAdvStandard) {
    write(title + ", day advertisement rate: " + statusAdvStandard +
            ", day transmission power: " + statusTxStandard +
            ", night advertisement rate: " + statusAdvEnergy +
            ", night transmission power: " + statusTxEnergy);
  }

  private void writeFrameTypeConnectionRates(String title, boolean resultConnectable,
                                             boolean resultNonConnectable) {
    write(title + ", result connectable: " + resultConnectable +
            ", result non connectable: " + resultNonConnectable);
  }

  public void logOnIBeaconUuid(boolean result) {
    String title = "i beacon uuid";
    logIBeaconUuid(title, result);
    getBeaconResult().getField(Field.I_BEACON_UUID).setResult(result);
  }

  public void logOnFailedtoSetIBeaconUuid(boolean result) {
    String title = "i beacon uuid set failed";
    logIBeaconUuid(title, result);
    getBeaconResult().getField(Field.I_BEACON_UUID).setFailureSet(result);
  }

  public void logOnFailedToReadIBeaconUuid(boolean result) {
    String title = "i beacon uuid read failed";
    logIBeaconUuid(title, result);
    getBeaconResult().getField(Field.I_BEACON_UUID).setFailureRead(result);
  }

  private void logIBeaconUuid(String title, boolean result) {
    write(title + ", result: " + result);
  }

  public void logOnIBeaconMajorMinor(boolean major, boolean minor) {
    logIBeaconMajorMinor("i beacon major minor", major, minor);
    getBeaconResult().getField(Field.I_BEACON_MAJOR).setResult(major);
    getBeaconResult().getField(Field.I_BEACON_MINOR).setResult(minor);
  }

  public void logOnFailedToSetIBeaconMajorMinor(boolean major, boolean minor) {
    logIBeaconMajorMinor("i beacon major minor set failed", major, minor);
    getBeaconResult().getField(Field.I_BEACON_MAJOR).setFailureSet(major);
    getBeaconResult().getField(Field.I_BEACON_MINOR).setFailureSet(minor);
  }

  public void logOnFailedToReadIBeaconMajorMinor(boolean major, boolean minor) {
    logIBeaconMajorMinor("i beacon major minor read failed", major, minor);
    getBeaconResult().getField(Field.I_BEACON_MAJOR).setFailureRead(major);
    getBeaconResult().getField(Field.I_BEACON_MINOR).setFailureRead(minor);
  }

  private void logIBeaconMajorMinor(String title, boolean major, boolean minor) {
    write(title + ", major: " + major + ", minor: " + minor);
  }

  public void logOnSetFrameTypeIntervalTxPowerIBeacon(boolean statusTxEnergy, boolean statusTxStandard,
                                                      boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerIBeacon("i beacon interval tx power set", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.I_BEACON_DAY_ADVERTISEMENT_RATE).setResult(statusAdvStandard);
    getBeaconResult().getField(Field.I_BEACON_DAY_TRANSMISSION_POWER).setResult(statusTxStandard);
    getBeaconResult().getField(Field.I_BEACON_NIGHT_ADVERTISEMENT_RATE).setResult(statusAdvEnergy);
    getBeaconResult().getField(Field.I_BEACON_NIGHT_TRANSMISSION_POWER).setResult(statusTxEnergy);
  }

  public void logOnFailedToSetFrameTypeIntervalTxPowerIBeacon(boolean statusTxEnergy, boolean statusTxStandard,
                                                              boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerIBeacon("i beacon interval tx power set failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.I_BEACON_DAY_ADVERTISEMENT_RATE).setFailureSet(statusAdvStandard);
    getBeaconResult().getField(Field.I_BEACON_DAY_TRANSMISSION_POWER).setFailureSet(statusTxStandard);
    getBeaconResult().getField(Field.I_BEACON_NIGHT_ADVERTISEMENT_RATE).setFailureSet(statusAdvEnergy);
    getBeaconResult().getField(Field.I_BEACON_NIGHT_TRANSMISSION_POWER).setFailureSet(statusTxEnergy);
  }

  public void logOnFailedToReadFrameTypeIntervalTxPowerIBeacon(boolean statusTxEnergy, boolean statusTxStandard,
                                                              boolean statusAdvEnergy, boolean statusAdvStandard) {
    logFrameTypeIntervalTxPowerIBeacon("i beacon interval tx power read failed", statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
    getBeaconResult().getField(Field.I_BEACON_DAY_ADVERTISEMENT_RATE).setFailureRead(statusAdvStandard);
    getBeaconResult().getField(Field.I_BEACON_DAY_TRANSMISSION_POWER).setFailureRead(statusTxStandard);
    getBeaconResult().getField(Field.I_BEACON_NIGHT_ADVERTISEMENT_RATE).setFailureRead(statusAdvEnergy);
    getBeaconResult().getField(Field.I_BEACON_NIGHT_TRANSMISSION_POWER).setFailureRead(statusTxEnergy);
  }

  private void logFrameTypeIntervalTxPowerIBeacon(String title, boolean statusTxEnergy, boolean statusTxStandard,
                                                  boolean statusAdvEnergy, boolean statusAdvStandard) {
    writeFrameTypeIntervalTxPower(title, statusTxEnergy, statusTxStandard, statusAdvEnergy, statusAdvStandard);
  }

  public void logOnMaintenanceTlmResult(boolean result) {
    write("maintenance tlm, result: " + result);
    getBeaconResult().getField(Field.MAINTENANCE_TLM).setResult(result);
  }

  public void logOnReadMaintenanceDeviceStatus(boolean result) {
    logMaintenanceDeviceStatus("maintenance device status read", result);
    getBeaconResult().getField(Field.MAINTENANCE_DEVICE_STATUS).setResult(result);
  }

  public void logOnFailedToReadMaintenanceDeviceStatus(boolean result) {
    logMaintenanceDeviceStatus("maintenance device status read failed", result);
    getBeaconResult().getField(Field.MAINTENANCE_DEVICE_STATUS).setFailureRead(result);
  }

  private void logMaintenanceDeviceStatus(String title, boolean result) {
    write(title + ", result: " + result);
  }

  public void logSetPassword() {
    getPasswordWatch().start();
  }

  public void logOnSetPassword(boolean result) {
    getPasswordWatch().stop();
    getBeaconResult().setDurationPassword(getPasswordWatch().getElapsedTime());
    getBeaconResult().getField(Field.PASSWORD).setResult(result);
  }

  public void logOnConnect(boolean connected, boolean authenticated) {
    getConnectWatch().stop();
    write(getSeparator("On Connect"));
    Log.d(TAG, "connected: " + connected + ", authenticated: " + authenticated);
    long elapsedTime = getConnectWatch().getElapsedTime();
    if (getBeacon().getAction() == Action.ConnectTest && getConnectStatistics() != null) {
      if (connected && authenticated) {
        getConnectStatistics().addDuration(elapsedTime);
      }
      getConnectStatistics().addConnected(connected);
      getConnectStatistics().addAuthenticated(authenticated);
    }
    getBeaconResult().setDurationConnect(elapsedTime);
    getBeaconResult().getField(Field.CONNECT).setResult(connected && authenticated);
  }

  public void logOnConnectScalability(boolean connected, boolean authenticated) {
    getConnectResult().addValue(connected);
    getConnectResult().addValue(authenticated);
  }

  public void connectScalability(String password) {
    getTotalConfigWatch().start();
    getBeacon().getConfigurable().connect(MainActivity.getInstance(), password);
  }

  public void logOnDisconnectScalability() {
    getTotalConfigWatch().stop();
  }

  private Frequency getConnectResult() {
    return this.connectResult;
  }

  public void disconnect() {
    getDisconnectWatch().start();
    getBeacon().getConfigurable().disconnect();
  }

  public void logOnDisconnect() {
    getDisconnectWatch().stop();
    getTotalConfigWatch().stop();
    if (getBeacon().getAction() == Action.ConfigPartsTest) {
      getBeaconResult().setMemoryUsageConfigEnd(AppStorage.getAppMemoryUsage());
    } else if (getBeacon().getAction() == Action.MemoryTest) {
      getMemoryConfigEndOutput().writeCsv(AppStorage.getAppMemoryUsage());
    }
    getBeaconResult().setDurationTotalConfig(getTotalConfigWatch().getElapsedTime());
    getBeaconResult().setDurationDisconnect(getDisconnectWatch().getElapsedTime());
    getBeaconResult().getField(Field.DISCONNECT).setResult(true);
  }

  private BeaconObject getBeacon() {
    return this.beacon;
  }

  public Output getOutput() {
    return this.output;
  }

  private BeaconResult getBeaconResult() {
    return this.beaconResult;
  }

  private BeaconActionListener getBeaconActionListener() {
    return getBeacon().getBeaconActionListener();
  }

  private ObjectSerialization getSerializationConfigResult() {
    return this.serializationConfigResult;
  }

  private String getSeparator(String title) {
    return "################ " + title + " ################";
  }

  private void writeIdentifier() {
    if (MainController.SETTING.isTest()) {
      getOutput().write(getSeparator("Start General"));
      getOutput().write("mac: " + getBeacon().getMac() + ", sBeacon id: " + getBeacon().getSBeaconId());
      getOutput().write(getSeparator("End General"));
    }
  }

  public void save() {
    if (MainController.SETTING.isTest()) {
      getSerializationConfigResult().serialize(getBeaconResult());
    }
  }

  private void write(String data) {
    if (MainController.SETTING.isTest()) {
      long timestamp = TestSuite.getDateTime().getUnixTimestamp();
      getOutput().write(timestamp, data);
    }
  }

  private void writeConfigParts(String data) {
    long timestamp = TestSuite.getDateTime().getUnixTimestamp();
    getConfigPartsOutput().write(timestamp, data);
  }

  public Frequency getConfigResult() {
    return this.configResult;
  }

  private StopWatch getConnectWatch() {
    return this.connectWatch;
  }

  private StopWatch getTotalConfigWatch() {
    return this.totalConfigWatch;
  }

  private StopWatch getVerifyConfigWatch() {
    return this.verifyConfigWatch;
  }

  private StopWatch getIdentifyWatch() {
    return this.identifyWatch;
  }

  private StopWatch getRegisterWatch() {
    return this.registerWatch;
  }

  private StopWatch getUpdateWatch() {
    return this.updateWatch;
  }

  private StopWatch getEddystoneUrlWatch() {
    return this.eddystoneUrlWatch;
  }

  private StopWatch getEddystoneUidWatch() {
    return this.eddystoneUidWatch;
  }

  private StopWatch getPasswordWatch() {
    return this.passwordWatch;
  }

  private StopWatch getDisconnectWatch() {
    return this.disconnectWatch;
  }

  public StopWatch getEddystoneTlmWatch() {
    return eddystoneTlmWatch;
  }

  public StopWatch getSBeaconWatch() {
    return sBeaconWatch;
  }

  public StopWatch getIBeaconWatch() {
    return iBeaconWatch;
  }

  public StopWatch getMaintenanceWatch() {
    return maintenanceWatch;
  }

  public void logStartEddystoneUrl() {
    getEddystoneUrlWatch().start();
  }

  public void logStartEddystoneUid() {
    getEddystoneUidWatch().start();
  }

  public void logStartEddystoneTlm() {
    getEddystoneTlmWatch().start();
  }

  public void logStartSBeacon() {
    getSBeaconWatch().start();
  }

  public void logStartIBeacon() {
    getIBeaconWatch().start();
  }

  public void logStartMaintenance() {
    getMaintenanceWatch().start();
  }

  public void logEndEddystoneUrl() {
    getEddystoneUrlWatch().stop();
    getBeaconResult().setDurationEddystoneUrl(getEddystoneUrlWatch().getElapsedTime());
  }

  public void logEndEddystoneUid() {
    getEddystoneUidWatch().stop();
    getBeaconResult().setDurationEddystoneUid(getEddystoneUidWatch().getElapsedTime());
  }

  public void logEndEddystoneTlm() {
    getEddystoneTlmWatch().stop();
    getBeaconResult().setDurationEddystoneTlm(getEddystoneTlmWatch().getElapsedTime());
  }

  public void logEndSBeacon() {
    getSBeaconWatch().stop();
    getBeaconResult().setDurationSBeacon(getSBeaconWatch().getElapsedTime());
  }

  public void logEndIBeacon() {
    getIBeaconWatch().stop();
    getBeaconResult().setDurationIBeacon(getIBeaconWatch().getElapsedTime());
  }

  public void logEndMaintenance() {
    getMaintenanceWatch().stop();
    getBeaconResult().setDurationMaintenance(getMaintenanceWatch().getElapsedTime());
  }

}
