package com.aozoradev.saaf.radioplayer;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.aozoradev.saaf.constant.Constant;
import com.aozoradev.saaf.Radio;
import com.aozoradev.saaf.R;

import java.util.Locale;
import java.io.IOException;

import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.content.res.AssetFileDescriptor;
import android.view.View;
import android.media.MediaPlayer;
import android.widget.TextView;
import android.widget.SeekBar;
import android.content.Context;

public class RadioPlayer {
  private static MediaPlayer mediaPlayer;
  private static Runnable runnable;
  private static Handler mHandler;
  
  private static void stop (MediaPlayer mp) {
    mp.stop();
    mp.release();
    mp = null;
  }
  
  // https://www.11zon.com/zon/android/how-to-play-audio-file-in-android-programmatically.php (Timer Conversion)
  private static String timerConversion(long value) {
    String audioTime;
    int dur = (int) value;
    int hrs = (dur / 3600000);
    int mns = (dur / 60000) % 60000;
    int scs = dur % 60000 / 1000;
    
    if (hrs > 0) {
      audioTime = String.format(Locale.US, "%02d:%02d:%02d", hrs, mns, scs);
    } else {
      audioTime = String.format(Locale.US, "%02d:%02d", mns, scs);
    }
    return audioTime;
  }
  
  // Code below is not from 11zon.com, okay? got it? cool kthxcya.
  public static void play (Context context, Radio radio) throws IOException, IllegalArgumentException{
    mediaPlayer = new MediaPlayer();
    
    try (AssetFileDescriptor assetFileDescriptor = Constant.zipFile.getAssetFileDescriptor(radio.getFileName())) {
      mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
      mediaPlayer.prepareAsync();
      
      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
      View dialogView = View.inflate(context, R.layout.media_player, null);
      TextView _radio = (TextView) dialogView.findViewById(R.id.radio);
      TextView _artist = (TextView) dialogView.findViewById(R.id.artist);
      TextView _current = (TextView) dialogView.findViewById(R.id.current);
      TextView _max = (TextView) dialogView.findViewById(R.id.max);
      SeekBar seekBar = (SeekBar) dialogView.findViewById(R.id.seekbar);
      
      builder.setView(dialogView);
      builder.setCancelable(false);
      builder.setTitle(Constant.station);
      builder.setIcon((Constant.stationInt != 0) ? Constant.stationInt : R.drawable.utp);
      builder.setPositiveButton("Close", null);
      builder.setNegativeButton("Pause", null);
      
      AlertDialog dialog = builder.show();
      
      mHandler = new Handler();
      
      mediaPlayer.setOnPreparedListener(mp -> {
        seekBar.setMax(mp.getDuration());
        _max.setText(timerConversion((long) mp.getDuration()));
        _current.setText(timerConversion((long) mp.getCurrentPosition()));
        _radio.setText(radio.getTitle());
        _artist.setText(radio.getArtist());
        mediaPlayer.start();
        
        runnable = new Runnable() {
          @Override
          public void run() {
            seekBar.setProgress(mp.getCurrentPosition());
            mHandler.postDelayed(runnable, 500);
          }
        };
        mHandler.postDelayed(runnable, 500);
        
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(l -> {
          if (mediaPlayer.isPlaying()) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(context.getString(R.string.play));
            mHandler.removeCallbacks(runnable);
            mediaPlayer.pause();
          } else {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(context.getString(R.string.pause));
            mHandler.postDelayed(runnable, 500);
            mediaPlayer.start();
          }
        });
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            _current.setText(timerConversion((long) progress));
          }
          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(runnable);
          }
          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            mp.seekTo(seekBar.getProgress());
            mHandler.postDelayed(runnable, 100);
          }
        });
      });
      
      mediaPlayer.setOnCompletionListener(mp -> {
        dialog.dismiss();
      });
      
      dialog.setOnDismissListener(d -> {
        stop(mediaPlayer);
        mHandler.removeCallbacks(runnable);
      });
    }
  }
}