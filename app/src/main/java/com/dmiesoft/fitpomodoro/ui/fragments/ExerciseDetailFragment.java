package com.dmiesoft.fitpomodoro.ui.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExerciseHistory;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.dialogs.AddExerciseDialog;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedExerciseDescriptionFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedExerciseHistoryListFragment;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

import java.util.List;
import java.util.Vector;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseDetailFragment extends Fragment {

    private static final String PACKAGE_EXERCISES = "com.dmiesoft.fitpomodoro.model.Exercise";
    private static final String TAG = "EDF";
    private Exercise exercise;
    private View view;
    private ImageView mImageView;
    private TextView mNameTextView, mTypeTextView; //, descriptionText;
    private ViewPager mPager;

    public ExerciseDetailFragment() {
        // Required empty public constructor
    }

    public static ExerciseDetailFragment newInstance(Exercise exercise) {

        Bundle args = new Bundle();
        ExerciseDetailFragment fragment = new ExerciseDetailFragment();
        args.putParcelable(PACKAGE_EXERCISES, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            exercise = getArguments().getParcelable(PACKAGE_EXERCISES);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_exercise_detail, container, false);
        initializeViews();
        return view;
    }

    private void initializeViews() {
        mNameTextView = (TextView) view.findViewById(R.id.nameExercise);
        mTypeTextView = (TextView) view.findViewById(R.id.typeExercise);
//        descriptionText = (TextView) view.findViewById(R.id.descriptionText);
        mImageView = (ImageView) view.findViewById(R.id.imageExercise);
        mPager = (ViewPager) view.findViewById(R.id.pager);

        refreshDisplay();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.menu_edit_image_name_type:
                editExercise(AddExerciseDialog.EDIT_IMAGE_LAYOUT);
                break;

            case R.id.menu_edit_description:
                editExercise(AddExerciseDialog.EDIT_DESCRIPTION);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void editExercise(int editCode) {
        List<Exercise> exercises = ExercisesDataSource.findExercises(getContext(), null, null);
        AddExerciseDialog dialog = AddExerciseDialog.newInstance(exercises, exercise, exercise.getExerciseGroupId(), editCode);
        dialog.setCancelable(false);
        dialog.show(getChildFragmentManager(), MainActivity.ADD_EXERCISE_DIALOG);
    }

    /**
     * Set exercise and refresh display
     *
     * @param exercise
     */
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
        refreshDisplay();
    }

    private void refreshDisplay() {
        mNameTextView.setText(exercise.getName());
        mTypeTextView.setText(exercise.getType());

        ExercisePagerAdapter adapter = new ExercisePagerAdapter(getChildFragmentManager(), exercise);
        mPager.setAdapter(adapter);
//        descriptionText.setText(exercise.getDescription());
        int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.exercise_detail_image);
        if (exercise.getImage() != null) {
            Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercise.getImage(), true, resourceDimen);
            if (bitmap != null) {
                bitmap = BitmapHelper.getCroppedBitmap(bitmap, BitmapHelper.BORDER_SIZE);
                mImageView.setImageBitmap(bitmap);
                if (mImageView.getVisibility() == View.GONE) {
                    mImageView.setVisibility(View.VISIBLE);
                }
            } else {
                mImageView.setVisibility(View.GONE);
            }
        } else {
            mImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().invalidateOptionsMenu();
    }

    private class ExercisePagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragments;

        public ExercisePagerAdapter(FragmentManager fm, Exercise exercise) {
            super(fm);
            List<ExerciseHistory> exerciseHistoryList = ExercisesDataSource.getExerciseHistory(getContext(), exercise.getId());
            fragments = new Vector<>();
            fragments.add(NestedExerciseDescriptionFragment.newInstance(exercise));
            fragments.add(NestedExerciseHistoryListFragment.newInstance(exerciseHistoryList, 1));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }


}
