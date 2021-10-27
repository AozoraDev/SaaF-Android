package com.aozoradev.saaf;

import androidx.multidex.MultiDex;
import android.app.Application;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

public class SaaFApplication extends Application {
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}