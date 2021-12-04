package com.aozoradev.saaf;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    initialize(savedInstanceState);
    initializeLogic();
  }
  
  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    
    if (id == android.R.id.home) {
      onBackPressed();
      return true;
    }
    
    return super.onOptionsItemSelected(item);
  }
  
  private void initialize (Bundle savedInstanceState) {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle("Settings");
  }
  
  private void initializeLogic () {
    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment()).commit();
  }
}