package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base;

import android.content.Intent;
import android.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.blogspot.colibriapps.inthemusic.adapters.VkAudioAdapter;
import com.blogspot.colibriapps.inthemusic.dialogs.LyricsDialog;
import com.blogspot.colibriapps.inthemusic.musicplayer.MusicService;
import com.blogspot.colibriapps.inthemusic.musicplayer.playlist.PlayListManager;
import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 28.07.15.
 */
public abstract class BaseAudioWithPlayListFragment extends BaseVkLoaderFragment
        implements VkAudioAdapter.LyricsButtonClickListener,
        AdapterView.OnItemClickListener {

    private LyricsDialog mLyricsDialog;

    public BaseAudioWithPlayListFragment(){
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public void onPause(){
        super.onPause();
        // убираем диалог
        if(mLyricsDialog != null && mLyricsDialog.isVisible()){
            mLyricsDialog.dismiss();
        }
    }

    @Override
    public void onLyricsButtonClick(int position){
        showDialog(position);
    }

    private void showDialog(int position){
        if(mLyricsDialog == null){
            mLyricsDialog = new LyricsDialog();
        }
        mLyricsDialog.setVkApiAudio((VKApiAudio) mAdapter.getItem(position));
        mLyricsDialog.show(getFragmentManager(), "Dialog");
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO проверить null playList
        // проигрваем музыку
        PlayListManager.getInstance().switchToPlayList(getPlayListName());

        Intent intent = new Intent(getActivity(), MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY_AT_INDEX);
        intent.putExtra(MusicService.PARAM_TRACK_INDEX, position);

        getActivity().startService(intent);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<?>> loader, ArrayList<?> result){
        super.onLoadFinished(loader, result);
        String currentPlaylistName = PlayListManager.getInstance().getPlayList().getPlayListName();


        if(currentPlaylistName != null && currentPlaylistName.equals(getPlayListName())){
            // показываем текущий проигрываемый трек
            int index = PlayListManager.getInstance().getPlayList().currentTrackIndex();
            Log.i("AudioFragment", "index: " + index);
            setSelection(index);
        }
    }

    protected abstract void setSelection(int position);

    protected abstract String getPlayListName();
}
