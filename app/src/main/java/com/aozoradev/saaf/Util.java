package com.aozoradev.saaf;

import com.google.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.aozoradev.saaf.constant.Constant;

import java.io.IOException;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.media.MediaPlayer;
import android.content.Context;
import android.content.res.AssetFileDescriptor;

public class Util {
  private static MediaPlayer mediaPlayer;
  private static ZipResourceFile zipFile;
  private static Runnable runnable;
  private static Handler mHandler;
  
  public static void toast (Context context, String string) {
    Toast.makeText(context, string, Toast.LENGTH_LONG).show();
  }
  
  public static void playRadio (Context context, Radio radio) throws IOException, IllegalArgumentException {
    if (zipFile == null) {
      zipFile = new ZipResourceFile(radio.getPath());
      // If zipFile already called, we don't need to call it again
    }
    mediaPlayer = new MediaPlayer();
    
    try (AssetFileDescriptor assetFileDescriptor = zipFile.getAssetFileDescriptor(radio.getFileName())) {
      mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
      mediaPlayer.prepareAsync();
      
      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
      LayoutInflater dialogLayoutInflater = LayoutInflater.from(context);
      View dialogView = dialogLayoutInflater.inflate(R.layout.media_player, null);
      TextView _radio = (TextView) dialogView.findViewById(R.id.radio);
      TextView _artist = (TextView) dialogView.findViewById(R.id.artist);
      SeekBar seekBar = (SeekBar) dialogView.findViewById(R.id.seekbar);
      
      builder.setView(dialogView);
      builder.setTitle(Constant.station);
      builder.setIcon(ContextCompat.getDrawable(context, R.drawable.utp));
      builder.setPositiveButton("Close", null);
      builder.setNegativeButton("Pause", null);
      
      AlertDialog dialog = builder.show();
      dialog.setCanceledOnTouchOutside(true);
      
      mHandler = new Handler();
      
      mediaPlayer.setOnPreparedListener(mp -> {
        seekBar.setMax(mediaPlayer.getDuration());
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
        seekBar.setProgress(mp.getCurrentPosition());
        mHandler.postDelayed(runnable, 500);
        
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(l -> {
          if (mediaPlayer.isPlaying()) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText("Play");
            mediaPlayer.pause();
          } else {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText("Pause");
            mediaPlayer.start();
          }
        });
      });
      
      mediaPlayer.setOnCompletionListener(mp -> {
        dialog.dismiss();
      });
      
      dialog.setOnDismissListener(d -> {
        stopAudio(mediaPlayer);
        mHandler.removeCallbacks(runnable);
      });
    }
  }
  
  private static void stopAudio (MediaPlayer mp) {
    mp.stop();
    mp.release();
    mp = null;
  }
}