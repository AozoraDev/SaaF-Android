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
import java.util.zip.ZipEntry;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import com.anggrayudi.storage.extension.UriUtils;
import com.anggrayudi.storage.file.DocumentFileUtils;

public class Radio {
  private String mTitle;
  private String mArtist;
  private String mFileName;

  public Radio(String title, String artist, String fileName) {
    mTitle = title;
    mArtist = artist;
    mFileName = fileName;
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

  private static int index = 0;

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
    
    try (FileInputStream fis = new FileInputStream(path);
    BufferedInputStream bis = new BufferedInputStream(fis);
    ZipInputStream zis = new ZipInputStream(bis);
    InputStream metaStream = context.getAssets().open("meta.ini")) {
      Preferences prefs = new IniPreferences(metaStream);
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        index = ++index;
        String _title = "track" + index + ".title";
        String _artist = "track" + index + ".artist";
        String title = prefs.node(nodeName).get(_title, null);
        String artist = prefs.node(nodeName).get(_artist, null);
        String _fileName = ze.getName();
        songs.add(new Radio(title, (artist == null ? "-" : artist), _fileName));
      }
      index = 0;
    }
    return songs;
  }
}