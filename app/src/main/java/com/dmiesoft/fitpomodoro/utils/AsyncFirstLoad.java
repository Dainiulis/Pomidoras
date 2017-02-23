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
    private int oldOrientation;
    private AsyncFirstLoadListrener mListrener;

    public AsyncFirstLoad(Activity activity, ExercisesDataSource dataSource) {
        this.activity = activity;
        this.dataSource = dataSource;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (activity instanceof AsyncFirstLoadListrener) {
            mListrener = (AsyncFirstLoadListrener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement AsyncFirstLoadListrener");
        }
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("First time initialization...");
        progressDialog.show();
        oldOrientation = activity.getRequestedOrientation();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    @Override
    protected Void doInBackground(Void... params) {
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
        progressDialog.dismiss();
        activity.setRequestedOrientation(oldOrientation);
        mListrener.onPostFirstLoadExecute(exercisesGroups);
        super.onPostExecute(aVoid);
    }

    public interface AsyncFirstLoadListrener {
        void onPostFirstLoadExecute(List<ExercisesGroup> exercisesGroups);
    }
}
