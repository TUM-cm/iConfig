package de.tum.in.cm.android.eddystonemanager.utils.app;

import android.util.Log;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import de.tum.in.cm.android.eddystonemanager.controller.MainController;
import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.utils.general.FileUtils;

public class Config {

	public static final String APP_SECTION = "App";
	public static final String REST_BACKEND_SECTION = "RestBeaconBackend";
	private static final String TAG = Config.class.getSimpleName();
	public static boolean CHECKED_WRITE_CONFIG = false;
	public static boolean WRITE_CONFIG = false;
	private static Config instance;
	private static Config defaultConfig;

	private Ini ini;

	private Config(int resource) {
		try {
			InputStream inputStream = FileUtils.getInputStream(resource);
			this.ini = new Ini(inputStream);
		} catch (IOException e) {
			Log.e(TAG, "Config resource", e);
		}
	}

	private Config() {
		try {
			File configFile;
			if (MainController.SETTING.isDemo()) {
				configFile = new File(AppStorage.STORAGE_PATH + AppStorage.CONFIG_DEMO_FILENAME);
			} else {
				configFile = new File(AppStorage.STORAGE_PATH + AppStorage.CONFIG_FILENAME);
			}
			if (!configFile.exists()) {
				configFile.createNewFile();
			}
			this.ini = new Ini(configFile);
		} catch (IOException e) {
			Log.e(TAG, "Config file", e);
		}
	}

	private static boolean isExternalConfigFileReadable() {
		File configFile;
		if (MainController.SETTING.isDemo()) {
			configFile = new File(AppStorage.STORAGE_PATH + AppStorage.CONFIG_DEMO_FILENAME);
		} else {
			configFile = new File(AppStorage.STORAGE_PATH + AppStorage.CONFIG_FILENAME);
		}
		return configFile.canRead();
	}

	public static Config getInstance(int resource) {
		if (instance == null) {
			if (isExternalConfigFileReadable()) {
				instance = new Config();
			} else {
				instance = new Config(resource);
			}
		}
		return instance;
	}

	public static Config getDefaultConfig() {
		if (defaultConfig == null) {
			defaultConfig = new Config(MainActivity.getConfigResource());
		}
		return defaultConfig;
	}

	public void store() {
		if (isWriteable()) {
			Config fileConfig = new Config();
			for (Map.Entry<String, Ini.Section> entrySection : getIni().entrySet()) {
				String sectionName = entrySection.getKey();
				Ini.Section section = entrySection.getValue();
				for (Map.Entry<String, String> entryValue : section.entrySet()) {
					fileConfig.getIni().put(sectionName, entryValue.getKey(), entryValue.getValue());
				}
			}
			try {
				fileConfig.getIni().store();
			} catch (IOException e) {
				Log.e(TAG, "Store config", e);
			}
		}
	}

	private boolean isWriteable() {
		if (!WRITE_CONFIG && !CHECKED_WRITE_CONFIG) {
			CHECKED_WRITE_CONFIG = true;
			try {
				File testFile = new File(AppStorage.STORAGE_PATH + AppStorage.CONFIG_TEST_FILENAME);
				testFile.createNewFile();
				WRITE_CONFIG = true;
				testFile.delete();
			} catch (IOException e) {
				Log.e(TAG, "Test file not writeable", e);
				WRITE_CONFIG = false;
			}
		}
		return WRITE_CONFIG;
	}

	private Ini getIni() {
		return this.ini;
	}

	public <T> T get(String key, String section, Class<T> type) {
		return getIni().get(section).get(key, type);
	}

}
