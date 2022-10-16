package com.shumiproject.saaf.bottomsheet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.shumiproject.saaf.BuildConfig;
import com.shumiproject.saaf.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AboutBottomSheet {
    BottomSheetDialog dialog;
    Context context;
    
    // Visit us!
    final String GITHUB = "https://github.com/Shumi-Project/SaaF-Android/";
    final String WEBSITE = "https://shumi-project.github.io/";
    final String DISCORD = "https://discord.gg/aHHVRu7fKZ";
    
    public AboutBottomSheet(Context context) {
        this.context = context;
        dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.about);
        
        String moreInfo = context.getResources().getString(R.string.more_info);
        ((TextView) dialog.findViewById(R.id.more)).setText(String.format(moreInfo, BuildConfig.VERSION_NAME, System.getProperty("os.arch")));
        ((TextView) dialog.findViewById(R.id.hyperlinks)).setMovementMethod(LinkMovementMethod.getInstance()); // Make the hyperlink clickable
        
        dialog.findViewById(R.id.github).setOnClickListener(v -> open(GITHUB));
        dialog.findViewById(R.id.website).setOnClickListener(v -> open(WEBSITE));
        dialog.findViewById(R.id.discord).setOnClickListener(v -> open(DISCORD));
    }
    
    public void create() {
        dialog.create();
    }
    
    public void show() {
        dialog.show();
    }
    
    private void open(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }
}
