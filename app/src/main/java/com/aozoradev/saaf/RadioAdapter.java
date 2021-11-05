package com.aozoradev.saaf;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.aozoradev.saaf.constant.Constant;
import com.aozoradev.saaf.utils.OSWUtil;
import com.aozoradev.saaf.radioplayer.RadioPlayer;

import java.util.List;
import java.io.IOException;

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
        private Context context;
        
        public ViewHolder(Context context, View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            this.context = context;
            itemView.setOnClickListener(v -> clickAndHold());
            itemView.setOnLongClickListener(v -> clickAndHold());
        }
        
        public boolean clickAndHold() {
            int position = getAdapterPosition();
            Radio _radio = mRadio.get(position);

            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this.context);
            View dialogView = View.inflate(this.context, R.layout.custom_title, null);
            TextView mainTitle = (TextView) dialogView.findViewById(R.id.mainTitle);
            TextView subTitle = (TextView) dialogView.findViewById(R.id.subTitle);
            mainTitle.setText(_radio.getTitle());
            subTitle.setText(_radio.getFileName());
            
            dialog.setCustomTitle(dialogView)
            .setItems(Constant.itemsOption, (_dialog, _which) -> {
              switch (_which) {
                case 0:
                  try {
                    RadioPlayer.play(context, _radio);
                  } catch (NullPointerException err) {
                    Toast.makeText(context, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    err.printStackTrace();
                  } catch (IllegalArgumentException err) {
                    Toast.makeText(context, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    err.printStackTrace();
                  } catch (IOException err) {
                    Toast.makeText(context, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    err.printStackTrace();
                  }
                break;
                case 1:
                  try {
                    OSWUtil.extract(context, _radio);
                  } catch (IOException err) {
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
    }

    @Override
    public int getItemCount() {
        return mRadio.size();
    }
}