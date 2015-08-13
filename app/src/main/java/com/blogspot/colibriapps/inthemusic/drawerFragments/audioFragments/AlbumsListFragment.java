package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.adapters.BaseGenericAdapter;
import com.blogspot.colibriapps.inthemusic.adapters.VkAlbumAdapter;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base.BaseVkLoaderFragment;
import com.blogspot.colibriapps.inthemusic.navigation.INavigation;
import com.blogspot.colibriapps.inthemusic.vkLoaders.VkAudioAlbumLoader;
import com.blogspot.colibriapps.inthemusic.vkontakte.VKAudioAlbum;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 15.07.15.
 */
public class AlbumsListFragment extends
        BaseVkLoaderFragment implements AdapterView.OnItemClickListener {

    public AlbumsListFragment() {
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.audio_list_refresh, container, false);
    }

    @Override
    public Bundle makeLoaderArgs(boolean forceLoad) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(VkAudioAlbumLoader.FORCE_LOAD, forceLoad);
        return bundle;
    }

    @Override
    public BaseGenericAdapter initListViewAndMakeAdapter() {
        View view;
        view = getView();
        if(view == null){
            return  null;
        }

        VkAlbumAdapter adapter;
        adapter = new VkAlbumAdapter(getActivity(),
                R.layout.vk_audio_album_cell,
                new ArrayList<VKAudioAlbum>());

        ListView listView = (ListView)view.findViewById(R.id.audios_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return adapter;
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
    public Loader<ArrayList<?>> onCreateLoader(int id, Bundle args) {
        return new VkAudioAlbumLoader(getActivity(), args);
    }

    // ===

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        VKAudioAlbum vkAudioAlbum;
        vkAudioAlbum = (VKAudioAlbum)mAdapter.getItem(position);

        Bundle bundle = new Bundle();
        bundle.putInt(AudioFragmentConsts.BUNDLE_ALBUM_ID, vkAudioAlbum.get_id());
        bundle.putString(AudioFragmentConsts.BUNDLE_ALBUM_TITLE, vkAudioAlbum.title);

        INavigation navigation;
        navigation = (INavigation)getActivity();

        if(navigation != null){
            navigation.navigateTo(AudioFragmentConsts.ALBUM_AUDIOS_LIST, bundle);
        }
    }
}
