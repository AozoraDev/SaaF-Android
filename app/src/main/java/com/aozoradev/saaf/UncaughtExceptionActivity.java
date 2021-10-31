package com.aozoradev.saaf;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

public class UncaughtExceptionActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String err = intent.getStringExtra("error");

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle("Error :(");
		builder.setMessage(err);
		builder.setPositiveButton("Close App", (dialog, which) -> {
			finishAffinity();
			Process.killProcess(Process.myPid());
      System.exit(0);
		});
		builder.setCancelable(false);
		builder.show();
	}
}
