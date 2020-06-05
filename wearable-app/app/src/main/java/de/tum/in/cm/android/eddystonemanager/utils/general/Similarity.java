package de.tum.in.cm.android.eddystonemanager.utils.general;

public class Similarity {

    private final double similarity;
    private final int pos;

    public Similarity(double similarity, int pos) {
      this.similarity = similarity;
      this.pos = pos;
    }

    public double getSimilarity() {
      return this.similarity;
    }

    public int getPos() {
      return this.pos;
    }

}