package com.blogspot.colibriapps.inthemusic.dialogs;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.musicplayer.VkAudioHelper;
import com.vk.sdk.api.model.VKApiAudio;

/**
 * Created by Dmitriy Gaiduk on 09.07.15.
 */
public class LyricsDialog extends DialogFragment implements View.OnClickListener {
    private String lyricsText;
    private TextView lyricsTextView;
    private TextView vkAudioTextView;
    private VKApiAudio vkApiAudio;

    private static final String LYRICS_TEXT_KEY = "LYRICS_TEXT_KEY";
    private static final String VK_AUDIO_KEY = "VK_AUDIO_KEY";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.lyrics));

        View v = inflater.inflate(R.layout.lyrics_popup, container);
        v.findViewById(R.id.btn_done).setOnClickListener(this);

        lyricsTextView = (TextView) v.findViewById(R.id.lyrics_text);
        vkAudioTextView = (TextView) v.findViewById(R.id.vk_audio_title);

        lyricsTextView.setMovementMethod(new ScrollingMovementMethod());

        if(savedInstanceState != null){
            lyricsText = savedInstanceState.getString(LYRICS_TEXT_KEY);
            vkApiAudio = savedInstanceState.getParcelable(VK_AUDIO_KEY);
        }

        updateLyrics();
        updateVkAudio();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(outState != null){
            outState.putString(LYRICS_TEXT_KEY, lyricsText);
            outState.putParcelable(VK_AUDIO_KEY, vkApiAudio);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateLyrics();
        updateVkAudio();

        if(vkApiAudio != null){
            lyricsText = getString(R.string.lyrics_loading);
            updateLyrics();
            VkAudioHelper.getInstance().addListener(mVkAudioHelperListener);
            VkAudioHelper.getInstance().getLyrics(vkApiAudio, getString(R.string.no_lyrics));
        }
    }

    @Override
    public void onPause() {
        VkAudioHelper.getInstance().removeListener(mVkAudioHelperListener);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        VkAudioHelper.getInstance().removeListener(mVkAudioHelperListener);
        super.onDestroy();
    }

    private final VkAudioHelper.VkAudioHelperListener mVkAudioHelperListener =
            new VkAudioHelper.VkAudioHelperListener() {
        @Override
        public void onLyrics(int lyrics_id, String text) {
            if(vkApiAudio != null && vkApiAudio.lyrics_id == lyrics_id){
                setLyricsText(text);
            }
        }

        @Override
        public void onCover(int vkAudioId, Bitmap img) {

        }
    };

    public void onClick(View v) {
        dismiss();
    }

    private void setLyricsText(String lyricsText) {
        this.lyricsText = lyricsText;

        updateLyrics();
    }

    public void setVkApiAudio(VKApiAudio vkApiAudio) {
        this.vkApiAudio = vkApiAudio;
        lyricsText = "";
        updateLyrics();
        updateVkAudio();
    }

    private void updateLyrics(){
        if(lyricsTextView != null){
            lyricsTextView.setText(lyricsText);
            lyricsTextView.requestLayout();
            View view = getView();
            if(view != null){
                view.requestLayout();
            }
        }
    }

    private void updateVkAudio(){
        if(vkAudioTextView != null && vkApiAudio != null){
            vkAudioTextView.setText(vkApiAudio.title);
        }
    }
}
