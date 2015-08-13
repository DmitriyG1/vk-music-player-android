package com.blogspot.colibriapps.inthemusic.musicplayer.playlist;
import com.vk.sdk.api.model.VKApiAudio;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dmitriy Gaiduk on 06.07.15.
 */

/**
 * Управляет списками аудиозаписей
 */
public class PlayListManager {

    private static final PlayListManager _instance = new PlayListManager();
    private final PlayList mPlayList;

    private final Map<String, ArrayList<VKApiAudio>> mVKAudiosMap;

    private PlayListManager(){
        mPlayList = new PlayList();
        mVKAudiosMap = new HashMap<>();
    }

    public static PlayListManager getInstance() {
        return _instance;
    }

    public void clearAll(){
        mVKAudiosMap.clear();
    }

    public void put(String playListName, ArrayList<VKApiAudio> vkApiAudios, Boolean needClone) {
        ArrayList<VKApiAudio> cachedVkApiAudios;
        cachedVkApiAudios = mVKAudiosMap.get(playListName);
        if(cachedVkApiAudios != null){
            cachedVkApiAudios.clear();
            if(vkApiAudios != null){
                cachedVkApiAudios.addAll(vkApiAudios);
            }
        }else {
            ArrayList<VKApiAudio> resultVKApiAudios;
            if(needClone){
                resultVKApiAudios = new ArrayList<>();
                if(vkApiAudios != null){
                    resultVKApiAudios.addAll(vkApiAudios);
                }
            }else {
                resultVKApiAudios = vkApiAudios;
            }
            mVKAudiosMap.put(playListName, resultVKApiAudios);
        }
        // если тот же плейлист, сбрасываем очередь проигрвывания
        if(playListName.equals(mPlayList.getPlayListName())){
            mPlayList.resetQueue();
        }
    }

    public void switchToPlayList(String playListName){
        // TODO проверить если это обновление текущего плейлиста
        if(mPlayList.getPlayListName() != null && mPlayList.getPlayListName().equals(playListName)){
            return;
        }

        ArrayList<VKApiAudio> vkApiAudios;
        vkApiAudios = mVKAudiosMap.get(playListName);
        mPlayList.setPlaylist(vkApiAudios, playListName);
    }

    public ArrayList<VKApiAudio> getVKApiAudios(String playListName){
        return getVKApiAudios(playListName, true);
    }

    public ArrayList<VKApiAudio> getVKApiAudios(String playListName, boolean clone){
        ArrayList<VKApiAudio> vkApiAudios;
        vkApiAudios = mVKAudiosMap.get(playListName);
        ArrayList<VKApiAudio> resultVKApiAudios = null;

        if(clone){
            if(vkApiAudios != null){
                resultVKApiAudios = new ArrayList<>();
                resultVKApiAudios.addAll(vkApiAudios);
            }
        }else {
            resultVKApiAudios = vkApiAudios;
        }
        return resultVKApiAudios;
    }

    public PlayList getPlayList(){
        return mPlayList;
    }

}
