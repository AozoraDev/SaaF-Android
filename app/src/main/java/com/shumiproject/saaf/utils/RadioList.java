package com.shumiproject.saaf.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import org.ini4j.IniPreferences;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class RadioList {
    private String mTitle, mArtist, mFilename;
    public static String stationName, stationPath, stationCode;
    public static int stationLogo;
    public static ZipResourceFile osw;
    
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
            throw new IOException("This is not a GTASA audio file");
        }
        
        try (ZipFile _osw = new ZipFile(path);
        InputStream metadata = context.getAssets().open("meta.ini")) {
            List<FileHeader> fileHeaders = _osw.getFileHeaders();
            Preferences prefs = new IniPreferences(metadata);
            
            for (FileHeader fileHeader : fileHeaders) {
                String filename = fileHeader.getFileName();
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
            stationPath = path;
            stationCode = station;
            osw = new ZipResourceFile(path);
        }
        
        if (songs == null || songs.isEmpty()) {
            throw new IOException("The file is empty or not a valid file");
        }
        
        return songs;
    }
}