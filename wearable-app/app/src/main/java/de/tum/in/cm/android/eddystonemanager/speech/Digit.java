package de.tum.in.cm.android.eddystonemanager.speech;

public enum Digit {

  ZERO("zero", 0),
  ONE("one", 1),
  TWO("two", 2),
  THREE("three", 3),
  FOUR("four", 4),
  FIVE("five", 5),
  SIX("six", 6),
  SEVEN("seven", 7),
  EIGHT("eight", 8),
  NINE("nine", 9);

  private final String word;
  private final int value;

  Digit(String word, int value) {
    this.word = word;
    this.value = value;
  }

  public String getWord() {
    return this.word;
  }

  public int getValue() {
    return this.value;
  }

}
