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
import com.anggrayudi.storage.file.DocumentFileCompat;

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

    FileInputStream fis = null;
    BufferedInputStream bis = null;
    ZipInputStream zis = null;
    InputStream metaStream = null;
    
    // Get path bullshit
    DocumentFile df = DocumentFileCompat.fromUri(context, uri);
    String path = DocumentFileUtils.getAbsolutePath(df, context);
    
    try {
      fis = new FileInputStream(path);
      bis = new BufferedInputStream(fis);
      zis = new ZipInputStream(bis);
      metaStream = context.getAssets().open("meta.ini");
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
    } finally {
      index = 0;
      if (metaStream != null) {
        metaStream.close();
      }
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
    return songs;
  }
}