package com.dmiesoft.fitpomodoro.ui.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private ImageView imageView, iVeditImageNameType, iVeditDescription;
    private TextView nameTextView, typeTextView, descriptionText;
    private RelativeLayout imageLayout;
    private int color;
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
        
        view = inflater.inflate(R.layout.fragment_exercise_detail, container, false);
        initializeViews();
        return view;
    }

    private void initializeViews() {
        iVeditImageNameType = (ImageView) view.findViewById(R.id.edit_image_name_type);
        iVeditDescription = (ImageView) view.findViewById(R.id.edit_description);
        nameTextView = (TextView) view.findViewById(R.id.nameExercise);
        typeTextView = (TextView) view.findViewById(R.id.typeExercise);
        descriptionText = (TextView) view.findViewById(R.id.descriptionText);
        imageView = (ImageView) view.findViewById(R.id.imageExercise);
        imageLayout = (RelativeLayout) view.findViewById(R.id.imageLayout);

        refreshDisplay();

        iVeditImageNameType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEditExerciseLongClicked(exercise, false);
            }
        });

        iVeditDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEditExerciseLongClicked(exercise, true);
            }
        });

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
        color = generateColor();
        int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.exercise_detail_image);
        imageLayout.setBackgroundColor(color);
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

    public int getColor() {
        return color;
    }

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
        void onEditExerciseLongClicked(Exercise exercise, boolean description);
    }

}
