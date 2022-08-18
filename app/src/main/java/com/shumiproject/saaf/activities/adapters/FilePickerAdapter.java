package com.shumiproject.saaf.activities.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import com.shumiproject.saaf.R;

public class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.ViewHolder> {

    private File[] list;
    private Callback callback;

    public class ViewHolder extends RecyclerView.ViewHolder {
        
        private TextView textView;
        private ImageView imageView;
        private Context context;
        private View view;
        
        public ViewHolder(Context context, View view) {
            super(view);
            
            textView = (TextView) view.findViewById(R.id.textView);
            imageView = (ImageView) view.findViewById(R.id.imageView);
            
            this.context = context;
            this.view = view;
        }
    }
    
    // Interfacy callbacky thingy
    public interface Callback {
        void onItemClickedListener(File file);
    }
    
    public void setOnItemClickedListener (Callback listener) {
        callback = listener;
    }

    // Initialize the adapter
    public FilePickerAdapter(File[] _list) {
        list = _list;
    }
    
    // Update list
    public void updateList(File[] _list) {
        list = _list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_filepicker, viewGroup, false);
        
        return new ViewHolder(context, view);
    }

    // Replace the contents of a view
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        File file = list[position];
        Resources res = viewHolder.context.getResources();
        Drawable folderLogo = ResourcesCompat.getDrawable(res, R.drawable.folder, null);
        Drawable fileLogo = ResourcesCompat.getDrawable(res, R.drawable.album, null);
        
        if (file.isDirectory()) {
            viewHolder.imageView.setImageDrawable(folderLogo);
        } else if (file.isFile()) {
            viewHolder.imageView.setImageDrawable(fileLogo);
        }
        
        viewHolder.textView.setText(file.getName());
        viewHolder.view.setOnClickListener(v -> callback.onItemClickedListener(file));
    }

    // Return the size of your dataset
    @Override
    public int getItemCount() {
        return list.length;
    }
}