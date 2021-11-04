package com.aozoradev.saaf;

import com.aozoradev.saaf.constant.Constant;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.URISyntaxException;

import android.os.Handler;
import android.os.Bundle;
import android.os.Build;
import android.os.Looper;
import android.graphics.drawable.ColorDrawable;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.net.Uri;
import android.app.Activity;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Context;

public class MainActivity extends AppCompatActivity {
  private Button button;
  private RecyclerView recyclerView;
  private ArrayList<Radio> radio;
  private AlertDialog backPressedDialog;
  private AlertDialog loading;
  private ExecutorService executor;
  private Handler handler;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initialize(savedInstanceState);
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        ActivityCompat.requestPermissions(MainActivity.this, Constant.permissionsv2, 1000);
      } else {
        ActivityCompat.requestPermissions(MainActivity.this, Constant.permissions, 1000);
      }
    } else {
      initializeLogic();
    }
  }

  @Override
  public void onBackPressed() {
    backPressedDialog.show();
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
    executor.shutdown();
    executor.shutdownNow();
    System.exit(0);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
      executor.execute(() -> {
        handler.post(() -> loading.show());
        Uri uri = data.getData();
        DocumentFile df = DocumentFile.fromSingleUri(getApplicationContext(), uri);
        String nodeName = df.getName();
        try {
          radio = Radio.createRadioList(MainActivity.this, uri, nodeName.replaceAll(".osw", ""));
        } catch (IOException err) {
          handler.post(() -> {
            Util.toast(MainActivity.this, err.getMessage());
            loading.dismiss();
          });
          err.printStackTrace();
          return;
        }
        boolean isEqual = Arrays.asList(Constant.stationName).contains(nodeName);
        if ((isEqual == false) || (radio.isEmpty())) {
          handler.post(() -> {
            Util.toast(MainActivity.this, "Failed to load the file");
            loading.dismiss();
          });
          return;
        }
        
        handler.post(() -> {
          RadioAdapter adapter = new RadioAdapter(radio);
          recyclerView.setAdapter(adapter);
          recyclerView.setLayoutManager(new LinearLayoutManager(this));
          recyclerView.setVisibility(View.VISIBLE);
          button.setVisibility(View.GONE);
          getSupportActionBar().setSubtitle(Constant.station);
          loading.dismiss();
        });
      });
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
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    button = (Button) findViewById(R.id.button);
    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    setSupportActionBar(toolbar);
    
    backPressedDialog = new MaterialAlertDialogBuilder(MainActivity.this)
    .setCancelable(true)
    .setMessage("Are you sure you want to close this app?")
    .setPositiveButton("YES", (_which, _dialog) -> finish())
    .setNegativeButton("NO", null)
    .create();
    
    loading = new AlertDialog.Builder(this)
    .setCancelable(false)
    .setView(View.inflate(this, R.layout.haha_custom_progress_bar_layout_go_bbrrrr, null))
    .create();
    loading.getWindow().setBackgroundDrawable(new ColorDrawable(0));
    
    RecyclerView.ItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(divider);
    recyclerView.setVisibility(View.GONE);
    
    executor = Executors.newSingleThreadExecutor();
    handler = new Handler(Looper.getMainLooper());
  }

  private void initializeLogic() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      new MaterialAlertDialogBuilder(MainActivity.this)
      .setCancelable(false).setMessage("This app requires storage access to work properly. Please grant storage permission.")
      .setPositiveButton("OK", (_dialog, _which) -> ActivityCompat.requestPermissions(MainActivity.this, Constant.permissions, 1000)).show();
    }
    button.setVisibility(View.VISIBLE);
    button.setOnClickListener(v -> {
      Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      chooseFile.setType("*/*");
      chooseFile = Intent.createChooser(chooseFile, "Choose a file");
      startActivityForResult(chooseFile, 200);
    });
  }
}