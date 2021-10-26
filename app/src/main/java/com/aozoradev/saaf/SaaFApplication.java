package com.aozoradev.saaf;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class SaaFApplication extends Application {
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}