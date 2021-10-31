package com.aozoradev.saaf;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.aozoradev.saaf.constant.Constant;
import com.google.android.material.button.MaterialButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import java.util.ArrayList;
import androidx.core.content.ContextCompat;
import java.util.Arrays;
import android.Manifest;
import android.content.pm.PackageManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.widget.ArrayAdapter;
import java.net.URISyntaxException;
import android.net.Uri;
import android.app.Activity;
import java.io.IOException;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Context;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
  private Toolbar toolbar;
  private MaterialButton button;
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
      ActivityCompat.requestPermissions(MainActivity.this, new String[] {
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
      }, 1000);
    } else {
      initializeLogic();
    }
  }
  
  private void exitApp() {
    super.onBackPressed();
    finish();
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
        boolean isEqual = Arrays.stream(Constant.stationName).anyMatch(nodeName::equals);
        
        if (isEqual == false) {
          Util.toast(this, "Failed to load the file");
          return;
        }
        else if (radio.isEmpty()) {
          Util.toast(this, "Failed to load the file");
          return;
        }
        
        String toolbarTitle = Util.getStation(this, nodeName.replaceAll(".osw", ""), "station");
        RadioAdapter adapter = new RadioAdapter(radio);
        
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
        getSupportActionBar().setSubtitle(toolbarTitle);
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
    switch (item.getItemId()) {
        case R.id.darkMode:
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
        default:
            return super.onOptionsItemSelected(item);
    }
  }

  private void initialize(Bundle _savedInstanceState) {
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    button = (MaterialButton) findViewById(R.id.button);
    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    setSupportActionBar(toolbar);
    backPressedDialog = new MaterialAlertDialogBuilder(MainActivity.this)
    .setCancelable(true).setMessage("Are you sure you want to close this app?")
    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface _dialog, int _which) {
            exitApp();
        }
    })
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
      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface _dialog, int _which) {
          ActivityCompat.requestPermissions(MainActivity.this, new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
          }, 1000);
        }
      }).show();
    }

    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, 200);
      }
    });
  }
}