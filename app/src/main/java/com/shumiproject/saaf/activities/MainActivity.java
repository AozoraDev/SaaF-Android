package com.shumiproject.saaf.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.utils.Permission;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private RecyclerView recyclerView;
    private AlertDialog backPressedDialog, loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize(savedInstanceState);
        letsGo();
    }

    @Override
    public void onBackPressed() {
        backPressedDialog.show();
    }

    // AsyncTask deprecated, btw. Do pull request if you have any alt.
    private class openOSW extends AsyncTask <Intent, Void, Uri> {
        protected void onPreExecute() {
            loading.show();
        }

        protected Uri doInBackground (Intent... intent) {
            return intent[0].getData();
        }

        protected void onPostExecute (Uri uri) {
            loading.dismiss();
            Toast.makeText(MainActivity.this, uri.getPath(), Toast.LENGTH_LONG).show();
        }

    }
    
    // If everything's sets, just start it
    private void letsGo () {
        if (!Permission.checkPermission(this)) {
            new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setMessage("This app requires storage access to work properly. Please grant storage permission.")
                .setPositiveButton("OK", (_dialog, _which) -> {
                    try { 
                        Permission.requestPermission(this, activityLauncher);
                    } catch (Exception err) {
                        Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .show();
            return;
        }

        // If permissions are granted, show the button
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> {
            // Initialize file picker
            Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
            activityLauncher.launch(intent);
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
            .setMessage("Are you sure you want to close this app?")
            .setPositiveButton("YES", (_which, _dialog) -> finish())
            .setNegativeButton("NO", null)
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If permissions are granted
        if (requestCode == 1000) {
            letsGo();
        }
    }
    
    // ActivityResult things.
    public ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
        int resultCode = result.getResultCode();
        
        if (resultCode == 69420) {
            Intent intent = result.getData();
            String path = intent.getStringExtra("path");
            
            Toast.makeText(this, path, Toast.LENGTH_LONG).show();
        }
        else if (resultCode == AppCompatActivity.RESULT_OK) {
            letsGo();
        }
    });
}