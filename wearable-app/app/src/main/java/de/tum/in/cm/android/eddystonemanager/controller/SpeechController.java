package de.tum.in.cm.android.eddystonemanager.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Handler;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.tum.in.cm.android.eddystonemanager.data.BeaconUnregistered;
import de.tum.in.cm.android.eddystonemanager.gui.BeaconRegisterFragment;
import de.tum.in.cm.android.eddystonemanager.services.BeaconDataService;
import de.tum.in.cm.android.eddystonemanager.services.BeaconRegisterService;
import de.tum.in.cm.android.eddystonemanager.speech.SpeechAction;
import de.tum.in.cm.android.eddystonemanager.speech.SpeechCallback;
import de.tum.in.cm.android.eddystonemanager.speech.SpeechTextGoogle;
import de.tum.in.cm.android.eddystonemanager.speech.SpeechTextSphinx;
import de.tum.in.cm.android.eddystonemanager.speech.TextSpeech;
import de.tum.in.cm.android.eddystonemanager.utils.beacon.BeaconUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.GUIUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.ImageUtils;
import de.tum.in.cm.android.eddystonemanager.utils.general.Similarity;
import de.tum.in.cm.android.eddystonemanager.utils.general.SoundUtils;
import info.debatty.java.stringsimilarity.JaroWinkler;

public class SpeechController extends UtteranceProgressListener implements SpeechCallback {

  private static final String TAG = SpeechController.class.getSimpleName();
  public static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
  public static final int DEFAULT_STREAM_VOLUME = 13;
  public static final boolean MAX_VOLUME = false;
  public static final double THRESHOLD_SPEECH_SIMILARITY = 0.5;
  private static final int DEACTIVATE_TIME = 20;
  private static final int LEN_BEACON_ID_REC = 3;
  private static final List<String> BEACON_LIST_MENU = Arrays.asList("target", "select",
          "deactivate", "usage");
  private static final String BEACON_LIST_USAGE = "Say select nearby device or say target";
  private static final List<String> SINGLE_BEACON_MENU = Arrays.asList("identify", "register",
          "list", "deactivate", "usage");
  private static final String SINGLE_BEACON_USAGE = "Say identify or list";
  public static final String CAMERA_USAGE = "Say shot or zoom more, less, min or max";
  private static final String CONFIG_KEYWORD = "geronimo";
  public static final String ACTIVATED = "i Config activated " + BEACON_LIST_USAGE;
  private static final String DEACTIVATED = "i Config deactivated";

  private final ApplicationController applicationController;
  private final BeaconRegisterFragment beaconRegisterFragment;
  private final BeaconDataService beaconDataService;
  private final BeaconRegisterService beaconRegisterService;
  private final TextSpeech textToSpeech;
  private final SoundUtils soundUtils;
  private final SpeechTextSphinx speechTextSphinx;
  private final SpeechTextGoogle speechTextGoogle;
  private final boolean googleSpeechAvailable;
  private final ImageUtils imageUtils;
  private ScheduledFuture<?> speechActivityCountdown;
  private SpeechAction speechAction;
  private boolean registerEnabled;
  private boolean beaconSelected;

  public SpeechController(Context context, ApplicationController applicationController) {
    setVolumeToMax(context, DEFAULT_STREAM_VOLUME, MAX_VOLUME);
    this.applicationController = applicationController;
    this.beaconRegisterFragment = getApplicationController().getBeaconRegisterFragment();
    this.beaconDataService = getApplicationController().getBeaconDataService();
    this.beaconRegisterService = getApplicationController().getBeaconRegisterService();
    this.textToSpeech = new TextSpeech(context, this);
    this.soundUtils = new SoundUtils();
    this.speechTextGoogle = new SpeechTextGoogle(context, this);
    this.speechTextSphinx = new SpeechTextSphinx(context, getBeaconDataService(),
            getSoundUtils(), this, CONFIG_KEYWORD, BEACON_LIST_MENU, SINGLE_BEACON_MENU);
    this.googleSpeechAvailable = getSpeechTextGoogle().getSpeechRecognizer()
            .isRecognitionAvailable(context);
    this.imageUtils = new ImageUtils();
  }

  public void deactivateVoiceControl() {
    if (getSpeechTextGoogle() != null && getSpeechTextGoogle().getMainHandler() == null) {
      getSpeechTextGoogle().setMainHandler(new Handler());
    }
    shutdown();
    getApplicationController().deactivateVoiceControl();
  }

