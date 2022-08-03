package com.shumiproject.saaf.utils;

import android.widget.Toast;
import android.content.Context;

public class ToastUtils {
	public static void errorToast (Context context, Throwable throwable) {
		Toast.makeText(context, "Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
	}
}