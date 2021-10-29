package com.aozoradev.saaf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

public class UncaughtExceptionActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String madeErrorMessage = intent.getStringExtra("error");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Error LMAO");
		builder.setMessage(madeErrorMessage);
		builder.setPositiveButton("Close App", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finishAffinity();
				Process.killProcess(Process.myPid());
        System.exit(0);
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}
}
