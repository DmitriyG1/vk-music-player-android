package com.blogspot.colibriapps.inthemusic.vkLoaders;

import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import com.blogspot.colibriapps.inthemusic.musicplayer.playlist.PlayListManager;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 04.08.15.
 */
public abstract class BaseVkAudioLoader extends Loader {
    private static final String LOG_TAG = "BaseVkAudioLoader";
    public static final String FORCE_LOAD = "FORCE_LOAD";
    public static final String PARAM_PLAYLIST_NAME = "PARAM_PLAYLIST_NAME";

    private final String playListName;
    private VKRequest mRequest;
    protected boolean mForceLoad;
    private VKRequest.VKRequestListener mVKRequestListener;

    private ArrayList<VKApiAudio> vkApiAudios;

    public BaseVkAudioLoader(Context context, Bundle args) {
        super(context);

        mForceLoad = args.getBoolean(FORCE_LOAD);
        playListName = args.getString(PARAM_PLAYLIST_NAME);
        Log.i(LOG_TAG, " create BaseVkAudioLoader mForceLoad: " + mForceLoad
                + ", playListName: " + playListName);

        //onContentChanged();
    }

    private void deliverResult(ArrayList<VKApiAudio> data) {
        if(data == null){
            Log.i(LOG_TAG, "deliverResult data is null");
        }else {
            Log.i(LOG_TAG, "deliverResult data size: " + data.size());
        }

        vkApiAudios = data;

        if (isStarted()) {
            // если стартовал, то можем отправить результат
            super.deliverResult(data);
        }
    }


    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "====== onStartLoading");

        super.onStartLoading();

        vkApiAudios = PlayListManager.getInstance().getVKApiAudios(playListName);
        boolean changed;
        changed = takeContentChanged() ||
                mForceLoad ||
                (vkApiAudios != null && vkApiAudios.size() == 0) ||
                (vkApiAudios == null);
        Log.i(LOG_TAG, "    changed: " + changed);
        Log.i(LOG_TAG, "    mForceLoad: " + mForceLoad);
        Log.i(LOG_TAG, "    takeContentChanged(): " + takeContentChanged());
        Log.i(LOG_TAG, "    play list: " + playListName);
        Log.i(LOG_TAG, "    vkApiAudios is null: " + (vkApiAudios == null));

        if(vkApiAudios != null){
            Log.i(LOG_TAG, "    vkApiAudios size: " + vkApiAudios.size());
        }

        if(changed){
            Log.i(LOG_TAG, "    ==> forceLoad");
            forceLoad();
        }else {
            Log.i(LOG_TAG, "    ==> deliver from cache");

            deliverResult(vkApiAudios);
        }
    }

    @Override
    protected void onForceLoad() {
        Log.i(LOG_TAG, "onForceLoad");
        super.onForceLoad();

        stopVKRequest();

        mRequest = makeRequest();
        mRequest.executeWithListener(getVKRequestListener());
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        Log.i(LOG_TAG, "onStopLoading");
        stopVKRequest();
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

    private VKRequest.VKRequestListener getVKRequestListener(){
        if(mVKRequestListener != null){
            return mVKRequestListener;
        }

        mVKRequestListener = new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.i(LOG_TAG, "onComplete, method: " + mRequest.methodName);

                onRequestComplete(processResponse(response));
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.i(LOG_TAG, "attemptFailed attemptNumber: " + attemptNumber +
                        ", totalAttempts: " + totalAttempts);

            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.i(LOG_TAG, "error: " + error);
            }
        };

        return mVKRequestListener;
    }

    private void stopVKRequest(){
        if(mRequest != null){
            mRequest.setRequestListener(null);
            mRequest.cancel();
        }
        mRequest = null;
    }

    protected void onRequestComplete(ArrayList<VKApiAudio> itemsList){
        PlayListManager.getInstance().put(playListName, itemsList, true);
        deliverResult(itemsList);
    }

    abstract protected VKRequest makeRequest();

    abstract protected ArrayList<VKApiAudio>  processResponse(VKResponse response);

}
