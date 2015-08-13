package com.blogspot.colibriapps.inthemusic.vkLoaders;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 21.07.15.
 */
public class VkAudioListLoader extends BaseVkAudioLoader{
    public static final String ALBUM_ID = "ALBUM_ID";

    private final int mAlbumId;
    private static final String LOG_TAG = "VkAudioListLoader";


    public VkAudioListLoader(Context context, Bundle args) {
        super(context, args);
        Log.i(LOG_TAG, " create");

        mAlbumId = args.getInt(ALBUM_ID, 0);
    }

    @Override
    protected VKRequest makeRequest() {
        VKParameters vkParameters = new VKParameters();

        if(mAlbumId > 0){
            vkParameters.put(VKApiConst.ALBUM_ID, mAlbumId);
        }

        return VKApi.audio().get(vkParameters);
    }

    @Override
    protected ArrayList<VKApiAudio> processResponse(VKResponse response) {

        JSONObject responseObj = null;

        try {
            responseObj = response.json.getJSONObject("response");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (responseObj == null) {

            return null;
        }

        JSONArray items = null;

        try {
            items = responseObj.getJSONArray("items");
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
}
