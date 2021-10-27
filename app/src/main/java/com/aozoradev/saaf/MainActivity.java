package com.aozoradev.saaf;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import java.util.ArrayList;
import com.aozoradev.saaf.Util;
import com.aozoradev.saaf.Radio;
import com.aozoradev.saaf.RadioAdapter;
import androidx.core.content.ContextCompat;
import java.util.Arrays;
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
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;

public class MainActivity extends AppCompatActivity {
  private Toolbar toolbar;
  private Button button;
  private RecyclerView recyclerView;
  private ArrayList<Radio> radio;
  private AlertDialog.Builder dialog;
  private static final String[] stationName = { "AA.osw", "ADVERTS.osw", "AMBIENCE.osw", "BEATS.osw", "CH.osw", "CO.osw", "CR.osw", "CUTSCENE.osw", "DS.osw", "HC.osw", "MH.osw", "MR.osw", "NJ.osw", "RE.osw", "RG.osw", "TK.osw" };

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
    dialog = new AlertDialog.Builder(MainActivity.this)
    .setCancelable(true).setMessage("Are you sure you want to close this app?")
    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface _dialog, int _which) {
            exitApp();
        }
    })
    .setNegativeButton("NO", null);
    dialog.create().show();
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
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

      DocumentFile df = DocumentFile.fromSingleUri(getApplicationContext(), uri);
      String nodeName = df.getName();

      try {
        radio = Radio.createRadioList(this, path, nodeName.replaceAll(".osw", ""));
        boolean isEqual = Arrays.stream(stationName).anyMatch(nodeName::equals);
        
        if (isEqual == false) {
          Toast.makeText(this, "Failed to load the file", Toast.LENGTH_LONG).show();
          return;
        }
        else if (radio.isEmpty()) {
          Toast.makeText(this, "Failed to load the file", Toast.LENGTH_LONG).show();
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

  private void initialize(Bundle _savedInstanceState) {
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    button = (Button) findViewById(R.id.button);
    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    setSupportActionBar(toolbar);
    RecyclerView.ItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(divider);
    recyclerView.setVisibility(View.GONE);
  }

  private void initializeLogic() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      dialog = new AlertDialog.Builder(MainActivity.this)
      .setCancelable(false).setMessage("This app requires storage access to work properly. Please grant storage permission.")
      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface _dialog, int _which) {
          ActivityCompat.requestPermissions(MainActivity.this, new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
          }, 1000);
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