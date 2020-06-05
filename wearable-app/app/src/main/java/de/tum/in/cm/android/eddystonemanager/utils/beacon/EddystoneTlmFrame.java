package de.tum.in.cm.android.eddystonemanager.utils.beacon;

public final class EddystoneTlmFrame {

  private static final int TLM_MIN_SERVICE_DATA_LEN = 14;

  private final short version;
  private final int batteryVoltage_mV;
  private final double temperature_C;
  private final long advertCount;
  private final long uptime_ds;

  public EddystoneTlmFrame(
      final short version,
      final int batteryVoltage_mV,
      final double temperature_C,
      final long advertCount,
      final long uptime_ds) {
    this.version = version;
    this.batteryVoltage_mV = batteryVoltage_mV;
    this.temperature_C = temperature_C;
    this.advertCount = advertCount;
    this.uptime_ds = uptime_ds;
  }

  public static EddystoneTlmFrame parse(final byte[] frameBytes) {
    if (frameBytes.length < TLM_MIN_SERVICE_DATA_LEN) {
      return null;
    }
    final short version = frameBytes[1];
    final int batteryVoltage = read2Bytes(frameBytes, 2);
    final double temperature = read88fp( frameBytes, 4);
    final long advertCount = read4Bytes( frameBytes, 6);
    final long uptime = read4Bytes(frameBytes, 10);
    return new EddystoneTlmFrame(version, batteryVoltage, temperature, advertCount, uptime);
  }

  public static boolean isTlmFrame(final byte[] bytes) {
    return (((bytes[0] & 0xff) >> 4) == 2);
  }

  public short getVersion() {
    return this.version;
  }

  public int getBatteryVoltage() {
    return this.batteryVoltage_mV;
  }

  public double getTemperature() {
    return this.temperature_C;
  }

  public long getAdvertisedPackets() {
    return this.advertCount;
  }

  public long getUptime() {
    return this.uptime_ds;
  }

  private static long read4Bytes(
      final byte[] buffer,
      final int pos ) {
    long val = ( buffer[ pos     ] & 0xff ) << 24;
        val |= ( buffer[ pos + 1 ] & 0xff ) << 16;
        val |= ( buffer[ pos + 2 ] & 0xff ) << 8;
        val |= ( buffer[ pos + 3 ] & 0xff );
    return val;
  }

  private static int read2Bytes(final byte[] buffer, final int pos) {
    return ((buffer[pos] & 0xff) << 8) |
            ((buffer[pos+1] & 0xff));
  }

  private static double read88fp(
      final byte[] buffer,
      final int pos) {
    final short fixed = (short) read2Bytes(buffer, pos);
    return ((double) fixed) / 256.0;
  }

}
