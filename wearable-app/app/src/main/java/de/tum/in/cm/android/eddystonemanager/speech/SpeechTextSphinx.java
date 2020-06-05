package de.tum.in.cm.android.eddystonemanager.speech;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.tum.in.cm.android.eddystonemanager.controller.SpeechController;
import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.services.BeaconDataService;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.BeaconUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.GUIUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.SoundUtils;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class SpeechTextSphinx implements RecognitionListener {

  private static final String TAG = SpeechTextSphinx.class.getSimpleName();
  private static final String KEY_ACTIVATE = "activateSpeech";
  private static final String KEY_BEACON_LIST_MENU = "beaconListMenu";
  private static final String KEY_SINGLE_BEACON_MENU = "singleBeaconMenu";
  private static final String KEY_ROOM = "room";
  private static final String KEY_TARGET = "target";
  private static final String KEY_GENERIC_INPUT = "language";
  private static final String KEY_CONFIRM = "confirm";
  private static final String KEY_CAMERA_MENU = "cameraMenu";
  private static final String ACOUSTIC_MODEL = "en-us-ptm";
  private static final String DICT = "cmudict-en-us.dict";
  private static final String LANGUAGE_MODEL = "en-70k-0.2.lm.bin";
  private static final int BARRIER_WAIT_NUM_THREADS = 1;
  private static final int BARRIER_MAX_WAIT_TIME = 5; // seconds

  private SpeechRecognizer speechRecognizer;
  private SpeechController speechController;
  private final BeaconDataService beaconDataService;
  private final SoundUtils soundUtils;
  private final SpeechCallback callback;
  private final String configKeyword;
  private final String beaconListGrammar;
  private final String singleBeaconGrammar;
  private final String roomGrammar;
  private final String targetGrammar;
  private final String confirmGrammar;
  private final String cameraGrammar;
  private final CountDownLatch barrierSpeechRecognizerReady;
  private boolean beaconListMenuActivated;
  private boolean commandExpected;

  public SpeechTextSphinx(Context context, BeaconDataService beaconDataService,
                          SoundUtils soundUtils, SpeechCallback callback,
                          String configKeyword,
                          List<String> beaconListMenu, List<String> singleBeaconMenu) {
    runRecognizerSetup(context);
    this.beaconDataService = beaconDataService;
    if (callback instanceof  SpeechController) {
      this.speechController = (SpeechController) callback;
    }
    this.callback = callback;
    this.soundUtils = soundUtils;
    this.configKeyword = configKeyword;
    this.beaconListGrammar = createMenuGrammar(beaconListMenu);
    this.singleBeaconGrammar = createMenuGrammar(singleBeaconMenu);
    String digitGrammar = createDigitGrammar();
    this.roomGrammar = createRoomGrammar(digitGrammar);
    this.targetGrammar = createTargetGrammar();
    this.confirmGrammar = createConfirmGrammar();
    this.cameraGrammar = createCameraGrammar();
    this.barrierSpeechRecognizerReady = new CountDownLatch(BARRIER_WAIT_NUM_THREADS);
  }

  private void runRecognizerSetup(final Context context) {
    new AsyncTask<Void, Void, Exception>() {
      @Override
      protected Exception doInBackground(Void... params) {
        try {
          Assets assets = new Assets(context);
          File assetDir = assets.syncAssets();
          setupRecognizer(assetDir);
        } catch (IOException e) {
          if (e.getMessage().contains("Microphone might be already in use")) {
            String title = "Speech Recognizer is not available";
            String message = e.getMessage();
            GUIUtils.showOKAlertDialog(MainActivity.getInstance(), title, message);
          }
          Log.e(TAG, "Error initialize Sphinx speech recognizer", e);
        }
        return null;
      }

      @Override
      protected void onPostExecute(Exception result) {
        if (result != null) {
          Log.e(TAG, "Failed to init recognizer", result);
        } else {
          getBarrierSpeechRecognizerReady().countDown();
        }
      }
    }.execute();
  }

  private void setupRecognizer(File assetsDir) throws IOException {
    this.speechRecognizer = SpeechRecognizerSetup.defaultSetup()
            .setAcousticModel(new File(assetsDir, ACOUSTIC_MODEL))
            .setDictionary(new File(assetsDir, DICT))
            .getRecognizer();
    getSpeechRecognizer().addListener(this);
    getSpeechRecognizer().addKeyphraseSearch(KEY_ACTIVATE, getConfigKeyword());
    getSpeechRecognizer().addGrammarSearch(KEY_BEACON_LIST_MENU, getBeaconListGrammar());
    getSpeechRecognizer().addGrammarSearch(KEY_SINGLE_BEACON_MENU, getSingleBeaconGrammar());
    File languageModel = new File(assetsDir, LANGUAGE_MODEL);
    getSpeechRecognizer().addNgramSearch(KEY_GENERIC_INPUT, languageModel);
    getSpeechRecognizer().addGrammarSearch(KEY_CONFIRM, getConfirmGrammar());
    getSpeechRecognizer().addGrammarSearch(KEY_ROOM, getRoomGrammar());
    getSpeechRecognizer().addGrammarSearch(KEY_TARGET, getTargetGrammar());
    getSpeechRecognizer().addGrammarSearch(KEY_CAMERA_MENU, getCameraGrammar());
  }

  private void switchSearch(String searchName) {
    if (getSpeechRecognizer() != null) {
      getSpeechRecognizer().stop();
      if (!searchName.equals(KEY_ACTIVATE)) {
        getSpeechController().activateSpeechActivityCountdown();
      }
      getSpeechRecognizer().startListening(searchName);
    }
  }

  public void stop() {
    getSpeechRecognizer().stop();
  }

  @Override
  public void onBeginningOfSpeech() {}

  // Stop recognizer to get a final result
  @Override
  public void onEndOfSpeech() {
    if (!getSpeechRecognizer().getSearchName().equals(KEY_ACTIVATE)) {
      getSpeechRecognizer().stop();
    }
  }

  @Override
  public void onPartialResult(Hypothesis hypothesis) {
    if (hypothesis == null) {
      return;
    }
    String text = hypothesis.getHypstr();
    if (text.equals(getConfigKeyword()) && !isBeaconListMenuActivated()) {
      setBeaconListMenuActivated(true);
      getSpeechController().speak(SpeechController.ACTIVATED,
              SpeechAction.BeaconListMenuActivated);
    }
  }

  // Callback is called when we stop the recognizer
  @Override
  public void onResult(Hypothesis hypothesis) {
    if (hypothesis != null) {
      String text = hypothesis.getHypstr();
      if (!text.equals(getConfigKeyword()) && isBeaconListMenuActivated()) {
        if (isCommandExpected()) {
          if (getSpeechController().isBeaconSelected()) {
            getCallback().onSingleBeaconMenuResult(text);
          } else {
            getCallback().onBeaconListMenuResult(text);
          }
        } else {
          getCallback().onSpeechResult(text, false);
        }
      }
    }
  }

  private String createMenuGrammar(List<String> values) {
    StringBuilder menuGrammar = new StringBuilder("#JSGF V1.0;");
    menuGrammar.append(BeaconUtils.LINE_SEPARATOR);
    menuGrammar.append("grammar menu;");
    menuGrammar.append(BeaconUtils.LINE_SEPARATOR);
    menuGrammar.append("public <item> = ");
    boolean next = false;
    for(String menuKeyword : values) {
      if (next) {
        menuGrammar.append(" | ");
      }
      menuGrammar.append(menuKeyword);
      next = true;
    }
    menuGrammar.append(";");
    return menuGrammar.toString();
  }

  private String createRoomGrammar(String digitGrammar) {
    StringBuilder roomGrammar = new StringBuilder("#JSGF V1.0;");
    roomGrammar.append(BeaconUtils.LINE_SEPARATOR);
    roomGrammar.append("grammar room;");
    roomGrammar.append(BeaconUtils.LINE_SEPARATOR);
    roomGrammar.append(digitGrammar);
    roomGrammar.append(BeaconUtils.LINE_SEPARATOR);
    roomGrammar.append("public <room> = <digit><digit> point <digit><digit> point <digit><digit><digit>;");
    return roomGrammar.toString();
  }

  private String createDigitGrammar() {
    StringBuilder digitGrammar = new StringBuilder();
    digitGrammar.append("<digit> = ");
    boolean next = false;
    for(Digit digit : Digit.values()) {
      if (next) {
        digitGrammar.append(" | ");
      }
      digitGrammar.append(digit.getWord());
      next = true;
    }
    digitGrammar.append(";");
    return digitGrammar.toString();
  }

  private String createTargetGrammar() {
    StringBuilder targetGrammar = new StringBuilder("#JSGF V1.0;");
    targetGrammar.append(BeaconUtils.LINE_SEPARATOR);
    targetGrammar.append("grammar target;");
    targetGrammar.append(BeaconUtils.LINE_SEPARATOR);
    targetGrammar.append("<digit> = ");
    boolean next = false;
    for(Digit digit : Digit.values()) {
      if (next) {
        targetGrammar.append(" | ");
      }
      targetGrammar.append(digit.getWord());
      next = true;
    }
    targetGrammar.append(";");
    targetGrammar.append(BeaconUtils.LINE_SEPARATOR);
    targetGrammar.append("public <target> = <digit><digit><digit>;");
    return targetGrammar.toString();
  }

  private String createConfirmGrammar() {
    StringBuilder confirmGrammar = new StringBuilder("#JSGF V1.0;");
    confirmGrammar.append(BeaconUtils.LINE_SEPARATOR);
    confirmGrammar.append("grammar confirm;");
    confirmGrammar.append(BeaconUtils.LINE_SEPARATOR);
    confirmGrammar.append("public <answer> = yes | no;");
    return confirmGrammar.toString();
  }

  private String createCameraGrammar() {
    StringBuilder cameraGrammar = new StringBuilder("#JSGF V1.0;");
    cameraGrammar.append(BeaconUtils.LINE_SEPARATOR);
    cameraGrammar.append("grammar camera;");
    cameraGrammar.append("public <control> = shot | zoom (less | more | min | max);");
    return cameraGrammar.toString();
  }

  @Override
  public void onError(Exception e) {
    Log.d(TAG, "Error during sphinx speech recognition", e);
  }

  @Override
  public void onTimeout() {
    switchSearch(KEY_ACTIVATE);
  }

  public void shutdown() {
    if (getSpeechRecognizer() != null) {
      getSpeechRecognizer().stop();
      getSpeechRecognizer().shutdown();
    }
  }

  public void activate() {
    // Do not block UI
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        boolean speechRecognizerReady = false;
        try {
          speechRecognizerReady = getBarrierSpeechRecognizerReady()
                  .await(BARRIER_MAX_WAIT_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          Log.e(TAG, "try to activate sphinx speech recognizer", e);
        }
        if (speechRecognizerReady) {
          getSpeechController().speak("Speech recognition activated",
                  SpeechAction.SpeechActivated);
        } else {
          getSpeechController().deactivateVoiceControl();
          String title = "Speech Recognizer is not available";
          String message = "The Sphinx speech recognizer could not be set up.";
          GUIUtils.showOKAlertDialog(MainActivity.getInstance(), title, message);
        }
      }
    };
    AsyncTask.execute(runnable);
  }

  public void activateBeaconListMenu() {
    setCommandExpected(true);
    getSoundUtils().playBeep();
    switchSearch(KEY_BEACON_LIST_MENU);
  }

  public void activateSingleBeaconMenu(boolean beep) {
    setCommandExpected(true);
    if (beep) {
      getSoundUtils().playBeep();
    }
    switchSearch(KEY_SINGLE_BEACON_MENU);
  }

  public void activateTarget() {
    setCommandExpected(false);
    getSoundUtils().playBeep();
    switchSearch(KEY_TARGET);
  }

  public void activateRoom() {
    setCommandExpected(false);
    getSoundUtils().playBeep();
    switchSearch(KEY_ROOM);
  }

  public void activateGenericInput() {
    setCommandExpected(false);
    getSoundUtils().playBeep();
    switchSearch(KEY_GENERIC_INPUT);
  }

  public void activateConfirm() {
    setCommandExpected(false);
    getSoundUtils().playBeep();
    switchSearch(KEY_CONFIRM);
  }

  public void activateCameraMenu() {
    setCommandExpected(false);
    getSoundUtils().playBeep();
    switchSearch(KEY_CAMERA_MENU);
  }

  public void reset() {
    setCommandExpected(false);
    setBeaconListMenuActivated(false);
    getSoundUtils().playBeep();
    switchSearch(KEY_ACTIVATE);
  }

  private void setCommandExpected(boolean commandExpected) {
    this.commandExpected = commandExpected;
  }

  private SpeechRecognizer getSpeechRecognizer() {
    return this.speechRecognizer;
  }

  private String getConfigKeyword() {
    return this.configKeyword;
  }

  private String getBeaconListGrammar() {
    return this.beaconListGrammar;
  }

  private String getSingleBeaconGrammar() {
    return this.singleBeaconGrammar;
  }

  private String getRoomGrammar() {
    return this.roomGrammar;
  }

  private String getTargetGrammar() {
    return this.targetGrammar;
  }

  private String getConfirmGrammar() {
    return this.confirmGrammar;
  }

  private String getCameraGrammar() {
    return this.cameraGrammar;
  }

  private SpeechCallback getCallback() {
    return this.callback;
  }

  private SpeechController getSpeechController() {
    return this.speechController;
  }

  private void setBeaconListMenuActivated(boolean menuActivated) {
    this.beaconListMenuActivated = menuActivated;
  }

  private boolean isBeaconListMenuActivated() {
    return this.beaconListMenuActivated;
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private boolean isCommandExpected() {
    return this.commandExpected;
  }

  private SoundUtils getSoundUtils() {
    return this.soundUtils;
  }

  private CountDownLatch getBarrierSpeechRecognizerReady() {
    return this.barrierSpeechRecognizerReady;
  }

}
