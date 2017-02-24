package com.dmiesoft.fitpomodoro.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;

import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AsyncFirstLoad extends AsyncTask<Void, Void, Void>{

    private List<ExercisesGroup> exercisesGroups;
    private ProgressDialog progressDialog;
    private Activity activity;
    private ExercisesDataSource dataSource;
    private AsyncFirstLoadListrener mListrener;
    private ProgressDialog pD;

    public AsyncFirstLoad(Activity activity, ExercisesDataSource dataSource, ProgressDialog pD) {
        this.activity = activity;
        this.dataSource = dataSource;
        this.pD = pD;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (activity instanceof AsyncFirstLoadListrener) {
            mListrener = (AsyncFirstLoadListrener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement AsyncFirstLoadListrener");
        }
        mListrener.onPostFirstLoadExecute(null, pD);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //for avoiding database prepopulation
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InitialDatabasePopulation idp = new InitialDatabasePopulation(activity, dataSource);
        try {
            exercisesGroups = idp.readJson();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mListrener.onPostFirstLoadExecute(exercisesGroups, pD);
        super.onPostExecute(aVoid);
    }

    public interface AsyncFirstLoadListrener {
        void onPostFirstLoadExecute(List<ExercisesGroup> exercisesGroups, ProgressDialog pD);
    }
}
