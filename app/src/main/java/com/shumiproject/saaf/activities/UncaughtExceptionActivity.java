package com.shumiproject.saaf.activities;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shumiproject.saaf.R;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;

// This activity will trigger if there's a uncaught exception.
public class UncaughtExceptionActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get the intent and error log.
		Intent intent = getIntent();
		String error = intent.getStringExtra("error");

		String err = "PID: " + Process.myPid();
        err += "\nDevice Name: " + Build.MODEL;
        err += "\nAndroid Version: " + Build.VERSION.RELEASE;
        err += "\n\n" + error;

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle("Error :(");
		builder.setMessage(err);
		builder.setPositiveButton("Close App", (dialog, which) -> {
			Process.killProcess(Process.myPid());
    		System.exit(1);
		});
		builder.setCancelable(false);
		builder.show();
	}
}