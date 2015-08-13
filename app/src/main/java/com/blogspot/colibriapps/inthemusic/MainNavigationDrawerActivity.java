package com.blogspot.colibriapps.inthemusic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.blogspot.colibriapps.inthemusic.drawerFragments.SettingsFragment;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.AlbumAudioListFragment;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.AlbumsListFragment;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.AudioFragmentConsts;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.AudioListFragment;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.NowPlayingFragment;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.PopularAudioListFragment;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.VKSearchFragment;
import com.blogspot.colibriapps.inthemusic.navigation.INavigation;

/**
 * Created by Dmitriy Gaiduk on 19.06.15.
 */
public class MainNavigationDrawerActivity extends Activity implements INavigation {
    public static final String FROM_NOTIFICATION_PARAM = "FROM_NOTIFICATION_PARAM";

    private static final String KEY_TITLE = "KEY_TITLE";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMenuTitles;

    private Runnable mPendingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_drawer_layout);

        mDrawerTitle = getTitle();

        // текущий заголовок
        if(savedInstanceState == null){
            mTitle = getTitle();
        }else {
            mTitle = savedInstanceState.getCharSequence(KEY_TITLE);
            setTitle(mTitle);
        }

        Log.i("DrawerToggle", "title: " + mTitle);

        mMenuTitles = getResources().getStringArray(R.array.main_menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mMenuTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        // enable ActionBar app icon to behave as action to toggle nav drawer
        if(getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                // If mPendingRunnable is not null, then add to the message queue
                if (mPendingRunnable != null) {
                    Handler mHandler = new Handler();
                    mHandler.post(mPendingRunnable);
                    mPendingRunnable = null;
                }
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);


        if(savedInstanceState == null){
            // первый запуск
            selectItem(0);
        }
    }


    @Override
    protected void onNewIntent(Intent intent){
        boolean fromNotification;
        fromNotification = intent.getBooleanExtra(FROM_NOTIFICATION_PARAM, false);
        // если открыли из области уведомлений, показываем фрагмент Сейчас играет
        if(fromNotification){
            selectItem(4);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // сохраняем текущий заголовок
        CharSequence title = mDrawerLayout.isDrawerOpen(GravityCompat.START) ?
                mDrawerTitle : mTitle;
        outState.putCharSequence(KEY_TITLE, title);
    }

        @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if(getActionBar() != null){
            getActionBar().setTitle(mTitle);
        }
    }

    private void selectItem(int position) {

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);


        // update the main content by replacing fragments
        final int pos = position;
        mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = null;
                switch (pos){
                    case 0:
                        fragment = new AudioListFragment();
                        break;
                    case 1:
                        fragment = new AlbumsListFragment();
                        break;
                    case 2:
                        fragment = new PopularAudioListFragment();
                        break;
                    case 3:
                        fragment = new VKSearchFragment();
                        break;
                    case 4:
                        fragment = new NowPlayingFragment();
                        break;
                    case 5:
                        fragment = new SettingsFragment();
                        break;
                }

                if(fragment == null){
                    return;
                }

                setFragmentWithTitle(fragment, mMenuTitles[pos]);
            }
        };

        if(!mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            Handler mHandler = new Handler();
            mHandler.post(mPendingRunnable);
            mPendingRunnable = null;
        }
    }



    private void setFragmentWithTitle(Fragment fragment, String title){
        if(fragment == null){
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        setTitle(title);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    @Override
    public void navigateTo(String itemName, Bundle bundle) {
        switch (itemName){
            case AudioFragmentConsts.ALBUM_AUDIOS_LIST:
                AlbumAudioListFragment albumAudioListFragment;
                albumAudioListFragment = new AlbumAudioListFragment();
                albumAudioListFragment.setInitParams(bundle);
                setFragmentWithTitle(albumAudioListFragment, "Альбом");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //  не возвращаемся на страницу логина
        // показываем боковое меню
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

}