  public void activateSpeechActivityCountdown() {
    this.speechActivityCountdown = getApplicationController().getExecutorService()
            .getScheduledExecutorService().schedule(new Runnable() {
              @Override
              public void run() {
                getBeaconRegisterService().switchToBeaconList(null);
                reset();
              }
            }, DEACTIVATE_TIME, TimeUnit.SECONDS);
  }

  private void setVolumeToMax(Context context, int volume, boolean max) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    if (max) {
      volume = audioManager.getStreamMaxVolume(STREAM_TYPE);
    }
    audioManager.setStreamVolume(STREAM_TYPE, volume, 0);
  }

  private void onSpeechResult(boolean isGoogleSpeechRecognition) {
    if (getSpeechActivityCountdown() != null) {
      stopSpeechActivityCountdown();
      getSpeechTextSphinx().stop();
      getSpeechTextGoogle().stopListening();
      if (!isGoogleSpeechRecognition) {
        getSoundUtils().playBeep();
      }
    }
  }

  @Override
  public void onSingleBeaconMenuResult(String text) {
    onSpeechResult(false);
    switch (text) {
      case "identify": {
        identifyAction();
        break;
      }
      case "register": {
        if (isRegisterEnabled()) {
          speak("Provide additional information to register beacon", null);
          getBeaconImage();
        } else {
          speak("Beacon registration not possible due to missing identification",
                  SpeechAction.FailedIdentify);
        }
        break;
      }
      case "list": {
        setBeaconSelected(false);
        getBeaconRegisterService().switchToBeaconList(null);
        speak("List devices", SpeechAction.List);
        break;
      }
      case "deactivate": {
        getBeaconRegisterService().switchToBeaconList(null);
        reset();
        break;
      }
      case "usage": {
        speak(SINGLE_BEACON_USAGE, SpeechAction.SingleBeaconUsage);
        break;
      }
      default: {
        speak(SINGLE_BEACON_USAGE, SpeechAction.SingleBeaconUsage);
        break;
      }
    }
  }

  @Override
  public void onBeaconListMenuResult(String text) {
    onSpeechResult(false);
    switch (text) {
      case "select": {
        selectAction();
        break;
      }
      case "target": {
        getSpeechTextGoogle().setMainHandler(new Handler());
        askBeaconId();
        break;
      }
      case "deactivate": {
        reset();
        break;
      }
      case "usage": {
        speak(BEACON_LIST_USAGE, SpeechAction.BeaconListUsage);
        break;
      }
      default: {
        speak(BEACON_LIST_USAGE, SpeechAction.BeaconListUsage);
        break;
      }
    }
  }

  @Override
  public void onNoInputGoogleSpeechReset() {
    stopSpeechActivityCountdown();
    getSpeechTextSphinx().stop();
    getSpeechTextGoogle().stopListening();
    if (isBeaconSelected()) {
      getSpeechTextSphinx().activateSingleBeaconMenu(true);
    } else {
        getSpeechTextSphinx().activateBeaconListMenu();
    }
  }

  private void selectAction() {
    if (getBeaconDataService().getBeaconArrayAdapter() != null &&
            getBeaconDataService().getBeaconArrayAdapter().getCount() > 0) {
      BeaconUnregistered beaconUnregistered = getBeaconDataService()
              .getBeaconArrayAdapter().getItem(0);
      if (beaconUnregistered != null) {
        setBeaconSelected(true);
        getApplicationController().switchToBeaconRegister(beaconUnregistered);
        speak(SINGLE_BEACON_USAGE, SpeechAction.SelectBeacon);
      }
    }
  }

  private void targetAction(String targetSBeaconId) {
    List<BeaconUnregistered> unregisteredBeacons = getBeaconDataService()
            .getBeaconArrayAdapter().getBeacons();
    JaroWinkler jaroWinkler = new JaroWinkler();
    int size = unregisteredBeacons.size();
    List<Similarity> beacons = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      String sBeaconId = unregisteredBeacons.get(i).getSBeaconId();
      if (sBeaconId != null) {
        sBeaconId = BeaconUtils.restrictToDigits(sBeaconId, false).substring(0, LEN_BEACON_ID_REC);
        double similarity = jaroWinkler.similarity(sBeaconId, targetSBeaconId);
        beacons.add(new Similarity(similarity, i));
      }
    }
    Collections.sort(beacons, BeaconUtils.SIMILARITY_COMPARATOR);
    Similarity mostSimilarBeacon = beacons.get(0);
    if (mostSimilarBeacon.getSimilarity() >= THRESHOLD_SPEECH_SIMILARITY) {
      setBeaconSelected(true);
      getApplicationController().switchToBeaconRegister(
              unregisteredBeacons.get(mostSimilarBeacon.getPos()));
      speak(SINGLE_BEACON_USAGE, SpeechAction.TargetBeacon);
    } else {
      speak("Did not found similar beacon i d", SpeechAction.FailedTarget);
    }
  }

  private void identifyAction() {
    speak("Please wait, try to identify beacon", null);
    getBeaconRegisterService().identifyAction();
  }

  private void registerAction() {
    speak("Please wait, try to register beacon", null);
    getBeaconRegisterService().registerAction();
  }

  @Override
  public void onSpeechResult(String text, boolean isGoogleSpeechRecognition) {
    onSpeechResult(isGoogleSpeechRecognition);
    switch (getSpeechAction()) {
      case Target: {
        text = BeaconRegisterService.preprocessTarget(text);
        targetAction(text);
        break;
      }
      case ConfirmIdentify: {
        if (text.contains("yes")) {
          setRegisterEnabled(true);
          GUIUtils.getIdentifyDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();
          speak("Beacon identification was successful. Say register or list.", SpeechAction.Identify);
        } else if (text.contains("no")) {
          setRegisterEnabled(false);
          GUIUtils.getIdentifyDialog().getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
          speak("Beacon identification was not successful. Registration is not possible. Retry or list.",
                  SpeechAction.FailedIdentify);
        }
        break;
      }
      case NearestRoom: {
        text = BeaconRegisterService.preprocessRoom(text, isGoogleSpeechRecognition);
        if (BeaconRegisterService.isValidRoom(text)) {
          getBeaconDataService().getActiveBeacon().getConfig().setNearestRoom(text);
          getBeaconRegisterFragment().getNearbyRoomField().setText(text);
          getLocationDescription();
        } else {
          getNearestRoom();
        }
        break;
      }
      case LocationDescription: {
        if (BeaconRegisterService.isValidLocationDescription(text)) {
          getBeaconDataService().getActiveBeacon().getConfig().setLocationDescription(text);
          getBeaconRegisterFragment().getLocationDescriptionField().setText(text);
          registerAction();
        } else {
          getLocationDescription();
        }
        break;
      }
      case Camera: {
        if (text.contains("shot")) {
          getApplicationController().getCameraFragment().takePicture();
        } else if (text.contains("zoom")) {
          getApplicationController().getCameraFragment().zoom(text);
          getSpeechTextSphinx().activateCameraMenu();
        }
        break;
      }
      case ConfirmPicture: {
        if (text.contains("yes")) {
          getApplicationController().getCameraFragment().close();
          File image = getApplicationController().getCameraFragment().getImageFile();
          getImageUtils().inPlaceAdjustImage(image);
          File imageFile = getApplicationController().getCameraFragment().getImageFile();
          getBeaconRegisterService().getBeaconDataService().getActiveBeacon()
                  .setPlaceImage(imageFile);
          speak("Picture saved", SpeechAction.PictureSaved);
        } else if (text.contains("no")) {
          getApplicationController().getCameraFragment().reopen();
          speak(SpeechController.CAMERA_USAGE, SpeechAction.Camera);
        }
        break;
      }
    }
  }

  @Override
  public void onTextToSpeechInit() {
    getTextToSpeech().getEngine().setOnUtteranceProgressListener(this);
    getSpeechTextSphinx().activate();
  }

  // UtteranceProgressListener: start speech recognition after sound output
  @Override
  public void onStart(String utteranceId) {}

  @Override
  public void onDone(String utteranceId) {
    if (getSpeechAction() != null) {
      switch (getSpeechAction()) {
        case SpeechActivated: {
          getSpeechTextSphinx().reset();
          break;
        }
        case Target: {
          if (isGoogleSpeechAvailable()) {
            getSpeechTextGoogle().startListen();
          } else {
            getSpeechTextSphinx().activateTarget();
          }
          break;
        }
        case FailedTarget: {
          askBeaconId();
          break;
        }
        case Register:
        case List:
        case BeaconListMenuActivated:
        case BeaconListUsage: {
          getSpeechTextSphinx().activateBeaconListMenu();
          break;
        }
        case Identify:
        case FailedIdentify:
        case SingleBeaconUsage: {
          getSpeechTextSphinx().activateSingleBeaconMenu(true);
          break;
        }
        case NearestRoom: {
          if (isGoogleSpeechAvailable()) {
            getSpeechTextGoogle().startListen();
          } else {
            getSpeechTextSphinx().activateRoom();
          }
          break;
        }
        case ConfirmIdentify: {
          getSpeechTextSphinx().activateConfirm();
          break;
        }
        case LocationDescription:
          if (isGoogleSpeechAvailable()) {
            getSpeechTextGoogle().startListen();
          } else {
            getSpeechTextSphinx().activateGenericInput();
          }
          break;
        case Camera: {
          getSpeechTextSphinx().activateCameraMenu();
          break;
        }
        case ConfirmPicture: {
          getSpeechTextSphinx().activateConfirm();
          break;
        }
        case PictureSaved: {
          getBeaconRegisterService().getSpeechController().getNearestRoom();
          break;
        }
        case SelectBeacon:
        case TargetBeacon: {
          getSpeechTextSphinx().activateSingleBeaconMenu(true);
          break;
        }
      }
    }
  }

  @Override
  public void onError(String utteranceId) {}

  private void askBeaconId() {
    ask("Say first three digits of beacon i d", SpeechAction.Target);
  }

  private void speechAction(String question, SpeechAction speechAction) {
    setSpeechAction(speechAction);
    getTextToSpeech().speak(question);
  }

  private void ask(String question, SpeechAction speechAction) {
    speechAction(question, speechAction);
  }

  public void speak(String explanation, SpeechAction speechAction) {
    speechAction(explanation, speechAction);
  }

  private void getBeaconImage() {
    speak("Take a picture of the beacon placement", null);
    getSoundUtils().playBeep();
    getBeaconRegisterFragment().dispatchTakePictureIntent();
  }

  public void getNearestRoom() {
    ask("What is the nearest room", SpeechAction.NearestRoom);
  }

  private void getLocationDescription() {
    ask("What is the location description", SpeechAction.LocationDescription);
  }

  public void shutdown() {
    try {
      getTextToSpeech().shutdown();
      getSpeechTextSphinx().shutdown();
      getSpeechTextGoogle().shutdown();
    } catch (Exception e) {
      Log.e(TAG, "error on shutdown", e);
    }
  }

  public void reset() {
    setBeaconSelected(false);
    speak(DEACTIVATED, null);
    getSpeechTextGoogle().stopListening();
    getSpeechTextSphinx().reset();
  }

  public SpeechTextGoogle getSpeechTextGoogle() {
    return this.speechTextGoogle;
  }

  public TextSpeech getTextToSpeech() {
    return this.textToSpeech;
  }

  public void setSpeechAction(SpeechAction speechAction) {
    this.speechAction = speechAction;
  }

  public void stopSpeechActivityCountdown() {
    getSpeechActivityCountdown().cancel(true);
  }

  public boolean isBeaconSelected() {
    return this.beaconSelected;
  }

  private ScheduledFuture<?> getSpeechActivityCountdown() {
    return this.speechActivityCountdown;
  }

  private SoundUtils getSoundUtils() {
    return this.soundUtils;
  }

  private SpeechAction getSpeechAction() {
    return this.speechAction;
  }

  private void setRegisterEnabled(boolean registerEnabled) {
    this.registerEnabled = registerEnabled;
  }

  private boolean isRegisterEnabled() {
    return this.registerEnabled;
  }

  private void setBeaconSelected(boolean beaconSelected) {
    this.beaconSelected = beaconSelected;
  }

  private SpeechTextSphinx getSpeechTextSphinx() {
    return this.speechTextSphinx;
  }

  private ApplicationController getApplicationController() {
    return this.applicationController;
  }

  private BeaconRegisterFragment getBeaconRegisterFragment() {
    return this.beaconRegisterFragment;
  }

  private BeaconDataService getBeaconDataService() {
    return this.beaconDataService;
  }

  private BeaconRegisterService getBeaconRegisterService() {
    return this.beaconRegisterService;
  }

  private boolean isGoogleSpeechAvailable() {
    return this.googleSpeechAvailable;
  }

  private ImageUtils getImageUtils() {
    return this.imageUtils;
  }

}
