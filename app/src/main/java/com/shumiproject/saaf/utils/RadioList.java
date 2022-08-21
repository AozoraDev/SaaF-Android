package com.shumiproject.saaf.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.ini4j.IniPreferences;

public class RadioList {
    private String mTitle, mArtist, mFilename;
    public static String stationName;
    public static int stationLogo;
    
    public RadioList(String title, String artist, String filename) {
        mTitle = title;
        mArtist = artist;
        mFilename = filename;
    }
    
    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }
    
    public String getFilename() {
        return mFilename;
    }
    
    public static ArrayList<RadioList> createList(Context context, String path, String station) throws IOException {
        final String[] stationList = { "AA", "ADVERTS", "AMBIENCE", "BEATS", "CH", "CO", "CR", "CUTSCENE", "DS", "HC", "MH", "MR", "NJ", "RE", "RG", "TK" };
        
        ArrayList<RadioList> songs = new ArrayList<RadioList>();
        boolean isEqual = Arrays.asList(stationList).contains(station);
        if (!isEqual) {
            return songs;
        }
        
        // Now we do this shit
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(path)));
        InputStream metadata = context.getAssets().open("meta.ini")) {
            Preferences prefs = new IniPreferences(metadata);
            ZipEntry ze;
            
            // Do loop in every item
            while ((ze = zis.getNextEntry()) != null) {
                String filename = ze.getName();
                int index = Integer.parseInt(filename.replaceAll(".mp3", "").replaceAll("[^0-9]", ""));
                
                String title = prefs.node(station).get("track" + index + ".title", null);
                String _artist = prefs.node(station).get("track" + index + ".artist", null);
                // Cannot find artist? replace it with "-"
                String artist = (_artist == null) ? "-" : _artist;
                
                songs.add(new RadioList(title, artist, filename));
            }
            // Add some to static variable for use later
            stationName = prefs.node(station).get("station", null);
            stationLogo = context.getResources().getIdentifier(station.toLowerCase(Locale.US), "drawable", context.getPackageName());
            
            return songs;
        }
    }
}