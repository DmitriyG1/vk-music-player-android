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
 * Created by Dmitriy Gaiduk on 10.08.15.
 */
public class VkAudioPopularLoader extends BaseVkAudioLoader{
    private static final String LOG_TAG = "VkAudioPopularLoader";

    /**
     * количество возвращаемых аудиозаписей
     * Из документации: положительное число, максимальное значение 1000, по умолчанию 100
     */
    private static final int DEFAULT_COUNT = 500;

    public VkAudioPopularLoader(Context context, Bundle args) {
        super(context, args);
        Log.i(LOG_TAG, " create");
    }

    @Override
    protected VKRequest makeRequest() {
        VKParameters vkParameters = new VKParameters();
        vkParameters.put(VKApiConst.COUNT, DEFAULT_COUNT);
        return VKApi.audio().getPopular(vkParameters);
    }

    @Override
    protected ArrayList<VKApiAudio> processResponse(VKResponse response) {

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
}
