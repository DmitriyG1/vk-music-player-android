package com.blogspot.colibriapps.inthemusic;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.blogspot.colibriapps.inthemusic.fragments.LoginFragment;
import com.blogspot.colibriapps.inthemusic.fragments.UserLoaderFragment;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

/**
 * Created by Dmitriy Gaiduk on 31.07.15.
 */
public class LoginActivity extends Activity implements UserLoaderFragment.IUserLoaderCallback {
    private final static String LOG_TAG = "LoginActivity";

    private boolean mainActivityWasShown = false;
    private boolean userStartLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ActionBar actionBar = getActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        /*String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        if(fingerprints != null){
            for(int i = 0; i < fingerprints.length; i++){
                Log.i(LOG_TAG, "fingerprints: " + fingerprints[i]);
            }
        }else {
            Log.i(LOG_TAG, "fingerprints is null");
        }*/

        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                Log.i(LOG_TAG, "VKSdk.LoginState: "  + res + ", isLoggedIn: " + VKSdk.isLoggedIn());

                switch (res) {
                    case LoggedOut:
                        showLogin();
                        break;
                    case LoggedIn:
                        loadUser("wakeUpSession LoggedIn");
                        break;
                    case Pending:
                        break;
                    case Unknown:
                        break;
                }
            }

            @Override
            public void onError(VKError error) {
                Log.e(LOG_TAG, "VKError: "  + error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Log.i(LOG_TAG, "VKAccessToken: " + res);
                // User passed Authorization
                loadUser("onActivityResult");
            }
            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
                Log.e(LOG_TAG, "onActivityResult.onError: " + error);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(LOG_TAG, "onResume, isLoggedIn: " + VKSdk.isLoggedIn());

        if (VKSdk.isLoggedIn()) {
            loadUser("onResume");
        } else {
            showLogin();
        }

    }

    private void showLogin(){
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new LoginFragment())
                .commit();
    }

    private void loadUser(String context){
        // TODO два раза вызывается метод loadUser(): onResume(), wakeUpSession

        if(userStartLoad){
            return;
        }
        userStartLoad = true;

        // запрашиваем информацию о пользователе
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new UserLoaderFragment())
                .commit();

        Log.i(LOG_TAG, "loadUser: " + context);
    }


    @Override
    public void loadFinished(boolean success) {
        Log.i(LOG_TAG, "loadFinished: " + success);

        if(success){
            if(!mainActivityWasShown){
                mainActivityWasShown = true;
                Intent intent = new Intent(LoginActivity.this, MainNavigationDrawerActivity.class);
                startActivity(intent);
            }else {
                // TODO показать ошибку подключения и показать кнопку Login
            }
        }
    }
}
