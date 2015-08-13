package com.blogspot.colibriapps.inthemusic.vkontakte;

import org.json.JSONObject;

/**
 * Created by Dmitriy Gaiduk on 10.06.15.
 */
public class VKAudioAlbum {
    private int _id;
    private int _ownerID;
    public String title;

    public VKAudioAlbum(){
    }

    public void parse(JSONObject json){
        if(json == null){
            return;
        }

        _id = json.optInt("id");
        _ownerID = json.optInt("owner_id");
        title = json.optString("title");

    }

    public int get_id() {
        return _id;
    }

    public int get_ownerID() {
        return _ownerID;
    }

}
