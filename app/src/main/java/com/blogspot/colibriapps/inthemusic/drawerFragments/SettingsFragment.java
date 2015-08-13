package com.blogspot.colibriapps.inthemusic.drawerFragments;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.blogspot.colibriapps.inthemusic.LoginActivity;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.musicplayer.MusicService;
import com.blogspot.colibriapps.inthemusic.musicplayer.playlist.PlayListManager;
import com.blogspot.colibriapps.inthemusic.user.CacheData;
import com.blogspot.colibriapps.inthemusic.user.UserPreferences;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.model.VKApiUser;

/**
 * Created by Dmitriy Gaiduk on 30.07.15.
 */
public class SettingsFragment extends Fragment implements AdapterView.OnItemClickListener,
        DialogInterface.OnClickListener{

    private static final String TAG = "SettingsFragment";

    public SettingsFragment() {
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.settings_layout, container, false);

        ListView listView;
        listView = (ListView)view.findViewById(R.id.settings_list);



        // Меню
        String[] titles;
        titles = new String[2];

        // Оценить приложение
        titles[0] = getString(R.string.rate_app);

        // сменить пользователя
        String str;
        VKApiUser vkApiUser;
        vkApiUser =  UserPreferences.getInstance().getVKApiUser();
        str = getString(R.string.logout);
        str += "\n";
        str += getString(R.string.user) + ": ";
        str += vkApiUser.first_name + " " +  vkApiUser.last_name;
        titles[1] = str;

        // версия
        PackageInfo pInfo = null;
        try{
            pInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        if(pInfo != null){
            Log.i(TAG, "version: " + pInfo.versionName + ", code: " + pInfo.versionCode);
            TextView textView;
            textView = (TextView) view.findViewById(R.id.version_text);
            textView.setText(getString(R.string.version) + ": " + pInfo.versionName);
        }else {
            Log.i(TAG, "version is null");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.settings_list_item,
                titles);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                rateApp();
                break;
            case 1:
                logOut();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case Dialog.BUTTON_POSITIVE:
                VKSdk.logout();

                if (!VKSdk.isLoggedIn()) {
                    // останавливаем проигрывание музыки
                    Intent musicServiceIntent = new Intent(getActivity(), MusicService.class);
                    musicServiceIntent.setAction(MusicService.ACTION_STOP);
                    getActivity().startService(musicServiceIntent);

                    // очищаем кэш
                    PlayListManager.getInstance().clearAll();
                    CacheData.getInstance().clearAll();
                    // сброс настроек
                    UserPreferences.getInstance().reset();

                    // переходим в окно логина
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private void rateApp(){
        boolean successIntent = false;
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Log.i(TAG, "PackageName: " + getActivity().getPackageName() + ", market uri: " + uri);

        try {
            startActivity(intent);
            successIntent = true;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            successIntent = false;
        }

        if(!successIntent){
            uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
            intent = new Intent(Intent.ACTION_VIEW, uri);
            Log.i(TAG, "PackageName: " + getActivity().getPackageName() + ", http uri: " + uri);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getString(R.string.google_play_not_found),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void logOut(){
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        // сообщение
        adb.setMessage(getString(R.string.sure_logout_question));
        // иконка
        adb.setIcon(android.R.drawable.ic_dialog_info);
        // кнопка положительного ответа
        adb.setPositiveButton(getString(R.string.yes), this);
        // кнопка отрицательного ответа
        adb.setNegativeButton(getString(R.string.no), this);

        adb.show();
    }
}
