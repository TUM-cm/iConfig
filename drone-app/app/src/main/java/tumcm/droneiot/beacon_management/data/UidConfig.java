package tumcm.droneiot.beacon_management.data;

import com.bluvision.beeks.sdk.helper.EncryptUtilis;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UidConfig implements Serializable {

  private static final long serialVersionUID = 421998319074139251L;

  @SerializedName("namespace")
  @Expose
  private String namespace;

  @SerializedName("instance")
  @Expose
  private String instance;

  public byte[] getNamespace() {
    return EncryptUtilis.hexString2b(this.namespace);
  }

  public byte[] getInstance() {
    return EncryptUtilis.hexString2b(this.instance);
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

}
