package com.aozoradev.saaf;

import androidx.multidex.MultiDex;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Process;
import android.app.AlarmManager;

public class SaaFApplication extends Application {
  public void onCreate() {
    super.onCreate();
    
    Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
    {
      @Override
      public void uncaughtException (Thread thread, Throwable e)
      {
        handleUncaughtException (thread, e);
      }
    });
    
    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("data", Context.MODE_PRIVATE);
    boolean isDarkMode = sharedPref.getBoolean("darkMode", false);
    if (isDarkMode == true) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
  }
  
  private void handleUncaughtException (Thread thread, Throwable e)
  {
    Intent intent = new Intent(getApplicationContext(), UncaughtExceptionActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("error", Log.getStackTraceString(e));
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);
		
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);
		
		Process.killProcess(Process.myPid());
		System.exit(1);
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }
}