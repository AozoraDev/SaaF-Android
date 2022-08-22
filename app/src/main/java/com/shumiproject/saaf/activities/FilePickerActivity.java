package com.shumiproject.saaf.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.File;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.adapters.FilePickerAdapter;

public class FilePickerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FilePickerAdapter adapter;
    private String savedDir, storagePath;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);
        initialize(savedInstanceState);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
    }
    
    @Override
    public void onBackPressed() {
        // Close the activity if it's on storage dir
        if (savedDir.equals(storagePath)) {
            setResult(AppCompatActivity.RESULT_CANCELED, getIntent());
            finish();
        } else {
            // Otherwise, back to previous dir
            updateRecyclerView(savedDir, true);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void initialize(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        File storage = Environment.getExternalStorageDirectory();
        
        // Save it so onBackPressed can use it
        String mPath = getPath(storage);
        storagePath = mPath;
        savedDir = mPath;
        
        toolbar.setTitle("Choose a File");
        toolbar.setSubtitle(storagePath);
        setSupportActionBar(toolbar);
        
        File[] storageList = storage.listFiles((file) -> {
            return (file.isDirectory() && !file.isHidden()) || (file.isFile() && file.getName().endsWith(".osw"));
        });
        adapter = new FilePickerAdapter(storageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Handle click
        adapter.setOnItemClickedListener((f) -> {
            if (f.isFile()) {
                String path = getPath(f);
                String name = f.getName().replaceAll(".osw", "");
                
                Intent intent = getIntent();
                intent.putExtra("path", path);
                intent.putExtra("station", name);
                setResult(AppCompatActivity.RESULT_OK, intent);
                finish();
            } else if (f.isDirectory()) {
                updateRecyclerView(f.getAbsolutePath(), false);
            }
        });
        
        // Show back button on Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    private void updateRecyclerView (String f, boolean isBack) {
        // Why there's executor?
        // Delaying execution so there will be click animation on recycler. Problem?
        if (isBack) {
            File dir = new File(f + "/..");
            savedDir = getPath(dir);
            File[] anotherFile = dir.listFiles((file) -> {
                return (file.isDirectory() && !file.isHidden()) || (file.isFile() && file.getName().endsWith(".osw"));
            });
            getSupportActionBar().setSubtitle(getPath(dir));
             // Update data
            adapter.updateList(anotherFile);
            adapter.notifyDataSetChanged();
        } else {
            executor.execute(() -> {
                File dir = new File(f);
                savedDir = getPath(dir);
                File[] anotherFile = dir.listFiles((file) -> {
                    return (file.isDirectory() && !file.isHidden()) || (file.isFile() && file.getName().endsWith(".osw"));
                });
                
                handler.postDelayed(() -> {
                    getSupportActionBar().setSubtitle(getPath(dir));
                    // Update data
                    adapter.updateList(anotherFile);
                    adapter.notifyDataSetChanged();
                }, 200);
            });
        }
    }
    
    private String getPath (File file) {
        String path = null;
        
        try {
            path = file.getCanonicalPath();
        } catch (Exception err) {
            path = file.getAbsolutePath();
        }
        
        return path;
    }
}