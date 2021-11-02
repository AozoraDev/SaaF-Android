package com.aozoradev.saaf;

import com.aozoradev.saaf.constant.Constant;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.URISyntaxException;

import android.view.View;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.net.Uri;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Context;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
  private Toolbar toolbar;
  private Button button;
  private RecyclerView recyclerView;
  private ArrayList<Radio> radio;
  private MaterialAlertDialogBuilder backPressedDialog;
  private SharedPreferences sharedPref;
  private static boolean isDarkModeEnabled;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initialize(savedInstanceState);
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      ActivityCompat.requestPermissions(MainActivity.this, Constant.permissions, 1000);
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
    System.exit(0);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
      Uri uri = data.getData();

      DocumentFile df = DocumentFile.fromSingleUri(getApplicationContext(), uri);
      String nodeName = df.getName();

      try {
        radio = Radio.createRadioList(this, uri, nodeName.replaceAll(".osw", ""));
        boolean isEqual = Arrays.asList(Constant.stationName).contains(nodeName);
        
        if (isEqual == false) {
          Util.toast(this, "Failed to load the file");
          return;
        } else if (radio.isEmpty()) {
          Util.toast(this, "Failed to load the file");
          return;
        }
        
        RadioAdapter adapter = new RadioAdapter(radio);
        
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
        getSupportActionBar().setSubtitle(Constant.station);
      } catch (IOException err) {
        Util.toast(this, err.getMessage());
        err.printStackTrace();
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
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    menu.findItem(R.id.darkMode).setChecked(isDarkModeEnabled);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.darkMode) {
      SharedPreferences.Editor editor = sharedPref.edit();
      boolean isChecked = item.isChecked();
      if (isChecked == true) {
        item.setChecked(false);
        editor.putBoolean("darkMode", false).apply();
      } else if (isChecked == false) {
        item.setChecked(true);
        editor.putBoolean("darkMode", true).apply();
      }
      Util.toast(this, "The changes will take effect after the restart");
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void initialize(Bundle _savedInstanceState) {
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    button = (Button) findViewById(R.id.button);
    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    setSupportActionBar(toolbar);
    backPressedDialog = new MaterialAlertDialogBuilder(MainActivity.this)
    .setCancelable(true).setMessage("Are you sure you want to close this app?")
    .setPositiveButton("YES", (_dialog, _which) -> finish())
    .setNegativeButton("NO", null);
    RecyclerView.ItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(divider);
    recyclerView.setVisibility(View.GONE);
    sharedPref = getApplicationContext().getSharedPreferences("data", Context.MODE_PRIVATE);
    isDarkModeEnabled = sharedPref.getBoolean("darkMode", false);
  }

  private void initializeLogic() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      new MaterialAlertDialogBuilder(MainActivity.this)
      .setCancelable(false).setMessage("This app requires storage access to work properly. Please grant storage permission.")
      .setPositiveButton("OK", (_dialog, _which) -> ActivityCompat.requestPermissions(MainActivity.this, Constant.permissions, 1000)).show();
    }

    button.setOnClickListener(v -> {
      Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      chooseFile.setType("*/*");
      chooseFile = Intent.createChooser(chooseFile, "Choose a file");
      startActivityForResult(chooseFile, 200);
    });
  }
}