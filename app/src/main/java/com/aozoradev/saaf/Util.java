package com.aozoradev.saaf;

import org.ini4j.IniPreferences;
import com.google.android.vending.expansion.zipfile.ZipResourceFile;

import java.io.IOException;
import java.util.prefs.Preferences;
import java.io.InputStream;

import android.widget.Toast;
import android.media.MediaPlayer;
import android.content.Context;
import android.content.res.AssetFileDescriptor;

public class Util {
  public static String getStation(Context context, String id) throws IOException {
    try (InputStream metaStream = context.getAssets().open("meta.ini")) {
      Preferences prefs = new IniPreferences(metaStream);
      return prefs.node(id).get("station", null);
    }
  }
  
  public static void toast (Context context, String string) {
    Toast.makeText(context, string, Toast.LENGTH_LONG).show();
  }
  
  public static void playRadio (Context context, String station, String radio) {
    MediaPlayer mediaPlayer = new MediaPlayer();
    AssetFileDescriptor assetFileDescriptor = null;
    
    try {
      ZipResourceFile zipFile = null;
      if (zipFile != null) {
        // Do nothing
      } else {
        zipFile = new ZipResourceFile(station);
      }
      assetFileDescriptor = zipFile.getAssetFileDescriptor(radio);
      
      mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor());
      mediaPlayer.prepare();
      mediaPlayer.start();
      if (assetFileDescriptor != null) {
        assetFileDescriptor.close();
      }
    } catch (IOException err) {
      Util.toast(context, err.getMessage());
      err.printStackTrace();
    }
  }
}