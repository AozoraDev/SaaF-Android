package com.shumiproject.saaf.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.shumiproject.saaf.R;
import com.shumiproject.saaf.utils.RadioList;

public class AudioPlayer {
    private MediaPlayer player;
    private Context context;
    private RadioList radioList;
    private Runnable runnable;
    private Handler handler;
    
    private final int DELAY = 500;
    
    public AudioPlayer(Context context, RadioList radioList) {
        this.context = context;
        this.radioList = radioList;
    }
    
    public void play() throws IOException, IllegalArgumentException {
        player = new MediaPlayer();
        
        try (AssetFileDescriptor assetFileDescriptor = RadioList.osw.getAssetFileDescriptor(radioList.getFilename())) {
            player.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
        	player.prepareAsync();
            
            int logo = (RadioList.stationLogo != 0) ? RadioList.stationLogo : R.drawable.utp;
            
            BottomSheetDialog playerDialog = new BottomSheetDialog(context);
            playerDialog.setContentView(R.layout.player_bottom);
            LinearLayout playButton = (LinearLayout) playerDialog.findViewById(R.id.play);
            LinearLayout stopButton = (LinearLayout) playerDialog.findViewById(R.id.stop);
            LinearLayout closeButton = (LinearLayout) playerDialog.findViewById(R.id.cancel);
            TextView title = (TextView) playerDialog.findViewById(R.id.title);
            TextView artist = (TextView) playerDialog.findViewById(R.id.artist);
            TextView max = (TextView) playerDialog.findViewById(R.id.max);
            TextView current = (TextView) playerDialog.findViewById(R.id.current);
            ImageView station = (ImageView) playerDialog.findViewById(R.id.station);
            ImageView anotherPlayButton = (ImageView) playButton.getChildAt(0);
            SeekBar seekBar = (SeekBar) playerDialog.findViewById(R.id.seekBar);
            
            station.setImageResource(logo);
            title.setText(radioList.getTitle());
            artist.setText(radioList.getArtist());
            
            playerDialog.setCanceledOnTouchOutside(false);
            playerDialog.show();
            
            handler = new Handler();
            
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
                    if (player.isPlaying()) {
                        anotherPlayButton.setImageResource(R.drawable.play_circle);
                        handler.removeCallbacks(runnable);
                        player.pause();
                    } else {
                    	anotherPlayButton.setImageResource(R.drawable.pause_circle);
                        handler.postDelayed(runnable, DELAY);
                        player.start();
                    }
                });
                
                stopButton.setOnClickListener(v -> {
                    anotherPlayButton.setImageResource(R.drawable.play_circle);
                    seekBar.setProgress(0);
                    player.seekTo(0);
                    if (player.isPlaying()) {
                        handler.removeCallbacks(runnable);
                        player.pause();
                    }
                });
                
                closeButton.setOnClickListener(v -> playerDialog.dismiss());
            });
            
            playerDialog.setOnDismissListener(v -> {
                if (player.isPlaying()) player.stop();
                release();
            });
            player.setOnCompletionListener(p -> playerDialog.dismiss());
        }
    }
    
    public void release() {
        player.release();
        player = null;
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
        handler = null;
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