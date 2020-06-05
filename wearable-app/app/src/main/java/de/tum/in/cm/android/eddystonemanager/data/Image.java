package de.tum.in.cm.android.eddystonemanager.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Image implements Serializable {

  private static final long serialVersionUID = -7484069198084809194L;

  @SerializedName("filename")
  @Expose
  private String filename;

  @SerializedName("image")
  @Expose
  private String data;

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public void setData(String data) {
    this.data = data;
  }

}
