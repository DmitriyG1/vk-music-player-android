package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.blogspot.colibriapps.inthemusic.musicplayer.MusicService;

/**
 * Created by Dmitriy Gaiduk on 03.07.15.
 */
public abstract class BaseAudioServiceFragment extends Fragment {
    private Boolean mIsBound = false;

    public BaseAudioServiceFragment() {
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public void onResume() {
        super.onResume();

        doBindService();
    }

    @Override
    public void onPause() {

        if(getMusicService() != null){
            getMusicService().unregisterClient(getMusicServiceCallbacks());
        }
        doUnbindService();

        super.onPause();
    }

    @Override
    public void onDestroy() {

        if(getMusicService() != null){
            getMusicService().unregisterClient(getMusicServiceCallbacks());
        }

        doUnbindService();

        super.onDestroy();
    }

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getActivity().bindService(new Intent(getActivity(),
                MusicService.class), getServiceConnection(), Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(getServiceConnection());
            mIsBound = false;
        }
    }

    abstract protected MusicService getMusicService();
    abstract protected ServiceConnection getServiceConnection();
    abstract protected MusicService.Callbacks getMusicServiceCallbacks();
}
