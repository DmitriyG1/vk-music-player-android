package com.blogspot.colibriapps.inthemusic.vkLoaders;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.blogspot.colibriapps.inthemusic.user.CacheData;

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
 * Created by Dmitriy Gaiduk on 22.06.15.
 */
public class SearchLoader extends BaseVkAudioLoader {
    private static final String LOG_TAG = "SearchLoader";

    public static final String ARGS_SEARCH_STRING = "args_search_string";

    private String searchString;

    public SearchLoader(Context context, Bundle args) {
        super(context, args);
        Log.i(LOG_TAG, " create");

        searchString = args.getString(ARGS_SEARCH_STRING);

        if (TextUtils.isEmpty(searchString)){
            searchString = "";
        }

        if(!searchString.equals(CacheData.getInstance().getSearchQuery())){
            mForceLoad = true;
        }
    }

    @Override
    protected VKRequest makeRequest() {
        // из документации: максимальное значение 300.
        // С параметром offset: доступны только первые 1000 результатов
        final int count = 300;
        VKParameters vkParameters = new VKParameters();
        vkParameters.put(VKApiConst.Q, searchString);
        vkParameters.put(VKApiConst.COUNT, count);

        return VKApi.audio().search(vkParameters);
    }

    @Override
    protected void onRequestComplete(ArrayList<VKApiAudio> itemsList){
        super.onRequestComplete(itemsList);
        // дополнительно запоминаем строку поиска для текущего плейлиста в поиске
        CacheData.getInstance().setSearchQuery(searchString);
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
