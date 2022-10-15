package com.shumiproject.saaf.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Arrays;

import com.shumiproject.saaf.R;

public class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.ViewHolder> {

    private File[] list;
    private Callback callback;

    public class ViewHolder extends RecyclerView.ViewHolder {
        
        private TextView textView;
        private ImageView imageView;
        private Context context;
        
        public ViewHolder(Context context, View view) {
            super(view);
            
            textView = (TextView) view.findViewById(R.id.textView);
            imageView = (ImageView) view.findViewById(R.id.imageView);
            this.context = context;
            
            view.setOnClickListener(v -> {
                File file = list[getAbsoluteAdapterPosition()];
                callback.onItemClickedListener(file);
            });
        }
    }
    
    public interface Callback {
        void onItemClickedListener(File file);
    }
    
    public void setOnItemClickedListener (Callback listener) {
        callback = listener;
    }

    public FilePickerAdapter(File[] _list) {
        list = _list;
    }
    
    public void updateList(File[] _list) {
        list = _list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_filepicker, viewGroup, false);
        
        return new ViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Arrays.sort(list);
        File file = list[position];
        
        if (file.isDirectory()) {
            viewHolder.imageView.setImageResource(R.drawable.folder);
        } else if (file.isFile()) {
            viewHolder.imageView.setImageResource(R.drawable.album);
        }
        
        viewHolder.textView.setText(file.getName());
    }

    @Override
    public int getItemCount() {
        return list.length;
    }
}