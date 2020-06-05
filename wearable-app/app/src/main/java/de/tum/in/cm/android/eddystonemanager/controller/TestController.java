package de.tum.in.cm.android.eddystonemanager.controller;

import android.os.SystemClock;
import android.util.Log;

import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.domainobjects.ConfigurableBeacon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.tum.in.cm.android.eddystonemanager.configurator.Action;
import de.tum.in.cm.android.eddystonemanager.data.BeaconConfig;
import de.tum.in.cm.android.eddystonemanager.data.BeaconObject;
import de.tum.in.cm.android.eddystonemanager.data.BeaconUnregistered;
import de.tum.in.cm.android.eddystonemanager.evaluation.BeaconResult;
import de.tum.in.cm.android.eddystonemanager.evaluation.ConfigResult;
import de.tum.in.cm.android.eddystonemanager.evaluation.ConnectStatistics;
import de.tum.in.cm.android.eddystonemanager.evaluation.Output;
import de.tum.in.cm.android.eddystonemanager.evaluation.TestBeacons;
import de.tum.in.cm.android.eddystonemanager.evaluation.TestConfig;
import de.tum.in.cm.android.eddystonemanager.utils.app.AppStorage;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.BeaconUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.ObjectSerialization;
import de.tum.in.cm.android.eddystonemanager.utils.general.TestSuite;

public class TestController extends ApplicationController {

  private static final TestConfig CONFIG = TestConfig.WORST_SETTINGS;

  public static int TEST_ROUNDS = 20;
  public static int TEST_ROUND_COUNTER = 0;
  public static int TEST_CONNECT_COUNTER = 0;
  public static int TEST_AUTHENTICATE_COUNTER = 0;

  private static final String TAG = TestController.class.getSimpleName();
  private static final int INIT_WAIT_TIME = 5000;

  private static final String PASSWORD_2016 = "ZxFdGWwJGe9YSW9H5YwmQM2f9zWHEBdT0XYDOHzSwBk=";
  private static final String PASSWORD_2017 = "SIiAADrX7tQiueNq6oUIZIJc7G9I2T2h9XLHc5EKFBs=";
  private static final String PASSWORD_2016_UNENCRYPTED = "iotcm2016";

  //private static final Action ACTION = Action.PreConfigure;
  //public static final String PASSWORD = PASSWORD_2016;

  public static final Action ACTION = Action.ConfigTest;
  public static final String PASSWORD = PASSWORD_2016_UNENCRYPTED;

  public static final String TEST_MAC = TestBeacons.B2_C05211C2D46B980D.getMac();
  public static final String START_CONNECTION_TEST = "########## Start Connection Test ##########";
  public static final String END_CONNECTION_TEST = "########## End Connection Test ##########";
  public static final int WAIT_CONNECT = 1000;

  public static final List<String> CONFIG_DEVICES = TestBeacons.BEACONS_10;
  public static final int NUM_TEST_DEVICES = CONFIG_DEVICES.size();
  private static boolean CONFIG_TEST_STARTED = false;

  private final BeaconConfig testConfig;
  private static String rawNewPassword;
  private static List<BeaconObject> beaconObjects;
  private static ConnectStatistics connectStatistics;
  private static List<BeaconResult> beaconResultList;
  private static Map<String, List<ConfigResult>> beaconResultMap;
  private static ObjectSerialization serializationConnect;
  private static ObjectSerialization serializationConfigParts;
  private static Output configPartsOutput;
  private static Output memoryConfigStartOutput;
  private static Output memoryConfigEndOutput;
  private static Queue<Thread> configWorkers;

  public TestController() {
    this.beaconObjects = new LinkedList<>();
    this.connectStatistics = new ConnectStatistics();
    this.beaconResultList = new ArrayList<>();
    this.beaconResultMap = new HashMap<>();
    this.configWorkers = new LinkedList<>();
    this.testConfig = getBeaconRestfulService().getBeaconConfig(CONFIG.getStr());
    this.rawNewPassword = getConfig().getRawNewPassword();
    if (ACTION == Action.ConfigPartsTest) {
      this.configPartsOutput = new Output("config_parts_" + TestSuite.EVALUATION_FILENAME);
      this.serializationConfigParts = new ObjectSerialization("config_parts_" + TestSuite.SERIALIZATION_FILENAME);
    }
    if (ACTION == Action.MemoryTest) {
      this.memoryConfigStartOutput = new Output("memory_config_start_" + TestSuite.EVALUATION_FILENAME);
      this.memoryConfigEndOutput = new Output("memory_config_end_" + TestSuite.EVALUATION_FILENAME);
    }
    if (ACTION == Action.ConnectTest) {
      this.serializationConnect = new ObjectSerialization("connect_" + TestSuite.SERIALIZATION_FILENAME);
    }
  }

