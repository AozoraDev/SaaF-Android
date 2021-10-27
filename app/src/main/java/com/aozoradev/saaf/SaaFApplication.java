package com.aozoradev.saaf;

import androidx.multidex.MultiDex;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SaaFApplication extends Application {
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPref.getBoolean("darkMode", false);
        if (isDarkMode == true) {
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}