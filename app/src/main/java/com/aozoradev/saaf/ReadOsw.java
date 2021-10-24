package com.aozoradev.saaf;

import java.util.ArrayList;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipInputStream;
import android.content.Context;
import org.ini4j.IniPreferences;
import java.util.prefs.Preferences;
import java.io.InputStream;

public class ReadOsw {
  public static void load (Context context, String fileName, ArrayList<String> listItems) throws IOException {
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    ZipInputStream zis = null;
    
    try {
      InputStream metaStream = context.getAssets().open("meta.ini");
      Preferences prefs = new IniPreferences(metaStream);

      fis = new FileInputStream(fileName);
      bis = new BufferedInputStream(fis);
      zis = new ZipInputStream(bis); 
      int index = 0;
      while (zis.getNextEntry() != null) {
          index = ++index;
          String _title = "track" + index + ".title";
          String title = prefs.node("DS").get(_title, null);
          listItems.add(title);
      }
    } finally {
      if (zis != null) {
        zis.close();
      }
      if (bis != null) {
        bis.close();
      }
      if (fis != null) {
        fis.close();
      }
    }
  }
}