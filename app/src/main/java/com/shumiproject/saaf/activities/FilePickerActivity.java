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

import java.io.File;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.adapters.FilePickerAdapter;

public class FilePickerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FilePickerAdapter adapter;
    private String storagePath, extension;
    private static String savedDir;
    
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
        Intent intent = getIntent();
        extension = intent.getStringExtra("extension");
        
        // Save it so onBackPressed can use it
        String mPath = getPath(storage);
        storagePath = mPath;
        if (savedDir == null) savedDir = mPath;
        
        toolbar.setTitle(getResources().getString(R.string.choose_file));
        toolbar.setSubtitle(savedDir);
        setSupportActionBar(toolbar);
        
        File dir = new File(savedDir);
        File[] storageList = dir.listFiles((file) -> {
            return (file.isDirectory() && !file.isHidden()) || (file.isFile() && file.getName().endsWith(extension));
        });
        adapter = new FilePickerAdapter(storageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickedListener((f) -> {
            if (f.isFile()) {
                String path = getPath(f);
                String filename = intent.getStringExtra("filename");
                
                intent.putExtra("path", path);
                if (extension.equals(".osw")) {
                	String name = f.getName().replaceAll(".osw", "");
                	intent.putExtra("station", name);
                } else if (extension.equals(".mp3")) intent.putExtra("filename", filename);
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
        if (isBack) {
            File dir = new File(f + "/..");
            savedDir = getPath(dir);
            File[] anotherFile = dir.listFiles((file) -> {
                return (file.isDirectory() && !file.isHidden()) || (file.isFile() && file.getName().endsWith(extension));
            });
            
            getSupportActionBar().setSubtitle(getPath(dir));
            adapter.updateList(anotherFile);
            adapter.notifyDataSetChanged();
        } else {
            // Why there's handler right there?
            // Delaying execution so there will be click animation on recycler. Problem?
            handler.postDelayed(() -> {
                File dir = new File(f);
                savedDir = getPath(dir);
                File[] anotherFile = dir.listFiles((file) -> {
                    return (file.isDirectory() && !file.isHidden()) || (file.isFile() && file.getName().endsWith(extension));
                });
                
                getSupportActionBar().setSubtitle(getPath(dir));
                adapter.updateList(anotherFile);
                adapter.notifyDataSetChanged();
            }, 200);
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