package com.blogspot.colibriapps.inthemusic.musicplayer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Dmitriy Gaiduk on 09.07.15.
 */
public class VkAudioHelper {
    private static final String LOG_TAG = "VkAudioHelper";

    private static final VkAudioHelper _instance = new VkAudioHelper();

    private VkAudioHelperListener mVkAudioHelperListener;

    private GetCoverTask mGetCoverTask;

    // cache
    private Bitmap mAlbumArt = null;
    private int albumArtId = -1;

    private String mLyrics;
    private int mLyricsId = -1;

    private VkAudioHelper(){

    }

    /**
     * Note: add only one listener in current version
     * @param vkAudioHelperListener listener
     */
    public void addListener(VkAudioHelperListener vkAudioHelperListener){
        mVkAudioHelperListener = vkAudioHelperListener;
    }

    public void removeListener(VkAudioHelperListener vkAudioHelperListener){
        if(mVkAudioHelperListener == vkAudioHelperListener){
            mVkAudioHelperListener = null;
        }
    }

    public static VkAudioHelper getInstance() {
        return _instance;
    }

    public void getArt(VKApiAudio vkApiAudio){
        if(vkApiAudio == null){
            onCover(-1, null);
            return;
        }

        if(vkApiAudio.id == albumArtId){
            onCover(vkApiAudio.id, mAlbumArt);
            return;
        }

        mAlbumArt = null;
        albumArtId = vkApiAudio.id;

        if(mGetCoverTask != null){
            mGetCoverTask.cancel(true);
        }

        mGetCoverTask = new GetCoverTask();
        mGetCoverTask.execute(vkApiAudio);
    }

    public void getLyrics(VKApiAudio vkApiAudio, String noLyrics){
        getLyrics(vkApiAudio, noLyrics, false);
    }

    public void getLyrics(VKApiAudio vkApiAudio, String noLyrics, boolean needCache){
        if(vkApiAudio == null){
            onLyrics(-1, noLyrics);
            return;
        }

        if(vkApiAudio.lyrics_id < 1){
            onLyrics(vkApiAudio.lyrics_id, noLyrics);
            return;
        }

        if(mLyricsId == vkApiAudio.lyrics_id){
            onLyrics(mLyricsId, mLyrics);
            return;
        }

        if(needCache){
            mLyricsId = vkApiAudio.lyrics_id;
            mLyrics = "";
        }

        VKParameters vkParameters = new VKParameters();
        vkParameters.put("lyrics_id", vkApiAudio.lyrics_id);

        VKRequest request = VKApi.audio().getLyrics(vkParameters);
        request.executeWithListener(mVKRequestListener);
    }

    private final VKRequest.VKRequestListener mVKRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            super.onComplete(response);

            JSONObject responseObj = null;

            try {
                responseObj = response.json.getJSONObject("response");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (responseObj == null) {
                Log.i(LOG_TAG, "response is null");
                return;
            }

            String text = responseObj.optString("text");
            int lyricsId = responseObj.optInt("lyrics_id");
            onLyrics(lyricsId, text);
        }

        @Override
        public void onError(VKError error) {
            super.onError(error);
            Log.i(LOG_TAG, "error: " + error);
        }
    };

    private void onLyrics(int lyrics_id, String text){
        if(mLyricsId == lyrics_id){
            mLyrics = text;
        }

        if(mVkAudioHelperListener != null){
            mVkAudioHelperListener.onLyrics(lyrics_id, text);
        }
    }

    private void onCover(int vkAudioId, Bitmap img){
        if(albumArtId == vkAudioId){
            mAlbumArt = img;
        }

        if(mVkAudioHelperListener != null){
            mVkAudioHelperListener.onCover(vkAudioId, img);
        }
    }

    public interface VkAudioHelperListener{
        void onLyrics(int lyrics_id, String text);
        void onCover(int vkAudioId, Bitmap img);
    }


    class GetCoverTask extends AsyncTask<VKApiAudio, Void, Bitmap> {
        final String LOG_TAG_GetCoverTask = "GetCoverTask";

        MediaMetadataRetriever mediaMetadataRetriever;
        VKApiAudio mVkApiAudio;

        public GetCoverTask() {
            super();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if(!isCancelled() && mVkApiAudio != null){
                onCover(mVkApiAudio.id, bitmap);
            }
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            mVkApiAudio = null;
            super.onCancelled(bitmap);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if(mediaMetadataRetriever != null){
                mediaMetadataRetriever.release();
            }
        }

        @Override
        protected Bitmap doInBackground(VKApiAudio... params) {
            mVkApiAudio = params[0];
            String songUrl = mVkApiAudio.url;

            mediaMetadataRetriever = new MediaMetadataRetriever();
            Bitmap bitmap = null;
            try{
                if (Build.VERSION.SDK_INT >= 14){
                    mediaMetadataRetriever.setDataSource(songUrl,
                            new HashMap<String, String>());
                }
                else{
                    mediaMetadataRetriever.setDataSource(songUrl);
                }
            }catch (Exception e){
                Log.e(LOG_TAG_GetCoverTask, "getCover() can't set data source: " + songUrl);
                e.printStackTrace();
                mediaMetadataRetriever.release();
                return null;
            }
            byte[] artBytes =  mediaMetadataRetriever.getEmbeddedPicture();
            if(artBytes != null) {
                InputStream is = new ByteArrayInputStream(mediaMetadataRetriever.getEmbeddedPicture());
                bitmap = BitmapFactory.decodeStream(is);
            }

            mediaMetadataRetriever.release();
            return bitmap;
        }

    }


}
