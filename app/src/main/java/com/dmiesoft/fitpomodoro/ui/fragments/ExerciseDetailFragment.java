package com.dmiesoft.fitpomodoro.ui.fragments;


import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.utils.BitmapHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseDetailFragment extends Fragment {

    private static final String PACKAGE_EXERCISES = "com.dmiesoft.fitpomodoro.model.Exercise";
    private static final String TAG = "EDF";
    private Exercise exercise;
    private View view;
    private ImageView imageView;
    private TextView nameTextView, typeTextView, descriptionTextView;
    private LinearLayout imageLayout;
    private FloatingActionButton mainFab;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            exercise = getArguments().getParcelable(PACKAGE_EXERCISES);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        view = inflater.inflate(R.layout.fragment_exercise_detail, container, false);
        initializeViews();
        return view;
    }

    private void initializeViews() {
        nameTextView = (TextView) view.findViewById(R.id.nameExercise);
        typeTextView = (TextView) view.findViewById(R.id.typeExercise);
        descriptionTextView = (TextView) view.findViewById(R.id.descriptionExercise);
        imageView = (ImageView) view.findViewById(R.id.imageExercise);
        imageLayout = (LinearLayout) view.findViewById(R.id.imageLayout);

        nameTextView.setText(exercise.getName());
        typeTextView.setText(exercise.getType());
        descriptionTextView.setText(exercise.getDescription());

        int color = getColor();
        int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.exercise_detail_image);
        if (exercise.getImage() != null) {
            Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercise.getImage(), true, resourceDimen);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                // perdaryt
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resourceDimen);
                imageView.setLayoutParams(params);
                TextDrawable drawable = TextDrawable.builder().buildRect(exercise.getName(), color);
                imageView.setImageDrawable(drawable);
            }
        } else {
            // perdaryt
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resourceDimen);
            imageView.setLayoutParams(params);
            TextDrawable drawable = TextDrawable.builder().buildRect(exercise.getName(), color);
            imageView.setImageDrawable(drawable);
        }
    }

    private int getColor() {
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
        mainFab = ((MainActivity) getActivity()).getMainFab();
        if(mainFab != null) {
            mainFab.hide();
        }
    }

    private Drawable getDrawableFromAssets(String image) {
        AssetManager assetManager = getContext().getAssets();
        InputStream stream = null;

        try {
            stream = assetManager.open(image + ".png");
            Drawable drawable = Drawable.createFromStream(stream, null);
            return drawable;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().invalidateOptionsMenu();
    }
}
