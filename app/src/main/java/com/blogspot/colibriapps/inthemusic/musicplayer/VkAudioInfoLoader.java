package com.blogspot.colibriapps.inthemusic.musicplayer;

import android.util.Log;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 10.08.15.
 */
public class VkAudioInfoLoader {
    private static final String LOG_TAG = "VkAudioInfoLoader";

    private VKRequest mRequest;
    private VKRequest.VKRequestListener mVKRequestListener;

    private VkAudioInfoLoaderListener mLoaderListener;

    public VkAudioInfoLoader(){
    }

    public void loadInfoByIds(VKApiAudio audio){
        String str;
        str = audio.owner_id + "_" + audio.id;

        execute(str);
    }

    public void loadInfoByIds(VKApiAudio[] audios){
        cancel();

        int count;
        count = audios.length;
        String str = "";
        for(int i = 0; i < count; i++){
            if(i > 0){
                str += ",";
            }

            str += audios[i].owner_id + "_" + audios[i].id;
        }

        execute(str);
    }

    private void execute(String audioIds){
        VKParameters vkParameters = new VKParameters();
        vkParameters.put("audios", audioIds);
        mRequest = VKApi.audio().getById(vkParameters);

        mRequest.executeWithListener(getVKRequestListener());
    }

    public void cancel(){
        if(mRequest != null){
            mRequest.setRequestListener(null);
            mRequest.cancel();
        }
        mRequest = null;
    }


    // ====
    private VKRequest.VKRequestListener getVKRequestListener(){
        if(mVKRequestListener != null){
            return mVKRequestListener;
        }

        mVKRequestListener = new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.i(LOG_TAG, "onComplete, parameters: " +  mRequest.getMethodParameters());

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

    private ArrayList<VKApiAudio> processResponse(VKResponse response) {
        JSONArray items = null;

        try {
            items = response.json.getJSONArray("response");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (items == null) {
            return null;
        }

        JSONObject json;
        VKApiAudio vkApiAudio;
        int arrayCount = items.length();

        ArrayList<VKApiAudio> vkApiAudioArrayList = new ArrayList<>();


        for (int i = 0; i < arrayCount; i++) {
            json = null;
            try {
                json = items.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
            }

            vkApiAudio = new VKApiAudio();
            vkApiAudio.parse(json);

            vkApiAudioArrayList.add(vkApiAudio);
        }

        return  vkApiAudioArrayList;
    }

    private void onRequestComplete(ArrayList<VKApiAudio> vkApiAudios){
        if(mLoaderListener != null){
            mLoaderListener.onComplete(vkApiAudios);
        }
    }

    // ===== callbacks

    public interface VkAudioInfoLoaderListener {
        void onComplete(ArrayList<VKApiAudio> vkApiAudios);
    }

    public void setListener(VkAudioInfoLoaderListener listener){
        mLoaderListener = listener;
    }

}
