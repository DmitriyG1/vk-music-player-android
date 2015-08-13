package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import com.blogspot.colibriapps.inthemusic.R;
import com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base.BaseAudioSimpleListFragment;
import com.blogspot.colibriapps.inthemusic.user.CacheData;
import com.blogspot.colibriapps.inthemusic.vkLoaders.SearchLoader;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 22.06.15.
 */
public class VKSearchFragment extends BaseAudioSimpleListFragment
        implements SearchView.OnQueryTextListener,
        MenuItem.OnActionExpandListener,
        View.OnFocusChangeListener{

    private static final String LOG_TAG = "VKSearchFragment";

    private static final String PLAYLIST_NAME = "search";

    private boolean mIsExpand;
    public VKSearchFragment(){
    }

    // строка поиска
    private String mSearchText;


    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSearchText = CacheData.getInstance().getSearchQuery();

        // вызов onCreateOptionsMenu()
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.vk_search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.vk_search);
        menuItem.setOnActionExpandListener(this);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextFocusChangeListener(this);
        searchView.setQuery(mSearchText, false);
        searchView.setIconifiedByDefault(false);
    }

    // ======== SearchView.OnQueryTextListener
    public boolean onQueryTextChange(String newText) {
        Log.i(LOG_TAG, "onQueryTextChange, Search text: " + newText + ", mIsExpand: " + mIsExpand);
        if(!mIsExpand){
            return true;
        }

        mSearchText = !TextUtils.isEmpty(newText) ? newText : null;
        startRefreshing();
        getLoaderManager().restartLoader(LOADER_ID, makeLoaderArgs(false), this);
        return true;
    }

    @Override public boolean onQueryTextSubmit(String query) {
        // ничего не делаем
        return true;
    }

    // ========  LoaderManager.LoaderCallbacks
    public Loader<ArrayList<?>> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "onCreateLoader");
        return new SearchLoader(getActivity(), args);
    }


    // === BaseAudioSimpleListFragment
    @Override
    public Bundle makeLoaderArgs(boolean forceLoad) {
        Bundle bundle = new Bundle();
        bundle.putString(SearchLoader.PARAM_PLAYLIST_NAME, getPlayListName());
        bundle.putString(SearchLoader.ARGS_SEARCH_STRING, mSearchText);
        bundle.putBoolean(SearchLoader.FORCE_LOAD, forceLoad);

        return bundle;
    }

    @Override
    protected String getPlayListName() {
        return PLAYLIST_NAME;
    }

    // ======== View.OnFocusChangeListener
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if(!(view instanceof SearchView)){
            return;
        }

        mIsExpand = hasFocus;

        SearchView searchView = (SearchView)view;
        ViewGroup.LayoutParams lp = searchView.getLayoutParams();
        if(lp != null){
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        searchView.setQuery(mSearchText, false);
    }

    // ======== MenuItem.OnActionExpandListener
    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        mIsExpand = false;
        return true;
    }
}
