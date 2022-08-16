// https://stackoverflow.com/a/66366102

package com.shumiproject.saaf.utils;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permission {
	private static final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

	public static boolean checkPermission (Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
    	} else {
      		int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
      		int result1 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
      		return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
		}
	}

	public static void requestPermission (Activity activity) throws Exception {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        	Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
        	intent.addCategory("android.intent.category.DEFAULT");
        	intent.setData(Uri.parse(String.format("package:%s", activity.getPackageName())));
        	activity.startActivityForResult(intent, 1000);
    	} else {
      		ActivityCompat.requestPermissions(activity, permissions, 1000);
    	}
  	}
}