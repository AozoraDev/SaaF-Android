package com.aozoradev.saaf;

import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;

import com.aozoradev.saaf.variables.Constant;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;

public class SettingsFragment extends PreferenceFragmentCompat {
  private AlertDialog themeDialog, donateDialog;
  
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.settings, rootKey);
    donateDialog = new MaterialAlertDialogBuilder(getActivity())
    .setItems(Constant.donateList, (_dialog, _which) -> {
      switch (_which) {
        case 0:
          openItOkayLmao("https://saweria.co/AozoraDev");
        break;
        case 1:
          openItOkayLmao("https://trakteer.id/AozoraDev");
        break;
        case 2:
          openItOkayLmao("https://patreon.com/AozoraDev");
        break;
      }
    }).create();
    
    themeDialog = new MaterialAlertDialogBuilder(getActivity())
    .setItems(Constant.themeList, (_dialog, _which) -> {
      switch (_which) {
        case 0:
          // TODO
        break;
        case 1:
          // TODO
        break;
        case 2:
          // TODO
        break;
      }
    })
    .create();
  }
  
  @Override
  public boolean onPreferenceTreeClick (Preference preference) {
    String key = preference.getKey();
    
    switch (key) {
      case "setting_theme":
        themeDialog.show();
      break;
      case "setting_about":
        Intent intent = new Intent(getActivity(), AboutActivity.class);
        getActivity().startActivity(intent);
      break;
      case "setting_donate":
        donateDialog.show();
      break;
    }
    return true;
  }
  
  private void openItOkayLmao(String url) {
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_VIEW);
    intent.addCategory(Intent.CATEGORY_BROWSABLE);
    intent.setData(Uri.parse(url));

    startActivity(intent);
  }
}