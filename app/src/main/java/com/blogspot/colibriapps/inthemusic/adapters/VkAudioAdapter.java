package com.blogspot.colibriapps.inthemusic.adapters;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.utils.TextUtil;
import com.vk.sdk.api.model.VKApiAudio;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 03.07.15.
 */
public class VkAudioAdapter extends BaseGenericAdapter<VKApiAudio> implements View.OnClickListener {

    private LyricsButtonClickListener mLyricsButtonClickListener = null;
    private final int mResource;

    private static final int COLOR_ENABLED = Color.argb(0xFF, 0x00, 0x00, 0x00);
    private static final int COLOR_DISABLED = Color.argb(0xFF, 0x99, 0x99, 0x99);

    public VkAudioAdapter(Context context,
                          int resource,
                          ArrayList<VKApiAudio> objects,
                          LyricsButtonClickListener lyricsButtonClickListener) {
        super(context, resource, objects);

        mLyricsButtonClickListener = lyricsButtonClickListener;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);
        }

        VKApiAudio vkApiAudio;
        vkApiAudio = (VKApiAudio)getItem(position);
        TextView textView;

        // title
        textView = (TextView) row.findViewById(R.id.audio_cell_title);
        textView.setText(vkApiAudio.title);

        // artist
        textView = (TextView) row.findViewById(R.id.audio_cell_artist);
        textView.setText(vkApiAudio.artist);

        textView = (TextView) row.findViewById(R.id.audio_cell_time);
        String timeText = TextUtil.makeTimeStringWithSeconds(vkApiAudio.duration);
        textView.setText(timeText);

        // button
        ImageButton imageButton;
        imageButton = (ImageButton) row.findViewById(R.id.audio_cell_lyrics_button);
        if(imageButton != null){
            imageButton.setEnabled(vkApiAudio.lyrics_id > 0);

            if(vkApiAudio.lyrics_id > 0){
                imageButton.setColorFilter(COLOR_ENABLED);
            }else {
                imageButton.setColorFilter(COLOR_DISABLED);
            }

            imageButton.setTag(position);
            imageButton.setOnClickListener(this);
        }

        return row;
    }

    @Override
    public void addAllGeneric(ArrayList<?> result) {
        addAll(result);
    }

    @Override
    public void onClick(View v) {

        if(mLyricsButtonClickListener != null){
            int position;
            position = (int)v.getTag();
            mLyricsButtonClickListener.onLyricsButtonClick(position);
        }
    }

    public interface LyricsButtonClickListener{
        void onLyricsButtonClick(int position);
    }
}
