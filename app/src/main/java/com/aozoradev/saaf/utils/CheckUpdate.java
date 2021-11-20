package com.aozoradev.saaf.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Scanner;

import com.aozoradev.saaf.BuildConfig;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class CheckUpdate {
  private JSONObject json;
  private String versionName;
  private int versionCode;
  private Context context;
  private String url;
  
  public CheckUpdate (Context context, String url) throws IOException, JSONException {
    if (checkIfInternetAvailable()) {
      json = readJsonFromUrl(url);
      versionName = json.getString("versionName");
      versionCode = json.getInt("versionCode");
      
      this.context = context;
      this.url = url;
    }
  }
  
  public String getChangelog () {
    String list = null;
    
    try {
      JSONArray changelog = json.getJSONArray("changelog");
      list = changelog.join("\n");
    } catch (JSONException err) {
      err.printStackTrace();
    }
    
    return list;
  }
  
  public String getVersionFull () {
    StringBuilder sb = new StringBuilder();
    sb.append("v" + versionName);
    sb.append(" (" + versionCode + ")");
    
    return sb.toString();
  }
  
  public void check () {
    if (versionName != null) {
      if (!BuildConfig.VERSION_NAME.equals(versionName)) {
        new MaterialAlertDialogBuilder(context)
        .setTitle("SaaF Android " + getVersionFull() + " is available!")
        .setMessage(getChangelog().replaceAll("\"", ""))
        .setPositiveButton("Update", (_dialog, _which) -> {
          Intent intent = new Intent();
          intent.setAction(Intent.ACTION_VIEW);
          intent.addCategory(Intent.CATEGORY_BROWSABLE);
          intent.setData(Uri.parse("https://github.com/Shumi-Project/SaaF-Android/releases/tag/" + versionName));
          
          context.startActivity(intent);
        })
        .setNegativeButton("Later", null)
        .show();
      }
    }
  }
  
  private boolean checkIfInternetAvailable () {
    try {
      URL _url = new URL("https://github.com");
      URLConnection connection = _url.openConnection();
      connection.connect();
      return true;
    } catch (IOException err) {
      return false;
    }
  }
  
  // https://stackoverflow.com/a/28327017
  private String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }
  
  // https://stackoverflow.com/a/28327017
  private JSONObject readJsonFromUrl (String url) throws IOException, JSONException {
    try (InputStream is = new URL(url).openStream()) {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject _json = new JSONObject(jsonText);
      return _json;
    }
  }
}