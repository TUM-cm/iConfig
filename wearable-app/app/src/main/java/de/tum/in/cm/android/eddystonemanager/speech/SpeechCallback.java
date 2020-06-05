package de.tum.in.cm.android.eddystonemanager.speech;

public interface SpeechCallback {

  void onBeaconListMenuResult(String text);
  void onSingleBeaconMenuResult(String text);
  void onSpeechResult(String text, boolean isGoogleSpeechRecognition);
  void onTextToSpeechInit();
  void onNoInputGoogleSpeechReset();

}
