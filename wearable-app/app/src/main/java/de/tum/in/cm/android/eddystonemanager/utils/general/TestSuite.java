package de.tum.in.cm.android.eddystonemanager.utils.general;

import de.tum.in.cm.android.eddystonemanager.evaluation.DateTime;

public class TestSuite {

  public static final String EVALUATION_FILENAME = "evaluation.txt";
  public static final String SERIALIZATION_FILENAME = "evaluation.ser";

  private static final DateTime DATE_TIME = new DateTime();

  public static DateTime getDateTime() {
    return DATE_TIME;
  }

}
