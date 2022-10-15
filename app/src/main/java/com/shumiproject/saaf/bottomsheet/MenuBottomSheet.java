package com.shumiproject.saaf.bottomsheet;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.shumiproject.saaf.R;

public class MenuBottomSheet {
    private Context context;
    private BottomSheetDialog dialog;
    private OnItemClickListener callback;
    
    public interface OnItemClickListener {
        void onClick(View view);
    }
    
    public MenuBottomSheet(Context context) {
        this.context = context;
        dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.bottom);
    }
    
    public void setTitle(String title) {
        ((TextView) dialog.findViewById(R.id.title)).setText(title);
    }
    
    public void setArtist(String artist) {
        ((TextView) dialog.findViewById(R.id.artist)).setText(artist);
    }
    
    public void setIcon(int icon) {
        ImageView mIcon = (ImageView) dialog.findViewById(R.id.station);
        
        if (icon == 0) mIcon.setImageResource(R.drawable.utp);
        else mIcon.setImageResource(icon);
    }
    
    public void setItems(String[] items, int[] resources, OnItemClickListener callback) {
        this.callback = callback;
        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.layout);
        // Clear all childs to prevent more items
        layout.removeAllViews();
        
        for (int index = 0; index < items.length; index++) {
            // Icon
        	int iconScale = toDp(25);
        	ImageView icon = new ImageView(context);
        	icon.setLayoutParams(new LayoutParams(iconScale, iconScale));
            icon.setImageResource(resources[index]);
            
        	// Text
        	LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        	textParams.setMarginStart(toDp(10));
        	TextView text = new TextView(context);
        	text.setLayoutParams(textParams);
        	text.setTypeface(null, Typeface.BOLD);
            text.setText(items[index]);
            
            // Item
        	TypedValue typedValue = new TypedValue();
        	context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        	int layoutPadding = toDp(12);
        	LinearLayout item = new LinearLayout(context);
        	item.setGravity(Gravity.CENTER_VERTICAL);
        	item.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        	item.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding);
        	item.setBackgroundResource(typedValue.resourceId);
            
            item.addView(icon);
            item.addView(text);
            item.setTag(index);
            item.setOnClickListener(v -> callback.onClick(v));
            
            layout.addView(item);
        }
    }
    
    public void create() {
        dialog.create();
    }
    
    public void show() {
        dialog.show();
    }
    
    public void dismiss() {
        dialog.dismiss();
    }
    
    // https://stackoverflow.com/a/9685690
    private int toDp(int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        int res = (int) (dp * scale + 0.5f);
        
        return res;
    }
}
