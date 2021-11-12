package com.aozoradev.saaf;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.aozoradev.saaf.variables.Constant;
import com.aozoradev.saaf.variables.Static;
import com.aozoradev.saaf.utils.OSWUtil;
import com.aozoradev.saaf.radioplayer.RadioPlayer;

import java.util.List;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;

public class RadioAdapter extends
    RecyclerView.Adapter<RadioAdapter.ViewHolder> {
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView artist;
        public TextView file;
        private Context context;
        
        public ViewHolder(Context context, View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            file = (TextView) itemView.findViewById(R.id.file);
            this.context = context;
            itemView.setOnClickListener(v -> clickAndHold());
            itemView.setOnLongClickListener(v -> clickAndHold());
        }
        
        public boolean clickAndHold() {
            int position = getAdapterPosition();
            Radio _radio = mRadio.get(position);

            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this.context);
            dialog.setTitle(_radio.getTitle())
            .setIcon((Static.stationInt != 0) ? Static.stationInt : R.drawable.utp)
            .setItems(Constant.itemsOption, (_dialog, _which) -> {
              switch (_which) {
                case 0:
                  try {
                    RadioPlayer.play(context, _radio);
                  } catch (Exception err) {
                    Toast.makeText(context, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    err.printStackTrace();
                  }
                break;
                case 1:
                  try {
                    OSWUtil.extract(context, _radio);
                  } catch (Exception err) {
                    Toast.makeText(context, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    err.printStackTrace();
                  }
                break;
              }
            });
            dialog.show();
            return true;
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
        ViewHolder viewHolder = new ViewHolder(context, radioView);
        
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RadioAdapter.ViewHolder holder, int position) {
        Radio radio = mRadio.get(position);

        TextView textView1 = holder.title;
        textView1.setText(radio.getTitle());
        TextView textView2 = holder.artist;
        textView2.setText(radio.getArtist());
        TextView textView3 = holder.file;
        textView3.setText(radio.getFileName());
    }

    @Override
    public int getItemCount() {
        return mRadio.size();
    }
}