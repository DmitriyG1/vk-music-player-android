package com.blogspot.colibriapps.inthemusic.user;

import com.blogspot.colibriapps.inthemusic.vkontakte.VKAudioAlbum;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 29.07.15.
 */
public class CacheData {
    private static final CacheData mInstance = new CacheData();

    private ArrayList<VKAudioAlbum> mAlbums;

    private String mSearchQuery;

    private CacheData(){

    }

    public static CacheData getInstance(){
        return mInstance;
    }

    public void clearAll(){
        mAlbums = null;
        mSearchQuery = null;
    }
    public ArrayList<VKAudioAlbum> getAlbums() {
        return mAlbums;
    }

    public void setAlbums(ArrayList<VKAudioAlbum> mAlbums) {
        this.mAlbums = mAlbums;
    }

    public String getSearchQuery() {
        return mSearchQuery;
    }

    public void setSearchQuery(String mSearchQuery) {
        this.mSearchQuery = mSearchQuery;
    }
}
