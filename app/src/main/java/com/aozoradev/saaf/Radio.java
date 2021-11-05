package com.aozoradev.saaf;

import android.content.Context;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipInputStream;
import java.util.prefs.Preferences;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import org.ini4j.IniPreferences;
import com.google.android.vending.expansion.zipfile.ZipResourceFile;
import com.anggrayudi.storage.extension.UriUtils;
import com.anggrayudi.storage.file.DocumentFileUtils;
import com.aozoradev.saaf.constant.Constant;

public class Radio {
  private String mTitle;
  private String mArtist;
  private String mFileName;
  private String mPath;

  public Radio(String title, String artist, String fileName, String path) {
    mTitle = title;
    mArtist = artist;
    mFileName = fileName;
    mPath = path;
  }

  public String getTitle() {
    return mTitle;
  }

  public String getArtist() {
    return mArtist;
  }
  
  public String getFileName() {
    return mFileName;
  }
  
  public String getPath() {
    return mPath;
  }

  public static ArrayList < Radio > createRadioList(Context context, Uri uri, String nodeName) throws IOException {
    ArrayList < Radio > songs = new ArrayList < Radio > ();
    
    // Get path bullshit
    String path = null;
    if(UriUtils.isExternalStorageDocument(uri)) {
      DocumentFile df = UriUtils.toDocumentFile(uri, context);
      path = DocumentFileUtils.getAbsolutePath(df, context);
    } else {
      path = uri.getPath();
    }
    Preferences prefs = null;
    
    try (FileInputStream fis = new FileInputStream(path);
    BufferedInputStream bis = new BufferedInputStream(fis);
    ZipInputStream zis = new ZipInputStream(bis);
    InputStream metaStream = context.getAssets().open("meta.ini")) {
      prefs = new IniPreferences(metaStream);
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        String _fileName = ze.getName();
        int index = Integer.parseInt(_fileName.replaceAll(".mp3", "").replaceAll("[^0-9]", ""));
        String title = prefs.node(nodeName).get("track" + index + ".title", null);
        String artist = prefs.node(nodeName).get("track" + index + ".artist", null);
        songs.add(new Radio(title, (artist == null ? "-" : artist), _fileName, path));
      }
      
      // Add some data to Constant
      Constant.zipFile = new ZipResourceFile(path);
      Constant.station = prefs.node(nodeName).get("station", null);
      Constant.stationInt = context.getResources().getIdentifier(nodeName.toLowerCase(), "drawable", context.getPackageName());
    }
    return songs;
  }
}