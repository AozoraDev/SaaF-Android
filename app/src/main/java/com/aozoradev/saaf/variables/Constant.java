package com.aozoradev.saaf.variables;

import android.Manifest;
import android.annotation.SuppressLint;

public class Constant {
  @SuppressLint("InlinedApi")
  public static final int BUFFER_SIZE = 4096;
  public static final String updateURL = "https://raw.githubusercontent.com/Shumi-Project/SaaF-Android/main/changelog.json";
  public static final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
  public static final String[] itemsOption = { "Play", "Extract", "Copy Title" };
  public static final String[] donateList = { "Saweria", "Trakteer", "Patreon" };
  public static final String[] stationName = { "AA.osw", "ADVERTS.osw", "AMBIENCE.osw", "BEATS.osw", "CH.osw", "CO.osw", "CR.osw", "CUTSCENE.osw", "DS.osw", "HC.osw", "MH.osw", "MR.osw", "NJ.osw", "RE.osw", "RG.osw", "TK.osw" };
}