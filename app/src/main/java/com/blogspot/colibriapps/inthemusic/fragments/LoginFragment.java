package com.blogspot.colibriapps.inthemusic.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.blogspot.colibriapps.inthemusic.R;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

/**
 * Created by Dmitriy Gaiduk on 03.08.15.
 */
public class LoginFragment extends Fragment implements View.OnClickListener{
    private static final String[] sMyScope = new String[]{VKScope.AUDIO};

    public LoginFragment() {
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        view = inflater.inflate(R.layout.login_fragment, container, false);

        Button button;
        button = (Button)view.findViewById(R.id.login_button);
        button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_button:
                onLoginClick();
                break;
        }
    }

    private void onLoginClick(){
        VKSdk.login(this, sMyScope);

    }
}
