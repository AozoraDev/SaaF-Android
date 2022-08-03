package com.shumiproject.saaf.activities;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shumiproject.saaf.R;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

// This activity will trigger if there's a uncaught exception.
public class UncaughtExceptionActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get the intent and error log.
		Intent intent = getIntent();
		String err = intent.getStringExtra("error");

		// STRINGGGGG!!!!!!!!!!
		StringBuilder errStr = new StringBuilder();
		errStr.append("PID: " + Process.myPid());
		errStr.append("\n\n");
		errStr.append(err);

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle("Error :(");
		builder.setMessage(errStr.toString());
		builder.setPositiveButton("Close App", (dialog, which) -> {
			Process.killProcess(Process.myPid());
      		System.exit(0);
		});
		builder.setCancelable(false);
		builder.show();
	}
}