  private void startSingleBeaconTest(Beacon beacon) {
    String mac = beacon.getDevice().getAddress();
    if (mac.equals(TEST_MAC)) {
      getBeaconManager().stopScan();
      if (beacon instanceof ConfigurableBeacon) {
        BeaconUnregistered beaconUnregistered = new BeaconUnregistered(beacon);
        BeaconObject beaconObject = new BeaconObject(beaconUnregistered, getBeaconDataService(),
                getServiceCallback(), getConfig(), ACTION, getAppConfig());
        if (ACTION == Action.ConnectTest) {
          beaconObject.getStatistics().getOutput().write(START_CONNECTION_TEST);
        }
        TEST_ROUND_COUNTER++;
        beaconObject.connect(PASSWORD);
      }
    }
  }

  private void startPreconfigureBeacons(List<BeaconObject> beaconObjects) {
    for(int i = 0; i < beaconObjects.size(); i++) {
      final BeaconObject beaconObject = beaconObjects.get(i);
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          beaconObject.connect(PASSWORD);
        }
      });
      if (i == 0) {
        thread.start();
      } else {
        getConfigWorkers().add(thread);
      }
    }
  }

  private void startConfigTest(List<BeaconObject> beaconObjects) {
    CONFIG_TEST_STARTED = true;
    getBeaconManager().stopScan();
    beaconObjects.get(0).getStatistics().startConfigRound();
  }

  private void collectBeacons(Beacon beacon) {
    Log.d(TAG, "Beacon found: " + beacon.getDevice().getAddress());
    if (CONFIG_DEVICES.contains(beacon.getDevice().getAddress())) {
      BeaconUnregistered beaconUnregistered = new BeaconUnregistered(beacon);
      BeaconObject beaconObject = new BeaconObject(beaconUnregistered, getBeaconDataService(),
              getServiceCallback(), getConfig(), ACTION, getAppConfig());
      getBeaconObjects().add(beaconObject);
      if (getBeaconObjects().size() == NUM_TEST_DEVICES && !CONFIG_TEST_STARTED) {
        switch (ACTION) {
          case ConfigTest:
            startConfigTest(getBeaconObjects());
            break;
          case PreConfigure:
            startPreconfigureBeacons(getBeaconObjects());
            break;
        }
      }
    }
  }

  @Override
  public void onBeaconFound(Beacon beacon) {
    switch (ACTION) {
      case ConnectTest:
        startSingleBeaconTest(beacon);
        break;
      case ConfigTest:
        collectBeacons(beacon);
        break;
      case ConfigPartsTest:
        startSingleBeaconTest(beacon);
        break;
      case PreConfigure:
        collectBeacons(beacon);
        break;
      case MemoryTest:
        startSingleBeaconTest(beacon);
        break;
    }
  }

  public static void writeConfigResult() {
    try {
      String path = AppStorage.STORAGE_PATH +
              "config_devices_" + NUM_TEST_DEVICES + "_" +
              CONFIG.getStr().replaceAll(" ", "_") + "_" +
              TestSuite.EVALUATION_FILENAME;
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      String counter = "connected: " + TEST_CONNECT_COUNTER +
              ", authenticated: " + TEST_AUTHENTICATE_COUNTER +
              BeaconUtils.LINE_SEPARATOR;
      writer.write(counter);
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(getBeaconResultMap(), writer);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      Log.d(TAG, "write config evaluation", e);
    }
  }

  public static List<BeaconObject> getBeaconObjects() {
    return beaconObjects;
  }

  public static List<BeaconResult> getBeaconResultList() {
    return beaconResultList;
  }

  public static void addBeaconToResultMap(String key, ConfigResult configResult) {
    if (!getBeaconResultMap().containsKey(key)) {
      getBeaconResultMap().put(key, new ArrayList<ConfigResult>());
    }
    getBeaconResultMap().get(key).add(configResult);
  }

  public static Map<String, List<ConfigResult>> getBeaconResultMap() {
    return beaconResultMap;
  }

  public static ConnectStatistics getConnectStatistics() {
    return connectStatistics;
  }

  public static ObjectSerialization getSerializationConnect() {
    return serializationConnect;
  }

  public static ObjectSerialization getSerializationConfigParts() {
    return serializationConfigParts;
  }

  public static Output getConfigPartsOutput() {
    return configPartsOutput;
  }

  public static Output getMemoryConfigStartOutput() {
    return memoryConfigStartOutput;
  }

  public static Output getMemoryConfigEndOutput() {
    return memoryConfigEndOutput;
  }

  public static Queue<Thread> getConfigWorkers() {
    return configWorkers;
  }

  public static String getRawNewPassword() {
    return rawNewPassword;
  }

  private BeaconConfig getConfig() {
    return testConfig;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    getBeaconScanHandler().doBindService();
    new Thread(new Runnable() {
      @Override
      public void run() {
        while(getBeaconDataService().getTlmFrames().size() == 0) {
          SystemClock.sleep(INIT_WAIT_TIME);
        }
        getBeaconManager().startScan();
      }
    }).start();
  }

  @Override
  public void onDestroy() {
    getBeaconScanHandler().getScanService().stopScan();
    getBeaconScanHandler().doUnbindService();
  }

  @Override
  public void switchToBeaconRegister(BeaconUnregistered beaconUnregistered) {}

  @Override
  public void switchToCamera() {}

  @Override
  public void setTargetBeaconMac(String macAddress) {}

}
