package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.dialogs.NowPlayListDialog;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base.BaseAudioServiceFragment;
import com.blogspot.colibriapps.inthemusic.musicplayer.MusicService;
import com.blogspot.colibriapps.inthemusic.musicplayer.playlist.PlayList;
import com.blogspot.colibriapps.inthemusic.musicplayer.playlist.PlayListManager;
import com.blogspot.colibriapps.inthemusic.musicplayer.VkAudioHelper;
import com.blogspot.colibriapps.inthemusic.user.UserPreferences;
import com.blogspot.colibriapps.inthemusic.utils.TextUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.vk.sdk.api.model.VKApiAudio;

/**
 * Created by Dmitriy Gaiduk on 19.06.15.
 */
public class NowPlayingFragment extends BaseAudioServiceFragment implements View.OnClickListener{

    private static final String LOG_TAG = "NowPlayingFragment";

    private final int COLOR_ACCENT = Color.argb(0xFF, 0x00, 0x00, 0x00);
    private final int COLOR_NOT_ACCENT = Color.argb(0xFF, 0x99, 0x99, 0x99);

    private MusicService mMusicService;
    private AdView mAdView;
    private NowPlayListDialog mNowPlayListDialog;

    public NowPlayingFragment() {
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.now_playing, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        View rootView = getView();

        if(rootView == null){
            return;
        }

        mAdView = (AdView) rootView.findViewById(R.id.ad_view);

        AdRequest.Builder requestBuilder = new AdRequest.Builder();

        AdRequest adRequest = requestBuilder.build();

        mAdView.loadAd(adRequest);

        // === set listeners
        ImageButton button;
        // play button
        button = (ImageButton) rootView.findViewById(R.id.play_button);
        button.setOnClickListener(this);

        // skip next
        button = (ImageButton) rootView.findViewById(R.id.skip_next);
        button.setOnClickListener(this);

        // skip previous
        button = (ImageButton) rootView.findViewById(R.id.skip_previous);
        button.setOnClickListener(this);

        // shuffle Button
        button = (ImageButton) rootView.findViewById(R.id.shuffle_button);
        button.setOnClickListener(this);

        // repeat Button
        button = (ImageButton) rootView.findViewById(R.id.repeat_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.now_play_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resume the AdView
        if(mAdView != null){
            mAdView.resume();
        }

        View view = getView();
        if(view != null){
            SeekBar seekBar = (SeekBar)getView().findViewById(R.id.player_seek_bar);
            seekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        }
        VkAudioHelper.getInstance().addListener(mVkAudioHelperListener);

        updateShuffleButton();
        updateRepeatButton();
        updateCoverLyrics();
    }

    @Override
    public void onPause() {
        // Pause the AdView
        if(mAdView != null) {
            mAdView.pause();
        }

        VkAudioHelper.getInstance().removeListener(mVkAudioHelperListener);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Destroy the AdView
        if(mAdView != null) {
            mAdView.destroy();
        }

        VkAudioHelper.getInstance().removeListener(mVkAudioHelperListener);

        super.onDestroy();
    }

    // ===================== handlers

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_now_playing:
                    if(mNowPlayListDialog == null){
                        mNowPlayListDialog = new NowPlayListDialog();
                    }
                    mNowPlayListDialog.show(getFragmentManager(), null);
                return true;
            case R.id.action_switch_text_cover:
                if(mMusicService != null && mMusicService.getNowPlaying() != null){
                    UserPreferences.getInstance().toggleShowCoverImage();
                    updateCoverLyrics();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                onPlayClick();
                break;
            case R.id.skip_next:
                onSkipNextClick();
                break;
            case R.id.skip_previous:
                onSkipPreviousClick();
                break;
            case R.id.repeat_button:
                onRepeatClick();
                break;
            case R.id.shuffle_button:
                onShuffleClick();
                break;
        }
    }

    private void onShuffleClick() {
        PlayListManager.getInstance().getPlayList().toggleShuffle();
        updateShuffleButton(true);
    }

    private void onRepeatClick() {
        PlayListManager.getInstance().getPlayList().toggleRepeat();
        updateRepeatButton(true);
    }

    private void onSkipPreviousClick(){
        if(mMusicService == null){
            return;
        }

        mMusicService.previousTrack();
    }

    private void onSkipNextClick(){
        if(mMusicService == null){
            return;
        }

        mMusicService.nextTrack();
    }

    private void onPlayClick(){
        Intent intent = new Intent(getActivity(), MusicService.class);
        intent.setAction(MusicService.ACTION_TOGGLE_PLAYBACK);
        getActivity().startService(intent);
    }

    // ===================== ui

