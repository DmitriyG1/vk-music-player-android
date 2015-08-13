package com.blogspot.colibriapps.inthemusic.musicplayer.playlist;

import com.vk.sdk.api.model.VKApiAudio;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Dmitriy Gaiduk on 02.07.15.
 */

/**
 * Управляет порядком воспроизведения списка треков
 */
public class PlayList {
    public static final int NO_REPEAT = 0;
    public static final int REPEAT_ALL = 1;
    public static final int REPEAT_ONE_TRACK = 2;

    private ArrayList<VKApiAudio> mPlayList;
    // индексы из mPlayList
    private int[] mQueueIndexes;

    private String mPlayListName;
    private int mNowQueueIndex;

    private boolean mIsShuffle;
    private int mRepeat;

    private VKApiAudio mNowPlaying;

    public PlayList(){
        mRepeat = NO_REPEAT;
        mIsShuffle = false;
    }

    public synchronized void setPlaylist(ArrayList<VKApiAudio> playList, String name) {
        mPlayList = playList;
        mPlayListName = name;

        // генерируем индексы
        resetQueue();
    }

    public void resetQueue(){
        mNowQueueIndex = 0;

        // генерируем индексы
        initQueueIndexesWithFirstIndex(-1);
    }

    public String getPlayListName() {
        return mPlayListName;
    }

    /**
     * Трек по индексу. Если включено перемешивание, то будет произведено перемешивание,
     * и текущий трек будет первым в очереди
     * @param index
     * @return
     */
    public VKApiAudio getTrackAtIndex(int index){
        if(mPlayList == null){
            return null;
        }

        if(mIsShuffle){
            // текущий трек будет первым в очереди
            initQueueIndexesWithFirstIndex(index);
            mNowQueueIndex = 0;
        }else{
            mNowQueueIndex = index;
        }

        mNowPlaying = getAudioAtCurrentQueue();

        return mNowPlaying;
    }

    public synchronized VKApiAudio getNextTrack(){
        if(mPlayList == null){
            return null;
        }
        switch (mRepeat) {
            case NO_REPEAT:
                if(mNowQueueIndex < (mQueueIndexes.length - 1)){
                    mNowQueueIndex++;
                    mNowPlaying = getAudioAtCurrentQueue();
                    return mNowPlaying;
                }
                break;
            case REPEAT_ALL:
                // повторяем все
                if(mNowQueueIndex < (mQueueIndexes.length - 1)) {
                    mNowQueueIndex++;
                }else {
                    mNowQueueIndex = 0;
                }

                mNowPlaying = getAudioAtCurrentQueue();
                return mNowPlaying;
            case REPEAT_ONE_TRACK:
                mNowPlaying = getAudioAtCurrentQueue();
                return mNowPlaying;
        }

        return  null;
    }

    public synchronized VKApiAudio previousTrack(){
        if(mPlayList == null){
            return null;
        }

        switch (mRepeat) {
            case NO_REPEAT:
                if(mNowQueueIndex > 0){
                    mNowQueueIndex--;
                    mNowPlaying = getAudioAtCurrentQueue();
                    return mNowPlaying;
                }
                break;
            case REPEAT_ALL:
                // повторяем все
                if(mNowQueueIndex > 0) {
                    mNowQueueIndex--;
                }else {
                    mNowQueueIndex = mQueueIndexes.length - 1;
                }

                mNowPlaying = getAudioAtCurrentQueue();
                return mNowPlaying;
            case REPEAT_ONE_TRACK:
                mNowPlaying = getAudioAtCurrentQueue();
                return mNowPlaying;
        }

        return null;
    }

    public VKApiAudio currentTrack(){
        if(mPlayList == null){
            return null;
        }

        mNowPlaying = getAudioAtCurrentQueue();
        return mNowPlaying;
    }

    public  ArrayList<VKApiAudio> getVKApiAudios(){
        return mPlayList;
    }

    public void toggleRepeat(){
        int currentRepeat;
        currentRepeat = mRepeat;
        currentRepeat++;
        if(currentRepeat > REPEAT_ONE_TRACK) {
            currentRepeat = NO_REPEAT;
        }

        setRepeat(currentRepeat);
    }

    public void toggleShuffle(){
        setIsShuffle(!mIsShuffle);
    }

    public boolean getIsShuffle() {
        return mIsShuffle;
    }

    public void setIsShuffle(boolean mIsShuffle) {
        this.mIsShuffle = mIsShuffle;

        int previousIndex = currentTrackIndexInQueue();

        // текущий трек будет первым в очереди
        initQueueIndexesWithFirstIndex(previousIndex);

        if(mIsShuffle){
            mNowQueueIndex = 0;
        }

        // если не перемешиваем, продолжаем с текущей позиции
        mNowQueueIndex = mIsShuffle ? 0 : previousIndex;
    }

    public int getRepeat() {
        return mRepeat;
    }

    public void setRepeat(int mRepeat) {
        this.mRepeat = mRepeat;
    }

    public VKApiAudio getNowPlaying(){
        return mNowPlaying;
    }

    // ==== private

    /**
     * Формирует очередь проигрывания треков
     * @param firstIndex какой индекс должен быть первым в очереди. Если меньше 0,
     *                   то выбирается случайно
     */
    private void initQueueIndexesWithFirstIndex(int firstIndex){
        if(mPlayList == null){
            // список аудио еще не был добавлен
            return;
        }

        int[] randQueueIndexes;
        ArrayList<Integer> queueIndexes;
        int count;
        int i;
        int randIndex;

        count = mPlayList.size();
        randQueueIndexes = new int[count];
        queueIndexes = new ArrayList<>();

        if(mIsShuffle){
            Random random = new Random();

            // массив всех индексов
            for(i = 0; i < count; i++){
                queueIndexes.add(i);
            }

            // первый индекс
            if(firstIndex > -1){
                randQueueIndexes[0] = firstIndex;
                queueIndexes.remove(firstIndex);
            }

            for(i = firstIndex > -1 ? 1 : 0; i < count; i++){
                randIndex = random.nextInt(queueIndexes.size());
                randQueueIndexes[i] = queueIndexes.get(randIndex);
                queueIndexes.remove(randIndex);
            }
        }else {
            for(i = 0; i < count; i++){
                randQueueIndexes[i] = i;
            }
        }

        /*Log.i(TAG, "queue ===");
        for(i = 0; i < count; i++){
            Log.i(TAG, "queue index at " + i + ":" + randQueueIndexes[i]);
        }*/

        mQueueIndexes = randQueueIndexes;
    }

    /**
     * Текущий элемент в очереди
     * @return null, если очередь не создана либо индекс выходит за границы
     */
    VKApiAudio getAudioAtCurrentQueue(){
        int index;
        index = currentTrackIndexInQueue();
        if(index < 0){
            return null;
        }
        return mPlayList.get(index);
    }

    /**
     * текущий индекс в очереди треков
     * @return -1, если очередь не создана либо индекс выходит за границы
     */
    private int currentTrackIndexInQueue(){
        if(mQueueIndexes == null ||
                (mNowQueueIndex < 0 || mNowQueueIndex >= mQueueIndexes.length)){
            return -1;
        }

        return (mQueueIndexes[mNowQueueIndex]);
    }

    public int currentTrackIndex(){
        if(mNowPlaying == null || mPlayList == null){
            return  -1;
        }

        int count = mPlayList.size();
        for(int i = 0; i < count; i++){

            if(mNowPlaying.id == mPlayList.get(i).id){
                return  i;
            }
        }
        return  -1;
    }
}
