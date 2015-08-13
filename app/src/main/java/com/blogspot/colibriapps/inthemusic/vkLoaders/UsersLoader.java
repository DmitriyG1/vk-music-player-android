package com.blogspot.colibriapps.inthemusic.vkLoaders;

import android.content.Context;
import android.content.Loader;
import android.util.Log;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 06.08.15.
 */
public class UsersLoader extends Loader {
    private static final String LOG_TAG = "UsersLoader";

    private VKRequest mRequest;

    public UsersLoader(Context context) {
        super(context);
    }

    private void deliverResult(ArrayList<VKApiUser> data) {
        if(data == null){
            Log.i(LOG_TAG, "deliverResult data is null");
        }else {
            Log.i(LOG_TAG, "deliverResult data size: " + data.size());
        }

        if (isStarted()) {
            // если стартовал, то можем отправить результат
            super.deliverResult(data);
        }
    }


    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "====== onStartLoading");
        super.onStartLoading();
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        Log.i(LOG_TAG, "onForceLoad");

        stopVKRequest();

        mRequest = VKApi.users().get();
        mRequest.executeWithListener(mVkRequestListener);
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
    private void stopVKRequest(){
        if(mRequest != null){
            mRequest.setRequestListener(null);
            mRequest.cancel();
        }
        mRequest = null;
    }

    private final VKRequest.VKRequestListener mVkRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            super.onComplete(response);
            Log.i(LOG_TAG, "response.json: " + response.json);

            JSONArray items = null;

            try {
                items = response.json.getJSONArray("response");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(items == null){
                Log.i(LOG_TAG, " items is null");
                deliverResult(null);
                return;
            }

            JSONObject json;
            ArrayList<VKApiUser> itemsList = new ArrayList<>();
            VKApiUser vkApiUser ;
            int arrayCount = items.length();

            for (int i = 0; i < arrayCount; i++) {
                json = null;
                try {
                    json = items.getJSONObject(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                vkApiUser = new VKApiUser();
                vkApiUser.parse(json);

                itemsList.add(vkApiUser);
            }

            deliverResult(itemsList);
        }

        @Override
        public void onError(VKError error) {
            super.onError(error);
            Log.i(LOG_TAG, "error: " + error);
        }
    };
}
