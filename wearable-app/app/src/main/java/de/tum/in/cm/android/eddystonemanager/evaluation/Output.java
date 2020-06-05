package de.tum.in.cm.android.eddystonemanager.evaluation;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import de.tum.in.cm.android.eddystonemanager.utils.app.AppStorage;

public class Output extends DateTime {

  private static final String TAG = Output.class.getSimpleName();
  private final File file;
  private PrintWriter printWriter;

  public Output(String filename) {
    String path = AppStorage.STORAGE_PATH + filename;
    this.file = new File(path);
    try {
      getFile().createNewFile();
      boolean append = true;
      boolean autoFlush = true;
      this.printWriter = new PrintWriter(new FileWriter(getFile(), append), autoFlush);
    } catch (IOException e) {
      Log.e(TAG, "create output file", e);
    }
  }

  public void write(String line) {
    getWriter().println(line);
  }

  public void writeCsv(float value) {
    getWriter().print(value);
    getWriter().print(",");
  }
  
  public void write(long timestamp, String data) {
    String line = getCurrentDateTime(timestamp) + " " + data;
    write(line);
  }

  public void close() {
   getWriter().close();
  }

  private PrintWriter getWriter() {
    return this.printWriter;
  }

  private File getFile() {
    return this.file;
  }

}
