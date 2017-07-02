package com.dmiesoft.fitpomodoro.ui.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseDetailFragment extends Fragment {

    private static final String PACKAGE_EXERCISES = "com.dmiesoft.fitpomodoro.model.Exercise";
    private static final String TAG = "EDF";
    private Exercise exercise;
    private View view;
    private ImageView imageView;
    private TextView nameTextView, typeTextView, descriptionText;
    private ExerciseDetailFragmentListener mListener;

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
        if (context instanceof ExerciseDetailFragmentListener) {
            mListener = (ExerciseDetailFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ExerciseDetailFragmentListener");
        }
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
        nameTextView = (TextView) view.findViewById(R.id.nameExercise);
        typeTextView = (TextView) view.findViewById(R.id.typeExercise);
        descriptionText = (TextView) view.findViewById(R.id.descriptionText);
        imageView = (ImageView) view.findViewById(R.id.imageExercise);

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
                mListener.onEditExerciseDescClicked(exercise, false);
                break;

            case R.id.menu_edit_description:
                mListener.onEditExerciseDescClicked(exercise, true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set exercise and refresh display
     * @param exercise
     *
     */
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
        refreshDisplay();
    }

    private void refreshDisplay() {
        nameTextView.setText(exercise.getName());
        typeTextView.setText(exercise.getType());

        descriptionText.setText(exercise.getDescription());
        int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.exercise_detail_image);
        if (exercise.getImage() != null) {
            Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercise.getImage(), true, resourceDimen);
            if (bitmap != null) {
                bitmap = BitmapHelper.getCroppedBitmap(bitmap, BitmapHelper.BORDER_SIZE);
                imageView.setImageBitmap(bitmap);
                if (imageView.getVisibility() == View.GONE) {
                    imageView.setVisibility(View.VISIBLE);
                }
            } else {
                imageView.setVisibility(View.GONE);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

//    public int getColor() {
//        return color;
//    }

    private int generateColor() {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        return generator.getColor(exercise.getName());
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

    public interface ExerciseDetailFragmentListener{
        void onEditExerciseDescClicked(Exercise exercise, boolean description);
    }

}
