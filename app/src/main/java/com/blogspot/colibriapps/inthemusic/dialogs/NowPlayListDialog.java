package com.blogspot.colibriapps.inthemusic.dialogs;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.adapters.VkAudioAdapter;
import com.blogspot.colibriapps.inthemusic.musicplayer.MusicService;
import com.blogspot.colibriapps.inthemusic.musicplayer.playlist.PlayListManager;
import com.vk.sdk.api.model.VKApiAudio;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 10.07.15.
 */
public class NowPlayListDialog extends DialogFragment implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    private VkAudioAdapter mAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.now_playing));

        View view = inflater.inflate(R.layout.now_play_list_popup, container);
        view.findViewById(R.id.btn_done).setOnClickListener(this);

        mAdapter = new VkAudioAdapter(getActivity(),
                R.layout.vk_audio_cell,
                new ArrayList<VKApiAudio>(),
                null);

        ListView listView;
        listView = (ListView)view.findViewById(R.id.audios_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();

        if(view == null){
            return;
        }

        mAdapter.clear();
        ArrayList<VKApiAudio> vkApiAudios;
        vkApiAudios = PlayListManager.getInstance().getPlayList().getVKApiAudios();
        if(vkApiAudios != null){
            mAdapter.addAll(vkApiAudios);
        }

        view.requestLayout();

        ListView listView;
        listView = (ListView)view.findViewById(R.id.audios_list);
        if(listView != null){
            int index = PlayListManager.getInstance().getPlayList().currentTrackIndex();
            listView.setItemChecked(index, true);
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY_AT_INDEX);
        intent.putExtra(MusicService.PARAM_TRACK_INDEX, position);

        getActivity().startService(intent);
    }
}
