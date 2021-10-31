package com.aozoradev.saaf;

import java.io.IOException;
import android.widget.Toast;
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
  
  public static void toast (Context context, String string) {
    Toast.makeText(context, string, Toast.LENGTH_LONG).show();
  }
}