package com.aozoradev.saaf;

import java.io.IOException;
import android.content.Context;
import org.ini4j.IniPreferences;
import java.util.prefs.Preferences;
import java.io.InputStream;
import android.os.Environment;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

public class Util {
  public static String getStation(Context context, String id, String name) throws IOException {
    InputStream metaStream = null;
    try {
      metaStream = context.getAssets().open("meta.ini");
      Preferences prefs = new IniPreferences(metaStream);
      return prefs.node(id).get(name, null);
    } finally {
      if (metaStream != null) {
        metaStream.close();
      }
    }
  }
  
  public static String getPath(Context context, Uri uri) {
    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            
            // This is for checking Main Memory
            if ("primary".equalsIgnoreCase(type)) {
                if (split.length > 1) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
                } else {
                    return Environment.getExternalStorageDirectory() + "/";
                }
                // This is for checking SD Card
            } else {
                return "storage" + "/" + docId.replace(":", "/");
            }

        }
    }
    return null;
  }
}