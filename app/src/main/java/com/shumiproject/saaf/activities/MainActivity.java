package com.shumiproject.saaf.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.view.View;

import com.shumiproject.saaf.R;
import com.shumiproject.saaf.utils.Permission;
import com.shumiproject.saaf.utils.ToastUtils;
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
    // TODO
    
    // If everything's sets, just start it
    private void letsGo () {
        if (!Permission.checkPermission(this)) {
            new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setMessage("This app requires storage access to work properly. Please grant storage permission.")
                .setPositiveButton("OK", (_dialog, _which) -> {
                    try { 
                        Permission.requestPermission(this);
                    } catch (Exception err) {
                        ToastUtils.errorToast(this, err);
                    }
                })
                .show();
            return;
        }
    }

    // Initialize some shits b4.. uhh...
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            letsGo();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            letsGo();
        }
    }
}