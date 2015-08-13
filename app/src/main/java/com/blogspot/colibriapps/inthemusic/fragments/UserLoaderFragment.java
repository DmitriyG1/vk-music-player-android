package com.blogspot.colibriapps.inthemusic.fragments;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.user.UserPreferences;
import com.blogspot.colibriapps.inthemusic.vkLoaders.UsersLoader;
import com.vk.sdk.api.model.VKApiUser;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 06.08.15.
 */
public class UserLoaderFragment extends Fragment implements
LoaderManager.LoaderCallbacks<ArrayList<VKApiUser>> {

    private static final String LOG_TAG = "UserLoaderFragment";

    private static final int LOADER_ID = 1;

    public UserLoaderFragment() {
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_loader_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");

        // Вызываем здесь initLoader(), иначе дважды приходит onLoadFinished()
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<ArrayList<VKApiUser>> onCreateLoader(int id, Bundle args) {
        return new UsersLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<VKApiUser>> loader, ArrayList<VKApiUser> data) {
        boolean success;
        if(data != null && data.size() == 1){
            UserPreferences.getInstance().setVKApiUser(data.get(0));
            success = true;
            Log.i(LOG_TAG, "user name: " + UserPreferences.getInstance().getVKApiUser().first_name);
        }else {
            // что-то пошло не так
            success = false;
        }

        IUserLoaderCallback userLoaderCallback;
        userLoaderCallback = (IUserLoaderCallback) getActivity();
        if(userLoaderCallback != null){
            userLoaderCallback.loadFinished(success);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<VKApiUser>> loader) {

    }

    public interface IUserLoaderCallback{
        void loadFinished(boolean success);
    }
}
