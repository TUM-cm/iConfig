package de.tum.in.cm.android.eddystonemanager.utils.general;

import java.io.InputStream;

import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;

public class FileUtils {

    public static InputStream getInputStream(int resource) {
        return MainActivity.getInstance().getResources().openRawResource(resource);
    }

    public static String getFileExtension(String filename) {
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i);
        }
        return extension;
    }

}
