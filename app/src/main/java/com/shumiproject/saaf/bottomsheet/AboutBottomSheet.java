package com.shumiproject.saaf.bottomsheet;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.shumiproject.saaf.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AboutBottomSheet {
    BottomSheetDialog dialog;
    
    public AboutBottomSheet(Context context) {
        dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.about);
        ((TextView) dialog.findViewById(R.id.hyperlinks)).setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    public void show() {
        dialog.show();
    }
}
