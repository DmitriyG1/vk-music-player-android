package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments;

import android.content.Loader;
import android.os.Bundle;

import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base.BaseAudioSimpleListFragment;
import com.blogspot.colibriapps.inthemusic.vkLoaders.VkAudioPopularLoader;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 10.08.15.
 */
public class PopularAudioListFragment  extends BaseAudioSimpleListFragment {

    public PopularAudioListFragment(){
        // Пустой конструктор требуется для подклассов фрагментов
    }
    @Override
    public Loader<ArrayList<?>> onCreateLoader(int id, Bundle args) {
        return new VkAudioPopularLoader(getActivity(), args);
    }

    @Override
    protected String getPlayListName() {
        return "popular_audios";
    }
}