    private void updateCoverLyrics(){
        View view;
        view = getView();

        if(view == null){
            return;
        }

        ViewFlipper viewFlipper;
        viewFlipper = (ViewFlipper)view.findViewById(R.id.lyrics_flipper);

        if(viewFlipper == null){
            return;
        }

        final int FLIPPER_COVER_CHILD = 0;
        final int FLIPPER_LYRICS_CHILD = 1;

        if(UserPreferences.getInstance().isShowCoverImage()){
            if(viewFlipper.getDisplayedChild() != FLIPPER_COVER_CHILD){
                viewFlipper.setDisplayedChild(FLIPPER_COVER_CHILD);
            }
            ImageView imageView = (ImageView)view.findViewById(R.id.cover_image);
            if(imageView != null){
                imageView.setImageResource(R.drawable.ic_music_note_black);
                imageView.setColorFilter(COLOR_NOT_ACCENT);
            }

            if(mMusicService != null && mMusicService.readyToPlay()) {
                VKApiAudio vkApiAudio;
                vkApiAudio = PlayListManager.getInstance().getPlayList().getNowPlaying();
                if (vkApiAudio != null) {
                    VkAudioHelper.getInstance().getArt(vkApiAudio);
                }
            }
        }else {
            if(viewFlipper.getDisplayedChild() != FLIPPER_LYRICS_CHILD){
                viewFlipper.setDisplayedChild(FLIPPER_LYRICS_CHILD);
            }
            setLyricsText(getString(R.string.lyrics_loading));
            VKApiAudio vkApiAudio;
            vkApiAudio = PlayListManager.getInstance().getPlayList().getNowPlaying();
            if(vkApiAudio != null){
                VkAudioHelper.getInstance().getLyrics(vkApiAudio,
                        getString(R.string.no_lyrics),
                        true);
            }
        }
    }

    private void cleanCoverLyrics(){
        View view;
        view = getView();

        if(view == null){
            return;
        }

        ImageView imageView = (ImageView)view.findViewById(R.id.cover_image);

        if(imageView != null){
            imageView.setImageResource(R.drawable.ic_music_note_black);
            imageView.setColorFilter(COLOR_NOT_ACCENT);
        }

        setLyricsText(getString(R.string.lyrics_loading));
    }

    private void setCover(Bitmap img){
        View view;
        view = getView();

        if(view == null){
            return;
        }
        ImageView imageView = (ImageView)view.findViewById(R.id.cover_image);

        if(imageView == null){
            return;
        }

        if(img == null){
            imageView.setImageResource(R.drawable.ic_music_note_black);
            imageView.setColorFilter(COLOR_ACCENT);
        }else {
            imageView.setImageBitmap(img);
            imageView.clearColorFilter();
        }
    }

    private void setLyricsText(String text){
        View view;
        view = getView();

        if(view == null){
            return;
        }

        TextView lyricsTextView;
        lyricsTextView = (TextView)view.findViewById(R.id.lyrics_text);

        if(lyricsTextView != null){
            if(lyricsTextView.getMovementMethod() == null){
                lyricsTextView.setMovementMethod(new ScrollingMovementMethod());
            }
            lyricsTextView.setText(text);
            // устанавливаем первоначальное расположение
            lyricsTextView.scrollTo(0, 0);
        }
    }

    private void updatePlayButton(Boolean isPlaying){
        ImageButton imageButton;

        View view = getView();
        if(view == null){
            return;
        }

        imageButton = (ImageButton) view.findViewById(R.id.play_button);

        if(isPlaying){
            imageButton.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
        }else {
            imageButton.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
        }
    }

    private void updateShuffleButton(){
        updateShuffleButton(false);
    }

