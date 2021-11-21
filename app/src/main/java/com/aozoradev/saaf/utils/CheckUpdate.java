package com.aozoradev.saaf.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.IOException;

import com.aozoradev.saaf.BuildConfig;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class CheckUpdate {
  private JSONObject json;
  private String versionName;
  private int versionCode;
  private Context context;
  private String url;
  private String list;
  
  public CheckUpdate (Context context, String url) throws IOException, JSONException {
    json = readJson(url);
    
    versionName = json.getString("versionName");
    versionCode = json.getInt("versionCode");
    this.context = context;
    this.url = url;
  }
  
  public String getChangelog () {
    try {
      JSONArray changelog = json.getJSONArray("changelog");
      list = changelog.join("\n");
    } catch (JSONException err) {
      err.printStackTrace();
    }
    
    return list.replaceAll("\"", "");
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
        .setMessage(getChangelog())
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
  
  private JSONObject readJson (String url) throws IOException, JSONException {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(url).get().build();
    Response response = client.newCall(request).execute();
    JSONObject _json = new JSONObject(response.body().string());
    
    response.close();
    return _json;
  }
}