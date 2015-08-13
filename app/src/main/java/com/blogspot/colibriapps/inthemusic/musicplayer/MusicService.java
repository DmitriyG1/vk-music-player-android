package com.blogspot.colibriapps.inthemusic.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.blogspot.colibriapps.inthemusic.MainNavigationDrawerActivity;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.musicplayer.playlist.PlayListManager;
import com.example.android.musicplayer.AudioFocusHelper;
import com.example.android.musicplayer.MusicFocusable;
import com.vk.sdk.api.model.VKApiAudio;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 29.07.15.
 *
 * За основу взят класс com.example.android.musicplayer.MusicService
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MusicFocusable,
        VkAudioInfoLoader.VkAudioInfoLoaderListener {

    private static final String TAG = "MusicService";

    public static final String ACTION_PLAY_AT_INDEX =
            "com.blogspot.colibriapps.musicplayer.action.ACTION_PLAY_AT_INDEX";
    public static final String PARAM_TRACK_INDEX =
            "com.blogspot.colibriapps.musicplayer.action.param.TRACK_INDEX";
    public static final String ACTION_TOGGLE_PLAYBACK =
            "com.blogspot.colibriapps.musicplayer.action.action.TOGGLE_PLAYBACK";
    public static final String ACTION_NEXT =
            "com.blogspot.colibriapps.musicplayer.action.action.NEXT";
    public static final String ACTION_PREVIOUS =
            "com.blogspot.colibriapps.musicplayer.action.action.PREVIOUS";
    public static final String ACTION_STOP =
            "com.blogspot.colibriapps.musicplayer.action.action.STOP";

    // ==== binder
    private final IBinder mBinder = new LocalBinder();

    private VKApiAudio mNowPlaying;

    private Callbacks mCallback;

    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    private static final float DUCK_VOLUME = 0.1f;

    // media player
    private MediaPlayer mPlayer = null;

    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    private AudioFocusHelper mAudioFocusHelper = null;

    // indicates the state our service:
    enum State {
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    }

    private State mState = State.Stopped;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }

    private AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // title of the song we are currently playing
    private String mSongTitle = "";

    // Wifi lock that we hold when streaming files from the internet, in order to prevent the
    // device from shutting off the Wifi radio
    private WifiManager.WifiLock mWifiLock;

    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    private final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;


    private Notification mNotification = null;

    private VkAudioInfoLoader mVKAudioInfoLoader;

    @Override
    public void onCreate() {
        Log.i(TAG, "Creating service");

        mVKAudioInfoLoader = new VkAudioInfoLoader();
        mVKAudioInfoLoader.setListener(this);

        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus
    }

    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        if(mVKAudioInfoLoader != null){
            mVKAudioInfoLoader.cancel();
            mVKAudioInfoLoader.setListener(null);
        }
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.i(TAG, "onStartCommand action: " + action);

        switch (action){
            case ACTION_TOGGLE_PLAYBACK:
                processTogglePlaybackRequest();
                break;
            case ACTION_PLAY_AT_INDEX:
                processPlayAtIndex(intent);
                break;
            case ACTION_NEXT:
                nextTrack();
                break;
            case ACTION_PREVIOUS:
                previousTrack();
                break;
            case ACTION_STOP:
                processStopRequest();
                break;
        }

        return START_NOT_STICKY; // Means we started the service, but don't want it to
        // restart in case it's killed.
    }


    // ===================== Actions

    private void processPlayAtIndex(Intent intent){
        int trackIndex = intent.getIntExtra(PARAM_TRACK_INDEX, 0);

        playTrackAtIndex(trackIndex);
    }

    private void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }

        if(mPlayer != null && mCallback != null){
            mCallback.onTogglePlay(mPlayer.isPlaying());
        }
    }

    private void processPlayRequest() {

        tryToGetAudioFocus();

        // actually play the song
        if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing;
            setUpAsForeground(mSongTitle);
            configAndStartMediaPlayer();
        }else {
            VKApiAudio vkApiAudio;
            vkApiAudio = PlayListManager.getInstance().getPlayList().currentTrack();
            if(vkApiAudio != null){
                playTrack(vkApiAudio);
            }
        }
    }

    private void processPauseRequest() {
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
        }
    }

    private void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    // ===================== public

    public boolean readyToPlay(){
        return  (mState == State.Playing || mState == State.Paused);
    }

    /**
     * Gets the current playback position.
     * @return the current position in milliseconds
     */
    public int getCurrentPosition(){
        if(mPlayer == null){
            return 0;
        }

        if(mState == State.Paused || mState == State.Playing){
            return mPlayer.getCurrentPosition();
        }

        return 0;
    }

    /**
     * Gets the duration of the file
     * @return the duration in milliseconds
     */
    public int getDuration(){
        if(mPlayer == null){
            return  0;
        }

        if(mState == State.Paused || mState == State.Playing){
            return mPlayer.getDuration();
        }

        return 0;
    }

    public void seekPosition(int position) {
        if(mPlayer == null){
            return;
        }

        if(mState == State.Paused || mState == State.Playing){
            mPlayer.seekTo(position);
        }
    }

    public VKApiAudio getNowPlaying() {
        return mNowPlaying;
    }

    public Boolean isPlaying() {
        return (mPlayer != null && mState == State.Playing);
    }

    public void nextTrack(){
        VKApiAudio vkApiAudio = PlayListManager.getInstance().getPlayList().getNextTrack();
        if(vkApiAudio != null){
            playTrack(vkApiAudio);
        }else {
            Log.i(TAG, "nextTrack() is null");
        }
    }

    public void previousTrack(){
        VKApiAudio vkApiAudio = PlayListManager.getInstance().getPlayList().previousTrack();

        if(vkApiAudio != null){
            playTrack(vkApiAudio);
        }else {
            Log.i(TAG, "previousTrack is null");
        }
    }

    private void playTrackAtIndex(int index){
        // TODO если тот же трек, то перематываем на начало и играем без загрузки заново
        VKApiAudio vkApiAudio = PlayListManager.getInstance().getPlayList().getTrackAtIndex(index);
        playTrack(vkApiAudio);
    }

    private void processStopRequest() {
        processStopRequest(false);
    }

    private void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;

            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();

            // service is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }

    // ===================== music player logic

    private void playTrack(VKApiAudio vkApiAudio){
        mNowPlaying = vkApiAudio;

        if(mCallback != null){
            mCallback.onPlayItemChange(vkApiAudio);
        }

        // останавливаем текущую композицию
        if(mPlayer != null && mState == State.Playing){
            mPlayer.stop();
        }

        mState = State.Stopped;

        relaxResources(false); // release everything except MediaPlayer

        // запрашиваем информацию о композиции, чтобы узнать текущий url для mp3
        // из документации VK SDK: ссылки на mp3 привязаны к ip-адресу.
        mVKAudioInfoLoader.loadInfoByIds(vkApiAudio);
    }

    /**
     * Starts playing the song
     */
    private void playSong(String songUrl) {
        mState = State.Stopped;
        relaxResources(false); // release everything except MediaPlayer

        // whether the song we are playing is streaming from the network
        boolean mIsStreaming;

        try {
            createMediaPlayerIfNeeded();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(songUrl);
            mIsStreaming = songUrl.startsWith("http:") || songUrl.startsWith("https:");

            mSongTitle = mNowPlaying.title;

            mState = State.Preparing;
            setUpAsForeground(mSongTitle + " (" + getString(R.string.loading) + ")");

            // Until the media player is prepared, we *cannot* call start() on it!
            mPlayer.prepareAsync();

            // If we are streaming from the internet, we want to hold a Wifi lock, which prevents
            // the Wifi radio from going to sleep while the song is playing. If, on the other hand,
            // we are *not* streaming, we want to release the lock if we were holding it before.
            if (mIsStreaming) mWifiLock.acquire();
            else if (mWifiLock.isHeld()) mWifiLock.release();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    private void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        }
        else
            mPlayer.reset();
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    private void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) mPlayer.pause();
            return;
        }
        else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud

        if (!mPlayer.isPlaying()) mPlayer.start();
    }


    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    private void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) mWifiLock.release();
    }

    private void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    private void setUpAsForeground(String text) {
        Intent intent;
        intent = new Intent(getApplicationContext(), MainNavigationDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainNavigationDrawerActivity.FROM_NOTIFICATION_PARAM, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int icon = R.drawable.notification_icon;
        String title = getString(R.string.app_name);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        mNotification = builder.setContentIntent(pendingIntent)
                .setSmallIcon(icon).setTicker(text)
                .setAutoCancel(true).setContentTitle(title)
                .setContentText(text).build();

        startForeground(NOTIFICATION_ID, mNotification);
    }

    private void updateNotification(String text) {
        Intent intent;
        intent = new Intent(getApplicationContext(), MainNavigationDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainNavigationDrawerActivity.FROM_NOTIFICATION_PARAM, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int icon = R.drawable.notification_icon;
        String title = getString(R.string.app_name);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        mNotification = builder.setContentIntent(pendingIntent)
                .setSmallIcon(icon).setTicker(text)
                .setAutoCancel(true).setContentTitle(title)
                .setContentText(text).build();

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }


    // ===================== MusicFocusable
    @Override
    public void onGainedAudioFocus() {
        mAudioFocus = AudioFocus.Focused;

        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

    // =====================  VkAudioInfoLoader.VkAudioInfoLoaderListener

    @Override
    public void onComplete(ArrayList<VKApiAudio> vkApiAudios) {
        if(mNowPlaying == null){
            return;
        }

        if(vkApiAudios != null && vkApiAudios.size() == 1){
            VKApiAudio vkApiAudio;
            vkApiAudio = vkApiAudios.get(0);

            Log.i(TAG, "url before: " + mNowPlaying.url);
            Log.i(TAG, "url after: " + vkApiAudio.url);
            mNowPlaying.url = vkApiAudio.url;

            if(vkApiAudio.id == mNowPlaying.id){
                tryToGetAudioFocus();
                playSong(mNowPlaying.url);
            }
        }
    }

    // ===================== Handlers
    @Override
    public void onCompletion(MediaPlayer mp) {
        nextTrack();
    }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(),
                getString(R.string.media_player_error),
                Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        return true; // true indicates we handled the error
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "onPrepared");
        // The media player is done preparing. That means we can start playing!
        mState = State.Playing;
        updateNotification(mSongTitle);
        configAndStartMediaPlayer();

        if(mCallback != null){
            mCallback.readyToPlay();
            mCallback.onTogglePlay(mPlayer.isPlaying());
            mCallback.onDurationChange(mPlayer.getDuration());
        }
    }

    // ====== Binder
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    //===== callbacks interface
    public interface Callbacks{
        void onPlayItemChange(VKApiAudio vkApiAudio);
        void onTogglePlay(Boolean isPlaying);
        void onDurationChange(int duration);
        void readyToPlay();
    }

    // TODO сделать для более одного клиента
    public void registerClient(Callbacks client){
        this.mCallback = client;
    }

    public void unregisterClient(Callbacks client){
        if(client == this.mCallback){
            this.mCallback = null;
        }
    }
}
