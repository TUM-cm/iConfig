package de.tum.in.cm.android.eddystonemanager.evaluation;

import java.util.Arrays;
import java.util.List;

public enum TestBeacons {

  B1_AEE262B5E0D909D7("AEE262B5E0D909D7", "DE:86:48:4F:43:2D"),
  B2_C05211C2D46B980D("C05211C2D46B980D", "CA:F7:55:BB:61:DD"),
  B3_703D4861B605BB37("703D4861B605BB37", "D6:74:5F:92:4B:EC"),
  B4_83595522A5FE0675("83595522A5FE0675", "FA:5D:54:ED:52:9B"),
  B5_90D709D51FEA2969("90D709D51FEA2969", "D9:88:0C:A4:B9:21"),
  B6_9A9D3264FE5AA78C("9A9D3264FE5AA78C", "CE:13:44:96:CB:83"),
  B7_BB8731A504DACE73("BB8731A504DACE73", "E1:18:7A:32:53:71"),
  B8_ACFAB46E9DE5DC61("ACFAB46E9DE5DC61", "E7:3F:F8:EC:F1:21"),
  B9_C84D592E91C63CC9("C84D592E91C63CC9", "F8:73:10:71:AE:C6"),
  B10_8174205230DE0A7C("8174205230DE0A7C", "CD:BC:62:27:0F:FE");

  private final String sBeaconId;
  private final String mac;

  TestBeacons(String sBeaconId, String mac) {
    this.sBeaconId = sBeaconId;
    this.mac = mac;
  }

  public String getSBeaconId() {
    return this.sBeaconId;
  }

  public String getMac() {
    return this.mac;
  }

  public static final List<String> BEACONS_10 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac(), B3_703D4861B605BB37.getMac(), B4_83595522A5FE0675.getMac(),
          B5_90D709D51FEA2969.getMac(), B6_9A9D3264FE5AA78C.getMac(), B7_BB8731A504DACE73.getMac(),
          B8_ACFAB46E9DE5DC61.getMac(), B9_C84D592E91C63CC9.getMac(), B10_8174205230DE0A7C.getMac());

  public static final List<String> BEACONS_9 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac(), B3_703D4861B605BB37.getMac(), B4_83595522A5FE0675.getMac(),
          B5_90D709D51FEA2969.getMac(), B6_9A9D3264FE5AA78C.getMac(), B7_BB8731A504DACE73.getMac(),
          B8_ACFAB46E9DE5DC61.getMac(), B9_C84D592E91C63CC9.getMac());

  public static final List<String> BEACONS_8 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac(), B3_703D4861B605BB37.getMac(), B4_83595522A5FE0675.getMac(),
          B5_90D709D51FEA2969.getMac(), B6_9A9D3264FE5AA78C.getMac(), B7_BB8731A504DACE73.getMac(),
          B8_ACFAB46E9DE5DC61.getMac());

  public static final List<String> BEACONS_7 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac(), B3_703D4861B605BB37.getMac(), B4_83595522A5FE0675.getMac(),
          B5_90D709D51FEA2969.getMac(), B6_9A9D3264FE5AA78C.getMac(), B7_BB8731A504DACE73.getMac());

  public static final List<String> BEACONS_6 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac(), B3_703D4861B605BB37.getMac(), B4_83595522A5FE0675.getMac(),
          B5_90D709D51FEA2969.getMac(), B6_9A9D3264FE5AA78C.getMac());

  public static final List<String> BEACONS_5 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac(), B3_703D4861B605BB37.getMac(), B4_83595522A5FE0675.getMac(),
          B5_90D709D51FEA2969.getMac());

  public static final List<String> BEACONS_4 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac(), B3_703D4861B605BB37.getMac(), B4_83595522A5FE0675.getMac());

  public static final List<String> BEACONS_3 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac(), B3_703D4861B605BB37.getMac());

  public static final List<String> BEACONS_2 = Arrays.asList(B1_AEE262B5E0D909D7.getMac(),
          B2_C05211C2D46B980D.getMac());

  public static final List<String> BEACONS_1 = Arrays.asList(B1_AEE262B5E0D909D7.getMac());

}
