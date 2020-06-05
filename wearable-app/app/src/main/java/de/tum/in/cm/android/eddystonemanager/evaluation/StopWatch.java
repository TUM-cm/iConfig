package de.tum.in.cm.android.eddystonemanager.evaluation;

import android.os.SystemClock;

public class StopWatch {

  private static final String TAG = StopWatch.class.getSimpleName();
  private final String description;
  private long startTime;
  private long stopTime;
  private boolean running;

  public StopWatch(String description) {
    this.description = description;
    this.startTime = 0;
    this.stopTime = 0;
    this.running = false;
  }

  public void start() {
    this.startTime = SystemClock.elapsedRealtime();
    //Log.d(TAG, getDescription() + ", start: " + startTime);
    this.running = true;
  }

  public void stop() {
    this.stopTime = SystemClock.elapsedRealtime();
    //Log.d(TAG, getDescription() + ", stop: " + stopTime);
    this.running = false;
  }

  public long getElapsedTime() {
    long elapsed;
    if (running) {
      elapsed = (SystemClock.elapsedRealtime() - startTime);
    } else {
      elapsed = (stopTime - startTime);
    }
    return elapsed;
  }

  private String getDescription() {
    return this.description;
  }

}