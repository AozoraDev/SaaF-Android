package com.shumiproject.saaf.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.shumiproject.saaf.R;

public class AudioPlayer {
    private MediaPlayer player = new MediaPlayer();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private Context context;
    
    // We need to place anotherPlayButton to the top because pause() and play() need it
    private ImageView anotherPlayButton;
    private final int DELAY = 500;
    
    public AudioPlayer(Context context) {
        this.context = context;
    }
    
    public void play(RadioList radioList) throws IOException, IllegalArgumentException {
        try (AssetFileDescriptor assetFileDescriptor = RadioList.osw.getAssetFileDescriptor(radioList.getFilename())) {
            player.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
        	player.prepareAsync();
            
            int logo = (RadioList.stationLogo != 0) ? RadioList.stationLogo : R.drawable.utp;
            
            // TODO Create a class for player bottom sheet
            BottomSheetDialog playerDialog = new BottomSheetDialog(context);
            playerDialog.setContentView(R.layout.bottom);
            
            LinearLayout parent = (LinearLayout) playerDialog.findViewById(R.id.player);
            LinearLayout playButton = (LinearLayout) playerDialog.findViewById(R.id.pause);
            LinearLayout stopButton = (LinearLayout) playerDialog.findViewById(R.id.stop);
            LinearLayout closeButton = (LinearLayout) playerDialog.findViewById(R.id.cancel);
            TextView title = (TextView) playerDialog.findViewById(R.id.title);
            TextView artist = (TextView) playerDialog.findViewById(R.id.artist);
            TextView max = (TextView) playerDialog.findViewById(R.id.max);
            TextView current = (TextView) playerDialog.findViewById(R.id.current);
            ImageView station = (ImageView) playerDialog.findViewById(R.id.station);
            anotherPlayButton = (ImageView) playButton.getChildAt(0);
            SeekBar seekBar = (SeekBar) playerDialog.findViewById(R.id.seekBar);
            
            station.setImageResource(logo);
            title.setText(radioList.getTitle());
            artist.setText(radioList.getArtist());
            
            if (parent.getVisibility() == View.GONE) parent.setVisibility(View.VISIBLE);
            playerDialog.setCanceledOnTouchOutside(false);
            playerDialog.show();
            
            player.setOnPreparedListener(p -> {
                anotherPlayButton.setImageResource(R.drawable.pause_circle);
                seekBar.setMax(p.getDuration());
                max.setText(timerConversion((long) p.getDuration()));
                current.setText(timerConversion((long) p.getCurrentPosition()));
                player.start();
                
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(p.getCurrentPosition());
            			handler.postDelayed(runnable, DELAY);
                    }
                };
                handler.postDelayed(runnable, DELAY);
                
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            			current.setText(timerConversion((long) progress));
    				}
                    
                    @Override
        			public void onStartTrackingTouch(SeekBar seekBar) {
        				handler.removeCallbacks(runnable);
        			}
                    
                    @Override
        			public void onStopTrackingTouch(SeekBar seekBar) {
        				player.seekTo(seekBar.getProgress());
        				handler.postDelayed(runnable, 100);
        			}
                });
                
                playButton.setOnClickListener(v -> {
                    if (player.isPlaying()) pause();
                    else play();
                });
                
                stopButton.setOnClickListener(v -> {
                    seekBar.setProgress(0);
                    player.seekTo(0);
                    if (player.isPlaying()) pause();
                });
                
                closeButton.setOnClickListener(v -> playerDialog.dismiss());
            });
            
            playerDialog.setOnDismissListener(v -> {
                if (player.isPlaying()) player.stop();
                if (parent.getVisibility() == View.VISIBLE) parent.setVisibility(View.GONE);
                reset();
            });
            player.setOnCompletionListener(p -> playerDialog.dismiss());
        }
    }
    
    private void reset() {
        player.reset();
        handler.removeCallbacks(runnable);
    }
    
    public void release() {
        player.reset();
        player.release();
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
        
        // Nullify them
        player = null;
        handler = null;
        runnable = null;
    }
    
    public void pause() {
        if (player != null && player.isPlaying()) {
        	anotherPlayButton.setImageResource(R.drawable.play_circle);
        	handler.removeCallbacks(runnable);
        	player.pause();
        }
    }
    
    private void play() {
        anotherPlayButton.setImageResource(R.drawable.pause_circle);
        handler.postDelayed(runnable, DELAY);
        player.start();
    }
    
    // https://www.11zon.com/zon/android/how-to-play-audio-file-in-android-programmatically.php (Timer Conversion)
	private String timerConversion(long value) {
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
}