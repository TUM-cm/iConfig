package de.tum.in.cm.android.eddystonemanager.gui;

import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageSaver implements Runnable {

  private final Image image;
  private final File imageFile;
  private final ImageCallback imageCallback;

  public ImageSaver(Image image, File imageFile, ImageCallback imageCallback) {
    this.image = image;
    this.imageFile = imageFile;
    this.imageCallback = imageCallback;
  }

  @Override
  public void run() {
    ByteBuffer buffer = getImage().getPlanes()[0].getBuffer();
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(getImageFile());
      output.write(bytes);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      getImage().close();
      if (null != output) {
        getImageCallback().onImageSaved();
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private Image getImage() {
    return this.image;
  }

  private File getImageFile() {
    return this.imageFile;
  }

  private ImageCallback getImageCallback() {
    return this.imageCallback;
  }

}