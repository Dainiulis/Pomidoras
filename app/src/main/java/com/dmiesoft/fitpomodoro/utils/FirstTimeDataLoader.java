package com.dmiesoft.fitpomodoro.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class FirstTimeDataLoader implements LoaderManager.LoaderCallbacks<List<ExercisesGroup>>{

    public static final int FIRST_TIME_LOADER = 1001;
    private static final String TAG = "FTDL";

    public static boolean isLoading = false;

    private Context context;
    private LoaderManager loaderManager;
    private ProgressDialog pD;
    private AsyncFirstDataLoaderListener mListener;

    public FirstTimeDataLoader(Context context, LoaderManager loaderManager) {
        this.context = context;
        this.loaderManager = loaderManager;

        if (context instanceof AsyncFirstDataLoaderListener) {
            mListener = (AsyncFirstDataLoaderListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AsyncFirstDataLoaderListener");
        }

    }
    public void startLoader() {
        pD = new ProgressDialog(context);
        pD.setMessage("Generating exercises...");
        pD.show();
        loaderManager.initLoader(FIRST_TIME_LOADER, null, this).forceLoad();
        isLoading = true;
    }

    @Override
    public Loader<List<ExercisesGroup>> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "starting loader");
        return new AsyncDataLoader(context);
    }

    @Override
    public void onLoadFinished(Loader<List<ExercisesGroup>> loader, List<ExercisesGroup> data) {
        pD.dismiss();
        mListener.onFirstLoadFinished(data);
        isLoading = false;
    }

    @Override
    public void onLoaderReset(Loader<List<ExercisesGroup>> loader) {

    }

    private static class AsyncDataLoader extends AsyncTaskLoader<List<ExercisesGroup>> {

        public AsyncDataLoader(Context context) {
            super(context);
        }

        @Override
        public List<ExercisesGroup> loadInBackground() {
            List<ExercisesGroup> exercisesGroups = null;
            InitialDatabasePopulation idp = new InitialDatabasePopulation(getContext());
            try {
                exercisesGroups = idp.readJson();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return exercisesGroups;
        }
    }

    public interface AsyncFirstDataLoaderListener {
        void onFirstLoadFinished(List<ExercisesGroup> exercisesGroups);
    }

}
