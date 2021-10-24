package com.aozoradev.saaf;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import java.util.List;
import android.view.ViewGroup;
import android.content.Context;
import android.view.LayoutInflater;
import com.aozoradev.saaf.Radio;

public class RadioAdapter extends
    RecyclerView.Adapter<RadioAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView artist;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
        }
    }
    
    private List<Radio> mRadio;
    public RadioAdapter(List<Radio> radio) {
        mRadio = radio;
    }
    
    @Override
    public RadioAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View radioView = inflater.inflate(R.layout.list_adapter, parent, false);
        ViewHolder viewHolder = new ViewHolder(radioView);
        
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RadioAdapter.ViewHolder holder, int position) {
        Radio radio = mRadio.get(position);

        TextView textView1 = holder.title;
        textView1.setText(radio.getTitle());
        TextView textView2 = holder.artist;
        textView2.setText(radio.getArtist());
    }

    @Override
    public int getItemCount() {
        return mRadio.size();
    }
}