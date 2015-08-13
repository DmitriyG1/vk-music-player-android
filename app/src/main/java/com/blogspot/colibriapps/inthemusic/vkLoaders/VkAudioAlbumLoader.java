package com.blogspot.colibriapps.inthemusic.vkLoaders;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import com.blogspot.colibriapps.inthemusic.user.CacheData;
import com.blogspot.colibriapps.inthemusic.vkontakte.VKAudioAlbum;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 29.07.15.
 */
public class VkAudioAlbumLoader extends Loader {
    public static final String FORCE_LOAD = "FORCE_LOAD";
    private VKRequest mRequest;
    private final boolean mForceLoad;
    private ArrayList<VKAudioAlbum> mAlbums;

    private static final String LOG_TAG = "VkAudioAlbumLoader";

    public VkAudioAlbumLoader(Context context,  Bundle args) {
        super(context);

        mForceLoad = args.getBoolean(FORCE_LOAD);
    }

    private void deliverResult(ArrayList<VKAudioAlbum> data) {
        Log.i(LOG_TAG, "deliverResult data size: " + data.size());

        mAlbums = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }
    }


    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "onStartLoading");

        super.onStartLoading();

        mAlbums = CacheData.getInstance().getAlbums();

        if(takeContentChanged() ||
                mForceLoad ||
                (mAlbums != null && mAlbums.size() == 0)
                || (mAlbums == null)){
            Log.i(LOG_TAG, "    onStartLoading force load, takeContentChanged(): " + takeContentChanged());
            forceLoad();
        }else {
            Log.i(LOG_TAG, "    onStartLoading deliver from cache");

            deliverResult(mAlbums);
        }

    }


    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        Log.i(LOG_TAG, "onStopLoading");
        stopVKRequest();
    }

    @Override
    protected void onForceLoad() {
        Log.i(LOG_TAG, "onForceLoad");

        super.onForceLoad();
        // TODO

        mRequest = VKApi.audio().getAlbums();

        mRequest.executeWithListener(mVKRequestListener);
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        Log.i(LOG_TAG, "onAbandon");
        stopVKRequest();
    }

    @Override
    protected void onReset() {
        super.onReset();
        Log.i(LOG_TAG, "onReset");
        stopVKRequest();
    }


    // =====

    private void stopVKRequest(){
        if(mRequest != null){
            mRequest.setRequestListener(null);
            mRequest.cancel();
        }
        mRequest = null;
    }

    private final VKRequest.VKRequestListener mVKRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            super.onComplete(response);
            Log.d(LOG_TAG, "onComplete getAudios");

            JSONObject responseObj = null;

            try {
                responseObj = response.json.getJSONObject("response");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (responseObj == null) {
                Log.d(LOG_TAG, "Response is null");
                return;
            }


            JSONArray items = null;

            try {
                items = responseObj.getJSONArray("items");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (items == null) {
                return;
            }

            JSONObject json;
            ArrayList<VKAudioAlbum> albums = new ArrayList<>();
            VKAudioAlbum vkAudioAlbum;
            int arrayCount = items.length();

            for (int i = 0; i < arrayCount; i++) {
                json = null;
                try {
                    json = items.getJSONObject(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                vkAudioAlbum = new VKAudioAlbum();
                vkAudioAlbum.parse(json);

                albums.add(vkAudioAlbum);
            }
            // cache
            CacheData.getInstance().setAlbums(albums);
            deliverResult(albums);
        }

        @Override
        public void onError(VKError error) {
            super.onError(error);
            Log.i(LOG_TAG, "error: " + error);
        }
    };
}
