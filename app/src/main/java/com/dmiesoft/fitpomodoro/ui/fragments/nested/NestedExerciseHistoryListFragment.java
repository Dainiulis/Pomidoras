package com.dmiesoft.fitpomodoro.ui.fragments.nested;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.events.exercises.UpdateNestedExerciseHistoryEvent;
import com.dmiesoft.fitpomodoro.model.ExerciseHistory;
import com.dmiesoft.fitpomodoro.utils.adapters.ExerciseHistoryRecyclerViewAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class NestedExerciseHistoryListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String EXERCISES_HISTORY_LIST = "exercises_history_list";
    private static final String TAG = "NEHLF";
    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;
    private List<ExerciseHistory> mExerciseHistoryList;
    private ExerciseHistoryRecyclerViewAdapter adapter;

    public NestedExerciseHistoryListFragment() {
    }

    @SuppressWarnings("unused")
    public static NestedExerciseHistoryListFragment newInstance(List<ExerciseHistory> exerciseHistoryList, int columnCount) {
        NestedExerciseHistoryListFragment fragment = new NestedExerciseHistoryListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putParcelableArrayList(EXERCISES_HISTORY_LIST, (ArrayList<ExerciseHistory>) exerciseHistoryList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mExerciseHistoryList = getArguments().getParcelableArrayList(EXERCISES_HISTORY_LIST);
        }
        if (mExerciseHistoryList == null) {
            mExerciseHistoryList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_history_list, container, false);
        Log.i(TAG, "onCreateView: ");
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new ExerciseHistoryRecyclerViewAdapter(mExerciseHistoryList, mListener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    public void setExerciseHistory(ExerciseHistory exerciseHistory) {
        mExerciseHistoryList.add(0, exerciseHistory);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onExerciseHistoryUpdate(UpdateNestedExerciseHistoryEvent event) {
        setExerciseHistory(event.getExerciseHistory());
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ExerciseHistory exercise);
    }
}
