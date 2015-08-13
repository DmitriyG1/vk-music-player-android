package com.blogspot.colibriapps.inthemusic.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.vkontakte.VKAudioAlbum;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 15.07.15.
 */
public class VkAlbumAdapter extends BaseGenericAdapter<VKAudioAlbum> {
    private final int mResource;

    public VkAlbumAdapter(Context context,
                          int resource,
                          ArrayList<VKAudioAlbum> objects) {
        super(context, resource, objects);
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

        VKAudioAlbum vkAudioAlbum;
        vkAudioAlbum = (VKAudioAlbum)getItem(position);
        TextView textView;

        // title
        textView = (TextView) row.findViewById(R.id.audio_cell_title);
        textView.setText(vkAudioAlbum.title);

        return row;
    }

    @Override
    public void addAllGeneric(ArrayList<?> result) {
        addAll(result);
    }
}
