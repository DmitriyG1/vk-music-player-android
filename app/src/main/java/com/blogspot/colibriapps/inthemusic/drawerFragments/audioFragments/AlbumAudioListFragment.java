package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.adapters.BaseGenericAdapter;
import com.blogspot.colibriapps.inthemusic.adapters.VkAudioAdapter;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base.BaseAudioWithPlayListFragment;
import com.blogspot.colibriapps.inthemusic.vkLoaders.VkAudioListLoader;
import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 16.07.15.
 */

/**
 * Список аудио в альбоме
 */
public class AlbumAudioListFragment extends BaseAudioWithPlayListFragment {

    private int mAlbumId;
    private String mPlayListName = "";
    private String mAlbumTitle = "";

    public AlbumAudioListFragment(){
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setInitParams(savedInstanceState);

        return inflater.inflate(R.layout.album_audio_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view;
        view = getView();

        if(view == null){
            return;
        }

        TextView textView = (TextView)view.findViewById(R.id.album_title);
        textView.setText(mAlbumTitle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // сохраняем текущий заголовок и id альбома
        outState.putString(AudioFragmentConsts.BUNDLE_ALBUM_TITLE, mAlbumTitle);
        outState.putInt(AudioFragmentConsts.BUNDLE_ALBUM_ID, mAlbumId);
    }

    public void setInitParams(Bundle bundle){
        if(bundle == null){
            return;
        }

        mAlbumId = bundle.getInt(AudioFragmentConsts.BUNDLE_ALBUM_ID);
        mAlbumTitle = bundle.getString(AudioFragmentConsts.BUNDLE_ALBUM_TITLE);

        mPlayListName = "audios_album_" + mAlbumId;
    }

    @Override
    public Bundle makeLoaderArgs(boolean forceLoad) {
        Bundle bundle;
        bundle = new Bundle();
        bundle.putString(VkAudioListLoader.PARAM_PLAYLIST_NAME, mPlayListName);
        bundle.putInt(VkAudioListLoader.ALBUM_ID, mAlbumId);
        bundle.putBoolean(VkAudioListLoader.FORCE_LOAD, forceLoad);

        return bundle;
    }

    @Override
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        View view = getView();
        if(view == null){
            return null;
        }
        return (SwipeRefreshLayout)view.findViewById(R.id.refresh);
    }

    @Override
    public BaseGenericAdapter initListViewAndMakeAdapter() {
        View view;
        view = getView();
        if(view == null){
            return  null;
        }

        VkAudioAdapter adapter;
        adapter = new VkAudioAdapter(getActivity(),
                R.layout.vk_audio_with_lyrics_cell,
                new ArrayList<VKApiAudio>(),
                this);

        ListView listView = (ListView)view.findViewById(R.id.audios_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return adapter;
    }


    @Override
    public Loader<ArrayList<?>> onCreateLoader(int id, Bundle args) {
        return new VkAudioListLoader(getActivity(), args);
    }

    @Override
    protected String getPlayListName() {
        return mPlayListName;
    }

    @Override
    protected void setSelection(int position) {
        View view;
        view = getView();
        if(view == null){
            return;
        }
        ListView listView = (ListView)view.findViewById(R.id.audios_list);
        if(listView == null){
            return;
        }

        listView.setItemChecked(position, true);
    }
}
