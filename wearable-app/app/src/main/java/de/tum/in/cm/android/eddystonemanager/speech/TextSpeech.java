package de.tum.in.cm.android.eddystonemanager.speech;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TextSpeech extends Activity implements TextToSpeech.OnInitListener {

  private static String TAG = TextSpeech.class.getSimpleName();
  private final TextToSpeech textToSpeech;
  private final SpeechCallback callback;
  private final Bundle params;

  public TextSpeech(Context context, SpeechCallback callback) {
    this.callback = callback;
    this.textToSpeech = new TextToSpeech(context, this);
    this.params = new Bundle();
    getParams().putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
  }

  @Override
  public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      int result = getEngine().setLanguage(Locale.getDefault());
      if (result == TextToSpeech.LANG_MISSING_DATA ||
              result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Log.e(TAG, "language not supported");
      } else {
        getCallback().onTextToSpeechInit();
      }
    }
  }

  public void shutdown() {
    if (getEngine() != null) {
      getEngine().shutdown();
    }
  }

  public void speak(String message) {
    getEngine().speak(message, TextToSpeech.QUEUE_FLUSH, getParams(), "");
  }

  public TextToSpeech getEngine() {
    return this.textToSpeech;
  }

  private SpeechCallback getCallback() {
    return this.callback;
  }

  private Bundle getParams() {
    return this.params;
  }

}
