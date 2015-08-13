package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.adapters.BaseGenericAdapter;
import com.blogspot.colibriapps.inthemusic.adapters.VkAudioAdapter;
import com.blogspot.colibriapps.inthemusic.vkLoaders.VkAudioListLoader;
import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 21.07.15.
 */
public abstract class BaseAudioSimpleListFragment extends BaseAudioWithPlayListFragment {

    public BaseAudioSimpleListFragment(){
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.audio_list_refresh, container, false);
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
        return adapter;
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

    /**
     * Аргументы по умолчанию: передаем имя плейлиста и нужна ли форсированная загрузка
     * @param forceLoad
     * @return
     */
    @Override
    public Bundle makeLoaderArgs(boolean forceLoad) {
        Bundle bundle;
        bundle = new Bundle();
        bundle.putString(VkAudioListLoader.PARAM_PLAYLIST_NAME, getPlayListName());
        bundle.putBoolean(VkAudioListLoader.FORCE_LOAD, forceLoad);

        return bundle;
    }
}
