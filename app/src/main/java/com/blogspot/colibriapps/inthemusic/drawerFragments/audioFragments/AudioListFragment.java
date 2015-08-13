package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments;
import android.content.Loader;
import android.os.Bundle;

import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base.BaseAudioSimpleListFragment;
import com.blogspot.colibriapps.inthemusic.vkLoaders.VkAudioListLoader;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 21.07.15.
 */
public class AudioListFragment extends BaseAudioSimpleListFragment {

    public AudioListFragment(){
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public Loader<ArrayList<?>> onCreateLoader(int id, Bundle args) {
        return new VkAudioListLoader(getActivity(), args);
    }

    @Override
    protected String getPlayListName() {
        return "all_audios";
    }
}
