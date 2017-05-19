package com.dmiesoft.fitpomodoro.utils.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.ExerciseHistory;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedExerciseHistoryListFragment.OnListFragmentInteractionListener;

import java.util.List;

public class ExerciseHistoryRecyclerViewAdapter extends RecyclerView.Adapter<ExerciseHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<ExerciseHistory> mExerciseHistory;
    private final OnListFragmentInteractionListener mListener;

    public ExerciseHistoryRecyclerViewAdapter(List<ExerciseHistory> exerciseHistory, OnListFragmentInteractionListener listener) {
        mExerciseHistory = exerciseHistory;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_exercise_history_nested, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mExerciseDetails = mExerciseHistory.get(position);
        holder.mExerciseName.setText(mExerciseHistory.get(position).getName());
        holder.mHowMany.setText(String.valueOf(mExerciseHistory.get(position).getHowMany()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mExerciseDetails);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mExerciseHistory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mExerciseName;
        public final TextView mHowMany;
        public ExerciseHistory mExerciseDetails;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mExerciseName = (TextView) view.findViewById(R.id.exerciseName);
            mHowMany = (TextView) view.findViewById(R.id.howMany);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mHowMany.getText() + "'";
        }
    }
}
