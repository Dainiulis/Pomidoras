package com.dmiesoft.fitpomodoro.ui.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.InitialDatabasePopulation;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesGroupListAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;


public class ExerciseGroupFragment extends ListFragment {

    private static final String TAG = "EXERCISEGROUP";
    private ExercisesDataSource dataSource;
    private List<ExercisesGroup> exercisesGroups;
    private ExercisesGroupListAdapter adapter;



    public ExerciseGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataSource = new ExercisesDataSource(getContext());
        dataSource.open();

        exercisesGroups = dataSource.findAllExerciseGroups();
        if (exercisesGroups.size() == 0) {

            new FirstDbInitTask().execute();

//            InitialDatabasePopulation idp = new InitialDatabasePopulation(getContext(), dataSource);
//            try {
//                exercisesGroups = idp.readJson();
//                Log.i(TAG, "onCreate: " + exercisesGroups);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_exercise_group, container, false);
        adapter = new ExercisesGroupListAdapter(getContext(), R.layout.list_exercise_groups, exercisesGroups);
        setListAdapter(adapter);
        Log.i(TAG, "onCreateView: " + adapter.getCount());

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        dataSource.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        dataSource.open();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach: Exercise");
    }

    private class FirstDbInitTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog alert;

        @Override
        protected void onPreExecute() {

            alert = new ProgressDialog(getContext());
            alert.setMessage("Loading database...");
            alert.show();
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "doInBackground: ");
            InitialDatabasePopulation idp = new InitialDatabasePopulation(getContext(), dataSource);
            try {
                exercisesGroups = idp.readJson();
                Log.i(TAG, "onCreate: " + exercisesGroups);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "onPostExecute: ");
            adapter.addAll(exercisesGroups);
            adapter.notifyDataSetChanged();
            alert.dismiss();
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

}
