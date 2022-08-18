package com.shumiproject.saaf.utils;

public class RadioList {
    private String mTitle;
    private String mArtist;
    
    public RadioList(String title, String artist) {
        mTitle = title;
        mArtist = artist;
    }
    
    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }
}