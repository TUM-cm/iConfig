package de.tum.in.cm.android.eddystonemanager.utils.beacon;

public class EddystoneUuid {

  private final String nameSpaceStr;
  private final byte[] nameSpace;

  private final String instanceIdStr;
  private final byte[] instanceId;

  public EddystoneUuid(String nameSpaceStr, byte[] nameSpace, String instanceIdStr, byte[] instanceId) {
    this.nameSpaceStr = nameSpaceStr;
    this.nameSpace = nameSpace;
    this.instanceIdStr = instanceIdStr;
    this.instanceId = instanceId;
  }

  public String getNameSpaceStr() {
    return this.nameSpaceStr;
  }

  public String getInstanceIdStr() {
    return this.instanceIdStr;
  }

  public byte[] getNameSpace() {
    return this.nameSpace;
  }

  public byte[] getInstanceId() {
    return this.instanceId;
  }

}