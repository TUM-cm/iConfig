package de.tum.in.cm.android.eddystonemanager.evaluation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTime {

  private static final String OUTPUT_PATTERN = "yyyy/MM/dd HH:mm:ss";
  private final DateFormat dateFormat;

  public DateTime() {
    this.dateFormat = new SimpleDateFormat(OUTPUT_PATTERN);
  }

  public String getCurrentDateTime(long unixTimestamp) {
    Date date = new Date(unixTimestamp * 1000L);
    return getDateFormat().format(date);
  }

  public long getUnixTimestamp() {
    return (System.currentTimeMillis() / 1000L);
  }

  private DateFormat getDateFormat() {
    return this.dateFormat;
  }

}
