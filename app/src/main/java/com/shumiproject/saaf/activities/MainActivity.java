package com.shumiproject.saaf.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.utils.*;
import com.shumiproject.saaf.adapters.RadioListAdapter;
import com.shumiproject.saaf.bottomsheet.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {
    private ArrayList<RadioList> radio;
    private Button button;
    private RecyclerView recyclerView;
    private AlertDialog backPressedDialog, loading;
    private BottomSheetDialog aboutDialog;
    private Menu menu;
    private Resources res;
    private AudioPlayer player;
    private boolean canCloseFile;
    
    private final int DELAY = 200;
    private final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private final int[] resources = { R.drawable.play, R.drawable.download, R.drawable.refresh };
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
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
        int id = item.getItemId();
        
        if (id == R.id.create_idx) {
            try {
            	OSW.createIDX(RadioList.stationPath);
                Toast.makeText(this, String.format(res.getString(R.string.idx_created), RadioList.stationCode), Toast.LENGTH_LONG).show();
        	} catch (Exception err) {
                Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
            }
            
            return true;
        } else if (id == R.id.about) {
            aboutDialog.show();
            
            return true;
        } else if (id == R.id.settings) {
            Toast.makeText(this, res.getString(R.string.unimplemented), Toast.LENGTH_LONG).show();
            
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
        launcher.unregister();
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
                player.release();
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
                menu.findItem(R.id.create_idx).setEnabled(false).setVisible(false);
                getSupportActionBar().setSubtitle(null);
                
                // Nullify (static) vars for no reason
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
    // https://stackoverflow.com/a/66366102 with some modifications
    // START
	private boolean checkPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return Environment.isExternalStorageManager();
		else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
			int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
		}
	}
    
	private void requestPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            permissionLauncher.launch(intent);
		} else ActivityCompat.requestPermissions(this, permissions, 1000);
	}
    
    ActivityResultLauncher<Intent> permissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
        letsGo();
    });
    
    @Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        letsGo();
	}
    // END
    
    // If everything's sets, just start it
    private void letsGo () {
        if (checkPermission()) {
            // Only check update if all permissions are granted.
            checkUpdate();
            
            permissionLauncher.unregister();
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
        new MaterialAlertDialogBuilder(this)
        .setCancelable(false)
        .setMessage(res.getString(R.string.storage_permission))
        .setPositiveButton(res.getString(R.string.ok), (dialog, which) -> requestPermission())
        .show();
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
        
        aboutDialog = new BottomSheetDialog(this);
        aboutDialog.setContentView(R.layout.about);
        String moreInfo = res.getString(R.string.more_info);
        ((TextView) aboutDialog.findViewById(R.id.more)).setText(String.format(moreInfo, System.getProperty("os.arch")));
        ((TextView) aboutDialog.findViewById(R.id.hyperlinks)).setMovementMethod(LinkMovementMethod.getInstance());
        aboutDialog.create();
        
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
            player = new AudioPlayer(this);
            
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
        final String[] items = { res.getString(R.string.play), res.getString(R.string.extract), res.getString(R.string.replace) };
        
        menuBottomSheet.setIcon(logo);
        menuBottomSheet.setTitle(radioList.getTitle());
        menuBottomSheet.setArtist(radioList.getArtist());
        menuBottomSheet.setItems(items, resources, v -> {
            switch ((int) v.getTag()) {
                case 0:
                	handler.postDelayed(() -> {
            			try {
                    		player.play(radioList);
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