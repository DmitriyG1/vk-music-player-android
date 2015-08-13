package com.blogspot.colibriapps.inthemusic.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 27.07.15.
 */
public abstract class BaseGenericAdapter<T> extends ArrayAdapter {
    public BaseGenericAdapter(Context context,
                          int resource,
                          ArrayList<T> objects) {
        super(context, resource, objects);
    }

    abstract public void addAllGeneric(ArrayList<?> result);
}
