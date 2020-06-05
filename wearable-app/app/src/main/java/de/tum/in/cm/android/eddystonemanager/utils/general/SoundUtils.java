package de.tum.in.cm.android.eddystonemanager.utils.general;

import android.media.ToneGenerator;

import de.tum.in.cm.android.eddystonemanager.controller.SpeechController;

public class SoundUtils {

  private static final int VOLUME = 100;
  private static final int BEEP_DURATION = 105; // ms

  private final ToneGenerator toneGenerator;

  public SoundUtils() {
    this.toneGenerator = new ToneGenerator(SpeechController.STREAM_TYPE, VOLUME);
  }

  public void playBeep() {
    getToneGenerator().startTone(ToneGenerator.TONE_CDMA_PIP, BEEP_DURATION);
  }

  private ToneGenerator getToneGenerator() {
    return this.toneGenerator;
  }

}