    private void updateShuffleButton(boolean showToast){
        ImageButton imageButton;

        View view = getView();
        if(view == null){
            return;
        }

        imageButton = (ImageButton) view.findViewById(R.id.shuffle_button);

        PlayList playList = PlayListManager.getInstance().getPlayList();
        int toastStringId;

        if(playList.getIsShuffle()){
            imageButton.setColorFilter(COLOR_ACCENT);
            toastStringId = R.string.shuffle_on;
        }else {
            imageButton.setColorFilter(COLOR_NOT_ACCENT);
            toastStringId = R.string.shuffle_off;
        }

        if(showToast){
            Toast.makeText(getActivity(), getString(toastStringId), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRepeatButton(){
        updateRepeatButton(false);
    }

    private void updateRepeatButton(boolean showToast){
        ImageButton imageButton;

        View view = getView();
        if(view == null){
            return;
        }

        imageButton = (ImageButton) view.findViewById(R.id.repeat_button);

        PlayList playList = PlayListManager.getInstance().getPlayList();
        int toastStringId = 0;
        switch (playList.getRepeat()){
            case PlayList.NO_REPEAT:
                imageButton.setImageResource(R.drawable.ic_repeat_black_48dp);
                imageButton.setColorFilter(COLOR_NOT_ACCENT);
                toastStringId = R.string.repeat_off;
                break;
            case PlayList.REPEAT_ALL:
                imageButton.setImageResource(R.drawable.ic_repeat_black_48dp);
                imageButton.setColorFilter(COLOR_ACCENT);
                toastStringId = R.string.repeat;
                break;
            case PlayList.REPEAT_ONE_TRACK:
                imageButton.setImageResource(R.drawable.ic_repeat_one_black_48dp);
                imageButton.setColorFilter(COLOR_ACCENT);
                toastStringId = R.string.repeat_one;
                break;
        }

        if(showToast){
            Toast.makeText(getActivity(), getString(toastStringId), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTitle(VKApiAudio vkApiAudio){
        View view = getView();
        if(view == null){
            return;
        }

        if(vkApiAudio != null){
            TextView textView;
            textView = (TextView)view.findViewById(R.id.audio_title);
            textView.setText(vkApiAudio.title);

            // artist
            textView = (TextView)view.findViewById(R.id.artist_label);
            textView.setText(vkApiAudio.artist);
        }
    }

    private void updateDuration(int duration) {
        View view = getView();
        if (view == null) {
            return;
        }

        SeekBar seekBar = (SeekBar)view.findViewById(R.id.player_seek_bar);
        seekBar.setMax(duration);

        TextView textView;

        // продолжительность трека
        textView = (TextView)view.findViewById(R.id.total_time_label);
        textView.setText(TextUtil.makeTimeStringWithSeconds(duration / 1000));
    }

    private final Runnable onEverySecond = new Runnable(){
        @Override
        public void run(){
            updateTime();
        }
    };

    private void updateTime(){
        View view = getView();
        if (view == null) {
            return;
        }

        SeekBar seekBar = (SeekBar)view.findViewById(R.id.player_seek_bar);
        TextView textView;
        textView = (TextView)view.findViewById(R.id.current_time_label);

        int currentPosition;
        if(mMusicService != null){
            currentPosition = mMusicService.getCurrentPosition();
        }else {
            currentPosition = 0;
        }

        seekBar.setProgress(currentPosition);
        textView.setText(TextUtil.makeTimeStringWithSeconds(currentPosition / 1000));

        // обновляем каждую секунду
        seekBar.postDelayed(onEverySecond, 1000);
    }

    private final MusicService.Callbacks mNowPlayMusicServiceCallbacks = new MusicService.Callbacks() {
        @Override
        public void readyToPlay() {
            updateCoverLyrics();
        }

        @Override
        public void onPlayItemChange(VKApiAudio vkApiAudio) {
            cleanCoverLyrics();
            updateTitle(vkApiAudio);
        }

        @Override
        public void onTogglePlay(Boolean isPlaying) {
            updatePlayButton(isPlaying);
        }

        @Override
        public void onDurationChange(int duration) {
            updateDuration(duration);
        }
    };

    // ===================== callbacks
    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser && mMusicService != null){
                        mMusicService.seekPosition(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };

    private final ServiceConnection mNowPlayConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(LOG_TAG, "onServiceConnected");
            // подключаемся к сервису
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mMusicService = binder.getService();
            mMusicService.registerClient(getMusicServiceCallbacks());

            updateTitle(mMusicService.getNowPlaying());
            updatePlayButton(mMusicService.isPlaying());
            updateDuration(mMusicService.getDuration());
            updateCoverLyrics();
            updateTime();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(LOG_TAG, "onServiceDisconnected");
        }
    };

    private final VkAudioHelper.VkAudioHelperListener mVkAudioHelperListener =
            new VkAudioHelper.VkAudioHelperListener() {
        @Override
        public void onLyrics(int lyrics_id, String text) {
            VKApiAudio vkApiAudio;
            vkApiAudio = PlayListManager.getInstance().getPlayList().getNowPlaying();

            if(vkApiAudio != null && vkApiAudio.lyrics_id == lyrics_id){
                setLyricsText(text);
            }
        }

        @Override
        public void onCover(int vkAudioId, Bitmap img) {
            VKApiAudio vkApiAudio;
            vkApiAudio = PlayListManager.getInstance().getPlayList().getNowPlaying();

            if(vkApiAudio != null && vkApiAudio.id == vkAudioId){
                setCover(img);
            }
        }
    };

    // ===================== BaseAudioServiceFragment

    @Override
    public MusicService getMusicService() {
        return mMusicService;
    }

    @Override
    public ServiceConnection getServiceConnection() {
        return mNowPlayConnection;
    }

    @Override
    protected MusicService.Callbacks getMusicServiceCallbacks() {
        return mNowPlayMusicServiceCallbacks;
    }
}
