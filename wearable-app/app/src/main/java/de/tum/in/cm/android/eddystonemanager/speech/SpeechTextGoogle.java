package de.tum.in.cm.android.eddystonemanager.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

import de.tum.in.cm.android.eddystonemanager.controller.SpeechController;

public class SpeechTextGoogle implements RecognitionListener {

  private static final String LANGUAGE = "en-US";
  private final SpeechRecognizer speechRecognizer;
  private final SpeechCallback callback;
  private final SpeechController speechController;
  private final Intent speechRegonizerIntent;
  private Handler mainHandler;

  public SpeechTextGoogle(Context context, SpeechCallback callback) {
    this.callback = callback;
    this.speechController = (SpeechController) callback;
    this.speechRegonizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    getSpeechRegonizerIntent().putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    getSpeechRegonizerIntent().putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE);
    getSpeechRegonizerIntent().putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
    this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
    getSpeechRecognizer().setRecognitionListener(this);
  }

  public void startListen() {
    if (getMainHandler() != null) {
      getMainHandler().post(new Runnable() {
        @Override
        public void run() {
          getSpeechRecognizer().startListening(getSpeechRegonizerIntent());
          getSpeechController().activateSpeechActivityCountdown();
        }
      });
    }
  }

  public void stopListening() {
    getSpeechRecognizer().stopListening();
  }

  public void shutdown() {
    if (getSpeechRecognizer() != null && getMainHandler() != null) {
      getMainHandler().post(new Runnable() {
        @Override
        public void run() {
          stopListening();
          getSpeechRecognizer().destroy();
        }
      });
    }
  }

  @Override
  public void onReadyForSpeech(Bundle bundle) {}

  @Override
  public void onBeginningOfSpeech() {}

  @Override
  public void onRmsChanged(float rmsdB) {}

  @Override
  public void onBufferReceived(byte[] buffer) {}

  @Override
  public void onEndOfSpeech() {}

  @Override
  public void onError(int error) {
    // No speech input
    if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
      getCallback().onNoInputGoogleSpeechReset();
    }
  }

  @Override
  public void onResults(Bundle results) {
    ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    getCallback().onSpeechResult(data.get(0), true);
  }

  @Override
  public void onPartialResults(Bundle partialResults) {}

  @Override
  public void onEvent(int eventType, Bundle params) {}

  public SpeechRecognizer getSpeechRecognizer() {
    return this.speechRecognizer;
  }

  private SpeechCallback getCallback() {
    return this.callback;
  }

  private SpeechController getSpeechController() {
    return this.speechController;
  }

  private Intent getSpeechRegonizerIntent() {
    return this.speechRegonizerIntent;
  }

  public void setMainHandler(Handler mainHandler) {
    this.mainHandler = mainHandler;
  }

  public Handler getMainHandler() {
    return this.mainHandler;
  }

}
