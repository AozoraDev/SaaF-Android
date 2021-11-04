package com.aozoradev.saaf;

import com.google.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.aozoradev.saaf.constant.Constant;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Environment;
import android.view.View;
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
  
  public static void extract (Context context, Radio radio) throws IOException {
    if (zipFile == null) {
      zipFile = new ZipResourceFile(radio.getPath());
      // If zipFile already called, we don't need to call it again
    }
    File file = new File(Environment.getExternalStorageDirectory().getPath() + "/SaaFAndroid/" + Constant.station);
    if (!file.exists()) {
      file.mkdirs();
    }
    String fn = radio.getFileName();
    
    try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + "/" + fn);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    InputStream is = zipFile.getInputStream(fn)) {
      byte[] bytesIn = new byte[Constant.BUFFER_SIZE];
      int read = 0;
      while ((read = is.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
      }
      Util.toast(context, fn + " has been extracted to " + file.getAbsolutePath());
    }
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
      View dialogView = View.inflate(context, R.layout.media_player, null);
      TextView _radio = (TextView) dialogView.findViewById(R.id.radio);
      TextView _artist = (TextView) dialogView.findViewById(R.id.artist);
      TextView _current = (TextView) dialogView.findViewById(R.id.current);
      TextView _max = (TextView) dialogView.findViewById(R.id.max);
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
        seekBar.setMax(mp.getDuration());
        _max.setText(timerConversion((long) mp.getDuration()));
        _radio.setText(radio.getTitle());
        _artist.setText(radio.getArtist());
        mediaPlayer.start();
        
        runnable = new Runnable() {
          @Override
          public void run() {
            seekBar.setProgress(mp.getCurrentPosition());
            mHandler.postDelayed(runnable, 100);
          }
        };
        mHandler.postDelayed(runnable, 100);
        
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(l -> {
          if (mediaPlayer.isPlaying()) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(context.getString(R.string.play));
            mHandler.removeCallbacks(runnable);
            mediaPlayer.pause();
          } else {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(context.getString(R.string.pause));
            mHandler.postDelayed(runnable, 100);
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
  
  // https://www.11zon.com/zon/android/how-to-play-audio-file-in-android-programmatically.php
  private static String timerConversion(long value) {
    String audioTime;
    int dur = (int) value;
    int hrs = (dur / 3600000);
    int mns = (dur / 60000) % 60000;
    int scs = dur % 60000 / 1000;
    
    if (hrs > 0) {
      audioTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mns, scs);
    } else {
      audioTime = String.format(Locale.getDefault(), "%02d:%02d", mns, scs);
    }
    return audioTime;
  }
}