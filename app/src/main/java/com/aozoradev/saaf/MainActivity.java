package com.aozoradev.saaf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.content.Context;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.core.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.Intent;
import android.provider.MediaStore;
import android.database.Cursor;
import android.net.Uri;
import android.app.Activity;
import com.aozoradev.saaf.PathUtil;
import java.net.URISyntaxException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.IOException;
import java.util.ArrayList;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Button button;
    private AlertDialog.Builder dialog;
    private ListView listView;
    private ArrayList<String> listItems = new ArrayList<String>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        } else {
            initializeLogic();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = null;
            try {
                path = PathUtil.getPath(MainActivity.this, uri);
            } catch (URISyntaxException err) {
                return; //idk what should i do
            }
            
            try {
                readOswOrZipIGuess(path);
                if (listItems.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Failed to load the file", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
            } catch (IOException err) {
                err.printStackTrace();
                return;
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            initializeLogic();
        }
	}
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Dark Mode").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0 :
                Toast.makeText(this, "Still working on it", Toast.LENGTH_SHORT).show();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void initialize(Bundle _savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        button = (Button) findViewById(R.id.button);
        listView = (ListView) findViewById(R.id.listView);
        setSupportActionBar(toolbar);
        listView.setVisibility(View.GONE);
    }
    
    private void initializeLogic () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            dialog = new AlertDialog.Builder(this).setCancelable(false).setMessage("This app requires storage access to work properly. Please grant storage permission.");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface _dialog, int _which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                    }
                });
            dialog.create().show();
        }
        
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, 200);
            }
        });
    }
    
    private void readOswOrZipIGuess (String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipInputStream zis = new ZipInputStream(bis)) { 
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                listItems.add(ze.getName());
            }
        }
    }
}
