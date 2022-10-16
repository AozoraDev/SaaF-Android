package com.shumiproject.saaf.bottomsheet;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shumiproject.saaf.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MenuBottomSheet {
    BottomSheetDialog dialog;
    
    public interface OnClickListener {
        public void onClick(View view);
    }
    
    public MenuBottomSheet(Context context) {
        dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.bottom);
        dialog.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        
        // Just in case
        View player = dialog.findViewById(R.id.player);
        if (player.getVisibility() == View.VISIBLE) player.setVisibility(View.GONE);
    }
    
    public void setTitle(String title) {
        ((TextView) dialog.findViewById(R.id.title)).setText(title);
    }
    
    public void setArtist(String artist) {
        ((TextView) dialog.findViewById(R.id.artist)).setText(artist);
    }
    
    public void setIcon(int icon) {
        ImageView iconView = (ImageView) dialog.findViewById(R.id.station);
        
        if (icon == 0) iconView.setImageResource(R.drawable.utp);
        else iconView.setImageResource(icon);
    }
    
    public void setOnClickListener(OnClickListener callback) {
        LinearLayout menu = (LinearLayout) dialog.findViewById(R.id.menu);
        for (int i = 0; i < menu.getChildCount(); i++) {
            View child = menu.getChildAt(i);
            
            child.setOnClickListener(v -> callback.onClick(v));
        }
    }
    
    public void show() {
        dialog.show();
    }
    
    public void dismiss() {
        dialog.dismiss();
    }
}