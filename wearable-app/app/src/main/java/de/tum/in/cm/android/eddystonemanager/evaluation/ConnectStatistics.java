package de.tum.in.cm.android.eddystonemanager.evaluation;

import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;

public class ConnectStatistics implements Serializable {

  private static final long serialVersionUID = 6830835357449402343L;

  private final DescriptiveStatistics duration;
  private final Frequency connected;
  private final Frequency authenticated;

  public ConnectStatistics() {
    this.duration = new DescriptiveStatistics();
    this.connected = new Frequency();
    this.authenticated = new Frequency();
  }

  private Frequency getConnected() {
    return this.connected;
  }

  private Frequency getAuthenticated() {
    return this.authenticated;
  }

  public DescriptiveStatistics getDuration() {
    return this.duration;
  }

  public void addDuration(long duration) {
    getDuration().addValue(duration);
  }

  public void addConnected(boolean connected) {
    getConnected().addValue(connected);
  }

  public void addAuthenticated(boolean authenticated) {
    getAuthenticated().addValue(authenticated);
  }

  public double getConnectRate() {
    return getConnected().getPct(true);
  }

  public long getConnectAttempts() {
    return getConnected().getSumFreq();
  }

  public double getAuthenticatedRate() {
    return getAuthenticated().getPct(true);
  }

  public String toString() {
    StringBuilder str = new StringBuilder("mean connect: ");
    str.append(getDuration().getMean());
    str.append(", std connect: ");
    str.append(getDuration().getStandardDeviation());
    str.append(", median connect: " + getDuration().getPercentile(50));
    str.append(", min connect: " + getDuration().getMin());
    str.append(", max connect: " + getDuration().getMax());
    str.append(", connect rate: ");
    str.append(getConnectRate());
    str.append(", authenticated rate: ");
    str.append(getAuthenticatedRate());
    return str.toString();
  }

}
