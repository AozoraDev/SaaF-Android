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
import android.os.Bundle;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.utils.RadioList;
import com.shumiproject.saaf.utils.OSW;
import com.shumiproject.saaf.activities.adapters.RadioListAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.OnPermissionCallback;

public class MainActivity extends AppCompatActivity implements OnPermissionCallback {
    private ArrayList<RadioList> radio;
    private Button button;
    private RecyclerView recyclerView;
    private AlertDialog backPressedDialog, loading;
    private boolean canCloseFile;

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
        if (XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)) {
            // Initialize launcher
            ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                int resultCode = result.getResultCode();
                
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    Intent intentFilePicker = result.getData();
                    open(intentFilePicker);
                }
            });
        
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                // Initialize file picker
                Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
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
    
    private void open(Intent intent) {
        String path = intent.getStringExtra("path");
        String station = intent.getStringExtra("station");
        
        try {
            radio = RadioList.createList(MainActivity.this, path, station);
            RadioListAdapter adapter = new RadioListAdapter(radio);
            
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            adapter.setCallback(new RadioListAdapter.Callback() {
                @Override
                public void onItemClicked(View view, RadioList radioList) {
                    Toast.makeText(MainActivity.this, "beep", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public boolean onItemLongClicked(View view, RadioList radioList) {
                    Toast.makeText(MainActivity.this, "boop", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            getSupportActionBar().setSubtitle(RadioList.stationName);
            
            // For onBackPressed if file is loaded
            canCloseFile = true;
        } catch (Exception err) {
            Toast.makeText(MainActivity.this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Initialize some shit
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
    
    // Permission handler
    @Override
    public void onGranted(List<String> permissions, boolean all) {
        letsGo();
    }
    
    @Override
    public void onDenied(List<String> permissions, boolean never) {
        new MaterialAlertDialogBuilder(MainActivity.this)
            .setCancelable(false)
            .setMessage("This app requires storage access to work properly. Please grant storage permission.")
            .setPositiveButton("OK", (_dialog, _which) -> {
                letsGo();
            })
            .show();
    }
}