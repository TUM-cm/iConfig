package tumcm.droneiot.beacon_management.utils.general;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectSerialization {

  private static final String TAG = ObjectSerialization.class.getSimpleName();
  private final String path;

  public ObjectSerialization(String filename) {
    this.path = AppStorage.STORAGE_PATH + filename;
  }

  public void serialize(Object data) {
    try {
      File file = new File(getPath());
      file.createNewFile();
      FileOutputStream fos = new FileOutputStream(getPath());
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(data);
      oos.close();
      fos.close();
    } catch (IOException e) {
      Log.e(TAG, "serialize Object", e);
    }
  }

  public Object deserialize() {
    try {
      FileInputStream fis = new FileInputStream(getPath());
      ObjectInputStream ois = new ObjectInputStream(fis);
      Object object = ois.readObject();
      ois.close();
      fis.close();
      return object;
    } catch (IOException e) {
      Log.e(TAG, "deserialize Object", e);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "deserialize Object", e);
    }
    return null;
  }

  public boolean checkFileExists() {
    return new File(getPath()).exists();
  }

  private String getPath() {
    return this.path;
  }

}
