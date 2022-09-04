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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.os.Looper;
import android.net.Uri;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.utils.*;
import com.shumiproject.saaf.adapters.RadioListAdapter;
import com.shumiproject.saaf.bottomsheet.MenuBottomSheet;
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
    private Resources res;
    private boolean canCloseFile;
    
    private final int DELAY = 200;
    private final String[] items = { "Play", "Extract", "Replace" };
    private final int[] resources = { R.drawable.play, R.drawable.download, R.drawable.refresh };
    
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
                    Toast.makeText(this, String.format(res.getString(R.string.idx_created), RadioList.stationCode), Toast.LENGTH_LONG).show();
                } catch (Exception err) {
                    Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                }
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        AudioPlayer.pause();
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
            .setMessage(String.format(res.getString(R.string.close_station), RadioList.stationName))
            .setNegativeButton(res.getString(R.string.no), null)
            .setPositiveButton(res.getString(R.string.yes), (_which, _dialog) -> {
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
        } else backPressedDialog.show();
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
        .setMessage(res.getString(R.string.storage_permission))
        .setPositiveButton(res.getString(R.string.ok), (dialog, which) -> letsGo())
        .show();
    }
    
    // If everything's sets, just start it
    private void letsGo () {
        if (XXPermissions.isGranted(getApplicationContext(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            // Only check update if all permissions are granted.
            checkUpdate();
            
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

    private void initialize(Bundle savedInstanceState) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        res = getApplicationContext().getResources();
        button = (Button) findViewById(R.id.button);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        setSupportActionBar(toolbar);
        
        backPressedDialog = new MaterialAlertDialogBuilder(this)
        .setCancelable(true)
        .setNegativeButton(res.getString(R.string.no), null)
        .setMessage(res.getString(R.string.close_app))
        .setPositiveButton(res.getString(R.string.yes), (_which, _dialog) -> finish())
        .create();
        
        loading = new MaterialAlertDialogBuilder(this)
        .setCancelable(false)
        .setView(View.inflate(this, R.layout.loading, null))
        .create();
        
        // Someone said using "setHasFixedSize" can optimize the recyclerview.
        recyclerView.setHasFixedSize(true);
        recyclerView.setVisibility(View.GONE);
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
        String path = intent.getStringExtra("path");
        String station = intent.getStringExtra("station");
        
        loading.show();
        ((TextView) loading.findViewById(R.id.loadingText)).setText(String.format(res.getString(R.string.loading_osw), station));
    
        executor.execute(() -> {
        	try {
                radio = RadioList.createList(this, path, station);
            } catch (Exception err) {
            	handler.post(() -> {
                	Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    loading.dismiss();
                });
                
                // Don't execute the code below if error happens
                return;
            }
        
        	RadioListAdapter adapter = new RadioListAdapter(radio);
        	canCloseFile = true; // Set it to true after the osw is loaded so we can close it if onBackPressed executed.
        
        	handler.post(() -> {
        		MenuBottomSheet menuBottomSheet = new MenuBottomSheet(this);
        		button.setVisibility(View.GONE);
        		menu.findItem(R.id.create_idx).setEnabled(true).setVisible(true);
        		recyclerView.setAdapter(adapter);
        		recyclerView.setLayoutManager(new LinearLayoutManager(this));
        		recyclerView.setVisibility(View.VISIBLE);
        		adapter.setCallback(new RadioListAdapter.Callback() {
        			@Override
        		    public void onItemClicked(RadioList radioList) {
        		    	menuDialog(menuBottomSheet, radioList);
        			}
            		
        		    @Override
        		    public boolean onItemLongClicked(RadioList radioList) {
        		        menuDialog(menuBottomSheet, radioList);
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
        .setMessage(String.format(res.getString(R.string.confirm_replace), filename))
        .setNegativeButton(res.getString(R.string.no), null)
        .setPositiveButton(res.getString(R.string.yes), (dialog, which) -> {
            loading.show();
            ((TextView) loading.findViewById(R.id.loadingText)).setText(String.format(res.getString(R.string.replacing), filename));
            
            executor.execute(() -> {
    			try {
            		OSW.replace(getExternalCacheDir(), path, filename, (index, total) -> {
                        final String text = String.format(res.getString(R.string.updating), index, total);
                        handler.post(() -> ((TextView) loading.findViewById(R.id.loadingText)).setText(text));
                    });
                    
                	handler.post(() -> {
                        Toast.makeText(this, String.format(res.getString(R.string.replaced), filename), Toast.LENGTH_LONG).show();
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
    
    private void menuDialog (MenuBottomSheet menuBottomSheet, RadioList radioList) {
        final int logo = (RadioList.stationLogo != 0) ? RadioList.stationLogo : R.drawable.utp;
        
        menuBottomSheet.setIcon(logo);
        menuBottomSheet.setTitle(radioList.getTitle());
        menuBottomSheet.setArtist(radioList.getArtist());
        menuBottomSheet.setItems(items, resources, v -> {
            switch ((int) v.getTag()) {
                case 0:
                	handler.postDelayed(() -> {
            			try {
                    		AudioPlayer.play(this, radioList);
                		} catch (Exception err) {
                			Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                		}
                        menuBottomSheet.dismiss();
            		}, DELAY);
                return;
                case 1:
                	handler.postDelayed(() -> {
            			try {
                			OSW.extract(radioList);
                			String path = Environment.getExternalStorageDirectory().getPath() + "/SaaFAndroid/" + RadioList.stationCode;
                			// Hehe... Don't ask.
                			path += "/";
                            
                            String extracted = String.format(res.getString(R.string.extracted), radioList.getFilename(), path);
            	    		Toast.makeText(this, extracted, Toast.LENGTH_LONG).show();
            			} catch (Exception err) {
            	    		Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
            			}
                        menuBottomSheet.dismiss();
            		}, DELAY);
                return;
                case 2:
                	handler.postDelayed(() -> {
                		Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
                		intent.putExtra("extension", ".mp3");
                		// I'm not gonna add radiolist to the top, so i'm gonna do a little hack
                		intent.putExtra("filename", radioList.getFilename());
                		launcher.launch(intent);
                		menuBottomSheet.dismiss();
            		}, DELAY);
                return;
            }
        });
        
        menuBottomSheet.show();
    }
    
    private void checkUpdate() {
        ExecutorService updateExecutor = Executors.newSingleThreadExecutor();
        
        updateExecutor.execute(() -> {
            CheckUpdate.check();
            
            handler.post(() -> {
                if(CheckUpdate.isUpdateAvailable) {
                    new MaterialAlertDialogBuilder(this)
                    .setTitle(String.format(res.getString(R.string.update_available), CheckUpdate.versionName))
                    .setMessage(CheckUpdate.getChangelog())
                    .setPositiveButton(res.getString(R.string.update), (d, v) -> {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(CheckUpdate.releaseURL + CheckUpdate.versionName));
                        
                        startActivity(intent);
                    })
                    .setNegativeButton(res.getString(R.string.later), null)
                    .show();
                    
                    updateExecutor.shutdown();
                }
            });
        });
    }
}