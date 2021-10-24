package com.aozoradev.saaf;

import java.io.IOException;
import android.content.Context;
import org.ini4j.IniPreferences;
import java.util.prefs.Preferences;
import java.io.InputStream;

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
}