package com.shumiproject.saaf;

import com.shumiproject.saaf.activities.UncaughtExceptionActivity;

import android.app.Application;
import android.util.Log;
import android.content.Intent;

public class SaaFApplication extends Application {
	public void onCreate() {
    	super.onCreate();
    	Thread.setDefaultUncaughtExceptionHandler((thread, err) -> handleUncaughtException(err));
	}
  
	private void handleUncaughtException (Throwable err) {
		Intent intent = new Intent(getApplicationContext(), UncaughtExceptionActivity.class);
		intent.putExtra("error", Log.getStackTraceString(err));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intent);
        
        System.exit(1);
	}
}