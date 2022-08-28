package com.shumiproject.saaf.utils;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shumiproject.saaf.BuildConfig;

public class CheckUpdate {
    private static JSONObject json;
    public static String versionName;
    public static boolean isUpdateAvailable;
    
    public static final String releaseURL = "https://github.com/Shumi-Project/SaaF-Android/releases/tag/";
    private static final String url = "https://raw.githubusercontent.com/Shumi-Project/SaaF-Android/rework/changelog.json";
    private static OkHttpClient client = new OkHttpClient();
    
    public static void check() {
        Request request = new Request.Builder()
        .url(url)
        .get()
        .build();
        
        try(Response response = client.newCall(request).execute()) {
        	JSONObject anotherJson = new JSONObject(response.body().string());
            json = anotherJson;
            
            if ((versionName = anotherJson.getString("versionName")) == null) throw new JSONException("Version name not found");
            if (!BuildConfig.VERSION_NAME.equals(versionName)) isUpdateAvailable = true;
        	else isUpdateAvailable = false;
        } catch (Exception err) {
        	isUpdateAvailable = false;
        }
    }
    
	public static String getChangelog() {
        try {
    		JSONArray changelog = json.getJSONArray("changelog");
    		String list = changelog.join("\n");
        
			return list.replaceAll("\"", "");
        } catch (Exception err) {
        	return "-";
        }
	}
}
