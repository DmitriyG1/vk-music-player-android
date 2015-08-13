package com.blogspot.colibriapps.inthemusic.drawerFragments.audioFragments.base;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import com.blogspot.colibriapps.inthemusic.adapters.BaseGenericAdapter;
import java.util.ArrayList;

/**
 * Created by Dmitriy Gaiduk on 21.07.15.
 */
public abstract class BaseVkLoaderFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<ArrayList<?>>,
        SwipeRefreshLayout.OnRefreshListener{

    private static final String LOG_TAG = "BaseVkLoaderFragment";

    private SwipeRefreshLayout mSwipeRefreshLayout;

    protected BaseGenericAdapter<?> mAdapter;

    // id загрузчика, уникальный для конкретной Activity или Fragment
    protected static final int LOADER_ID = 1;

    public BaseVkLoaderFragment(){
        // Пустой конструктор требуется для подклассов фрагментов
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(LOG_TAG, "onActivityCreated");


        mSwipeRefreshLayout = getSwipeRefreshLayout();
        if(mSwipeRefreshLayout != null){
            mSwipeRefreshLayout.setOnRefreshListener(this);
        }

        mAdapter = initListViewAndMakeAdapter();
        // TODO второй вариант: хранить данные при повороте экрана в savedInstanceState
        //if(savedInstanceState == null){

        //}
    }

    abstract protected Bundle makeLoaderArgs(boolean forceLoad);
    abstract protected BaseGenericAdapter initListViewAndMakeAdapter();
    abstract protected SwipeRefreshLayout getSwipeRefreshLayout();
    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");

        // Вызываем здесь initloader(), иначе дважды приходит onLoadFinished()
        startRefreshing();
        getLoaderManager().initLoader(LOADER_ID, makeLoaderArgs(false), this);
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, "onPause");

        stopRefreshing();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");

        stopRefreshing();

        super.onDestroy();
    }

    // =====  LoaderManager.LoaderCallbacks
    public abstract Loader<ArrayList<?>> onCreateLoader(int id, Bundle args);

    public void onLoadFinished(Loader<ArrayList<?>> loader, ArrayList<?> result){
        Log.i(LOG_TAG, "onLoadFinished");

        mAdapter.clear();
        if(result != null){
            mAdapter.addAllGeneric(result);
        }
        stopRefreshing();
    }


    public void onLoaderReset(Loader<ArrayList<?>> loader){
        mAdapter.clear();
    }

    //==

     @Override
    public void onRefresh() {
         getLoaderManager().restartLoader(LOADER_ID, makeLoaderArgs(true), this);
     }

    // == refresh
    protected void startRefreshing(){
        mSwipeRefreshLayout.post(mRunnableStartRefresh);
    }

    private void stopRefreshing(){
        mSwipeRefreshLayout.removeCallbacks(mRunnableStartRefresh);
        mSwipeRefreshLayout.setRefreshing(false);
        
        // чтобы не оставалось изображение на экране при смене фрагмента
        mSwipeRefreshLayout.destroyDrawingCache();
        mSwipeRefreshLayout.clearAnimation();
    }

    private final Runnable mRunnableStartRefresh = new Runnable() {
        @Override
        public void run() {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    };

}
