package com.shumiproject.saaf.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.shumiproject.saaf.utils.RadioList;
import com.shumiproject.saaf.R;

public class RadioListAdapter extends RecyclerView.Adapter<RadioListAdapter.ViewHolder> {
    private List<RadioList> mList;
    private Callback callback;
    
    public interface Callback {
        void onItemClicked(RadioList radio);
        boolean onItemLongClicked(RadioList radio);
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView artist;
        private TextView filename;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            artist = (TextView) view.findViewById(R.id.artist);
            filename = (TextView) view.findViewById(R.id.file);
            
            view.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                RadioList radio = mList.get(position);
                callback.onItemClicked(radio);
            });
            view.setOnLongClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                RadioList radio = mList.get(position);
                callback.onItemLongClicked(radio);
                return true;
            });
        }
    }
    
    public void setCallback(Callback listener) {
        callback = listener;
    }

    public RadioListAdapter(List<RadioList> list) {
        mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        RadioList list = mList.get(position);
        
        viewHolder.title.setText(list.getTitle());
        viewHolder.artist.setText(list.getArtist());
        viewHolder.filename.setText(list.getFilename());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}