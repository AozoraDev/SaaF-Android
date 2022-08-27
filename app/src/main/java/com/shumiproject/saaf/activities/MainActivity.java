package com.shumiproject.saaf.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.os.Looper;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.utils.RadioList;
import com.shumiproject.saaf.utils.AudioPlayer;
import com.shumiproject.saaf.utils.OSW;
import com.shumiproject.saaf.adapters.RadioListAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.OnPermissionCallback;

public class MainActivity extends AppCompatActivity implements OnPermissionCallback {
    private ArrayList<RadioList> radio;
    private Button button;
    private RecyclerView recyclerView;
    private AlertDialog backPressedDialog, loading;
    private Menu menu;
    private boolean canCloseFile;
    private final int DELAY = 200;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize(savedInstanceState);
        letsGo();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_idx:
                try {
                    OSW.createIDX(RadioList.stationPath);
                    Toast.makeText(this, RadioList.stationCode + ".osw.idx created successfully!", Toast.LENGTH_LONG).show();
                } catch (Exception err) {
                    Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                }
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
    }

    @Override
    public void onBackPressed() {
        if (canCloseFile) {
            new MaterialAlertDialogBuilder(this)
            .setCancelable(true)
            .setMessage("Do you want to close " + RadioList.stationName + " station?")
            .setNegativeButton("NO", null)
            .setPositiveButton("YES", (_which, _dialog) -> {
                canCloseFile = false;
                radio.clear();
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
                menu.findItem(R.id.create_idx).setEnabled(false).setVisible(false);
                getSupportActionBar().setSubtitle(null);
                
                // Nullify static vars for no reason
                // RadioList.stationLogo = null; // Can't nullify int
                RadioList.stationName = null;
                RadioList.stationPath = null;
                RadioList.stationCode = null;
                RadioList.osw = null;
            })
            .show();
        } else {
            backPressedDialog.show();
        }
    }
    
    // If everything's sets, just start it
    private void letsGo () {
        if (XXPermissions.isGranted(getApplicationContext(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                Intent intent = new Intent(this, FilePickerActivity.class);
                intent.putExtra("extension", ".osw");
                launcher.launch(intent);
            });
            
            // Don't call permission request if all permissions granted
            return;
        }
    
        // Otherwise, ask permission.
        XXPermissions.with(this)
        .permission(Permission.MANAGE_EXTERNAL_STORAGE)
        .request(this);
    }
    
    private void menuDialog (RadioList radioList) {
        int logo = (RadioList.stationLogo != 0) ? RadioList.stationLogo : R.drawable.utp;
        
        BottomSheetDialog menuSheet = new BottomSheetDialog(this);
        menuSheet.setContentView(R.layout.menu_bottom);
        LinearLayout play = (LinearLayout) menuSheet.findViewById(R.id.play);
        LinearLayout extract = (LinearLayout) menuSheet.findViewById(R.id.extract);
        LinearLayout replace = (LinearLayout) menuSheet.findViewById(R.id.replace);
        ImageView station = (ImageView) menuSheet.findViewById(R.id.station);
        TextView title = (TextView) menuSheet.findViewById(R.id.title);
        TextView artist = (TextView) menuSheet.findViewById(R.id.artist);
        
        station.setImageResource(logo);
        title.setText(radioList.getTitle());
        artist.setText(radioList.getArtist());
        play.setOnClickListener(v -> {
            handler.postDelayed(() -> {
            	try {
                    AudioPlayer player = new AudioPlayer(this, radioList);
                    player.play();
                } catch (Exception err) {
                	Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                }
            	menuSheet.dismiss();
            }, DELAY);
        });
        extract.setOnClickListener(v -> {
            handler.postDelayed(() -> {
            	try {
                	OSW.extract(radioList);
                	String path = Environment.getExternalStorageDirectory().getPath() + "/SaaFAndroid/" + RadioList.stationCode;
                	// Hehe... Don't ask.
                	path += "/";
                
            	    Toast.makeText(this, radioList.getFilename() + " has been extracted to " + path, Toast.LENGTH_LONG).show();
            	} catch (Exception err) {
            	    Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
            	}
                menuSheet.dismiss();
            }, DELAY);
        });
        replace.setOnClickListener(v -> {
            handler.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
                intent.putExtra("extension", ".mp3");
                // I'm not gonna add radiolist to the top, so i'm gonna do a little hack
                intent.putExtra("filename", radioList.getFilename());
                launcher.launch(intent);
                menuSheet.dismiss();
            }, DELAY);
        });
        
        menuSheet.show();
    }

    private void initialize(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button = (Button) findViewById(R.id.button);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        backPressedDialog = new MaterialAlertDialogBuilder(this)
        .setCancelable(true)
        .setNegativeButton("NO", null)
        .setMessage("Are you sure you want to close this app?")
        .setPositiveButton("YES", (_which, _dialog) -> finish())
        .create();
            
        loading = new MaterialAlertDialogBuilder(this)
        .setCancelable(false)
        .setView(View.inflate(this, R.layout.loading, null))
        // Make the background transparent so it's only show loading
        .setBackground(new ColorDrawable(0))
        .create();

        // Someone said using "setHasFixedSize" can optimize the recyclerview.
        recyclerView.setHasFixedSize(true);
        recyclerView.setVisibility(View.GONE);
    }
    
    // Permission handler
    @Override
    public void onGranted(List<String> permissions, boolean all) {
        letsGo();
    }
    
    @Override
    public void onDenied(List<String> permissions, boolean never) {
        new MaterialAlertDialogBuilder(this)
        .setCancelable(false)
        .setMessage("This app requires storage access to work properly. Please grant storage permission.")
        .setPositiveButton("OK", (dialog, which) -> letsGo())
        .show();
    }
    
    // activity with result launcher
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
        int resultCode = result.getResultCode();
        Intent intentResult = result.getData();
        String path = intentResult.getStringExtra("path");
        
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (path.endsWith(".osw")) open(intentResult);
            else if (path.endsWith(".mp3")) replace(intentResult);
        }
    });
    
    // Methods with full of brackets
    // I hate it
    private void open(Intent intent) {
        loading.show();
    
        executor.execute(() -> {
        	try {
                String path = intent.getStringExtra("path");
                String station = intent.getStringExtra("station");
                
                radio = RadioList.createList(this, path, station);
            } catch (Exception err) {
            	handler.post(() -> {
                    if (err.getMessage() == null) Toast.makeText(this, "Error: Failed to open the file", Toast.LENGTH_LONG).show();
                    else Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    
                    loading.dismiss();
                });
                
                // Don't execute the code below if error
                return;
            }
            
            RadioListAdapter adapter = new RadioListAdapter(radio);
            canCloseFile = true; // Set it to true after the osw is loaded so we can close it if onBackPressed executed.
                
            handler.post(() -> {
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
                menu.findItem(R.id.create_idx).setEnabled(true).setVisible(true);
                adapter.setCallback(new RadioListAdapter.Callback() {
                    @Override
                    public void onItemClicked(RadioList radioList) {
                        menuDialog(radioList);
                    }
                    
                    @Override
                    public boolean onItemLongClicked(RadioList radioList) {
                        menuDialog(radioList);
                        return true;
                    }
                });
                getSupportActionBar().setSubtitle(RadioList.stationName);
                
                loading.dismiss();
            });
        });
    }
    
    private void replace (Intent intent) {
        String filename = intent.getStringExtra("filename");
        String path = intent.getStringExtra("path");
         
    	new MaterialAlertDialogBuilder(this)
        .setMessage("Are you sure you want to replace " + filename + "?")
        .setNegativeButton("NO", null)
        .setPositiveButton("YES", (dialog, which) -> {
            loading.show();
            executor.execute(() -> {
    			try {
            		OSW.replace(path, filename);
                	handler.post(() -> {
                        Toast.makeText(this, filename + " have been replaced!", Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    });
                } catch (Exception err) {
            		handler.post(() -> {
                        Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    });
                }
            });
        })
        .show();
    }
}