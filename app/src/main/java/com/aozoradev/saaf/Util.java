package com.aozoradev.saaf;

import org.ini4j.IniPreferences;
import com.google.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.prefs.Preferences;
import java.io.InputStream;

import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
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
  
  public static void playRadio (Context context, Radio radio) throws IOException {
    MediaPlayer mediaPlayer = new MediaPlayer();
    ZipResourceFile zipFile = null;
    if (zipFile == null) {
      zipFile = new ZipResourceFile(radio.getPath());
      // If zipFile already called, we don't need to call it again
    }
    
    try (AssetFileDescriptor assetFileDescriptor = zipFile.getAssetFileDescriptor(radio.getFileName())) {
      mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor());
      mediaPlayer.prepareAsync();
      
      MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
      LayoutInflater dialogLayoutInflater = LayoutInflater.from(context);
      View dialogView = dialogLayoutInflater.inflate(R.layout.media_player, null);
      TextView _radio = (TextView) dialogView.findViewById(R.id.radio);
      TextView _artist = (TextView) dialogView.findViewById(R.id.artist);
      dialog.setView(dialogView);
      dialog.show();
      
      mediaPlayer.setOnPreparedListener(mp -> {
        _radio.setText(radio.getTitle());
        _artist.setText(radio.getArtist());
        mp.start();
      });
    }
  }
}