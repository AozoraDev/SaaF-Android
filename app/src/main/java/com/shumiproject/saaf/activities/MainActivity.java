package com.shumiproject.saaf.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import java.util.ArrayList;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.utils.RadioList;
import com.shumiproject.saaf.activities.adapters.RadioListAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.permissionx.guolindev.PermissionX;

public class MainActivity extends AppCompatActivity {
    private ArrayList<RadioList> radio;
    private Button button;
    private RecyclerView recyclerView;
    private AlertDialog backPressedDialog, loading;
    private boolean canCloseFile;
    
    // Needed perms
    private final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private final String[] permissions11 = { Manifest.permission.MANAGE_EXTERNAL_STORAGE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize(savedInstanceState);
        letsGo();
    }

    @Override
    public void onBackPressed() {
        if (canCloseFile) {
            // If file is loaded rn, close it.
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
                    getSupportActionBar().setSubtitle(null);
                })
                .create().show();
        } else {
            backPressedDialog.show();
        }
    }
    
    // If everything's sets, just start it
    private void letsGo () {
        // A little hack
        String[] perms = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ? permissions11 : permissions;
    
        if (PermissionX.isGranted(this.getApplicationContext(), perms[0])) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                // Initialize file picker
                Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
                activityLauncher.launch(intent);
            });
            return;
        }
        
        PermissionX.init(this)
            .permissions(perms)
            .request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    letsGo();
                } else {
                    new MaterialAlertDialogBuilder(MainActivity.this)
                        .setCancelable(false)
                        .setMessage("This app requires storage access to work properly. Please grant storage permission.")
                        .setPositiveButton("OK", (_dialog, _which) -> {
                            letsGo();
                        })
                    .show();
                }
            });
    }

    // Initialize some shit b4.. uhh...
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
        loading = new AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(View.inflate(this, R.layout.loading, null))
            .create();
        // Make the background transparent so it's only show loading
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Someone said using "setHasFixedSize" can optimize the recyclerview.
        recyclerView.setHasFixedSize(true);
        recyclerView.setVisibility(View.GONE);
    }
    
    // ActivityResult things.
    // Messy...
    public ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
        int resultCode = result.getResultCode();
        
        if (resultCode == 69420) {
            Intent intent = result.getData();
            String path = intent.getStringExtra("path");
            String station = intent.getStringExtra("station");
            
            try {
                radio = RadioList.createList(MainActivity.this, path, station);
                RadioListAdapter adapter = new RadioListAdapter(radio);
                
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
                
                getSupportActionBar().setSubtitle(RadioList.stationName);
                
                canCloseFile = true;
            } catch (Exception err) {
                Toast.makeText(MainActivity.this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    });
}