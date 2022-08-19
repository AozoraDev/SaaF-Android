package com.shumiproject.saaf.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.activities.adapters.FilePickerAdapter;

public class FilePickerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FilePickerAdapter adapter;
    private String savedDir, storagePath;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);
        initialize(savedInstanceState);
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
        
        // Handle click
        adapter.setOnItemClickedListener((f) -> {
            if (f.isFile()) {
                String path = getPath(f);
                String name = f.getName().replaceAll(".osw", "");
                
                Intent intent = getIntent();
                intent.putExtra("path", path);
                intent.putExtra("station", name);
                setResult(69420, intent);
                finish();
            } else if (f.isDirectory()) {
                updateRecyclerView(f.getAbsolutePath(), false);
            }
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Show back button on Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    private void updateRecyclerView (String f, boolean isBack) {
        File dir = null;
        
        if (isBack) dir = new File(f + "/.."); else dir = new File(f);
        String path = getPath(dir);
        savedDir = path;
        
        File[] anotherFile = dir.listFiles((file) -> {
            return (file.isDirectory() && !file.isHidden()) || (file.isFile() && file.getName().endsWith(".osw"));
        });
                
        getSupportActionBar().setSubtitle(path);
        // Update data
        adapter.updateList(anotherFile);
        adapter.notifyDataSetChanged();
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