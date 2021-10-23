package com.aozoradev.saaf;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import java.util.ArrayList;
import com.aozoradev.saaf.ReadOsw;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.widget.ArrayAdapter;
import java.net.URISyntaxException;
import android.net.Uri;
import android.widget.Toast;
import android.app.Activity;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Button button;
    private ListView listView;
    private ArrayList<String> listItems = new ArrayList<String>();
    private AlertDialog.Builder dialog;

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
                err.printStackTrace();
                return;
            }
            
            try {
                ReadOsw.load(path, listItems);
                if (listItems.isEmpty()) {
                    Toast.makeText(this, "Failed to load the file", Toast.LENGTH_LONG).show();
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
    
    private void initialize(Bundle _savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        button = (Button) findViewById(R.id.button);
        listView = (ListView) findViewById(R.id.listView);
        setSupportActionBar(toolbar);
        listView.setVisibility(View.GONE);
    }
    
    private void initializeLogic () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            dialog = new AlertDialog.Builder(MainActivity.this).setCancelable(false).setMessage("This app requires storage access to work properly. Please grant storage permission.");
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
}
