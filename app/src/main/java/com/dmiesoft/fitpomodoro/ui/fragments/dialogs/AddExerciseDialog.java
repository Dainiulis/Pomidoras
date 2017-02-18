package com.dmiesoft.fitpomodoro.ui.fragments.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.utils.BitmapHelper;
import com.dmiesoft.fitpomodoro.utils.DisplayWidthHeight;
import com.dmiesoft.fitpomodoro.utils.EditTextInputFilter;
import com.dmiesoft.fitpomodoro.utils.FilePathGetter;

import java.util.ArrayList;
import java.util.List;

public class AddExerciseDialog extends DialogFragment {

    private static final int PICKFILE_RESULT_CODE = 2;
    private static final String PACKAGE_EXERCISE_ARRAY = "com.dmiesoft.fitpomodoro.model.Exercise.Array";
    private static final String EXERCISE_GROUP_ID_KEY = "EXERCISE_GROUP_ID_KEY";
    private static final String TAG = "AddED";

    private Button btnSave, btnCancel;
    private EditText editText;
    private RadioButton radioReps, radioTime;
    private RadioGroup radioGroup;
    private ImageView imageView;
    private ImageButton imageButton;
    private Bitmap bitmap;
    private View rootView;
    private Exercise exercise;
    private List<Exercise> exercises;
    private AddExerciseDialogListener mListener;
    private String exerciseType = "reps";
    private long exerciseGroupId;

    public AddExerciseDialog() {
    }

    public static AddExerciseDialog newInstance(List<Exercise> exercises, long exerciseGroupId) {

        Bundle args = new Bundle();
        AddExerciseDialog fragment = new AddExerciseDialog();
        args.putParcelableArrayList(PACKAGE_EXERCISE_ARRAY, (ArrayList<Exercise>) exercises);
        args.putLong(EXERCISE_GROUP_ID_KEY, exerciseGroupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddExerciseDialogListener) {
            mListener = (AddExerciseDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AddExerciseDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercises = getArguments().getParcelableArrayList(PACKAGE_EXERCISE_ARRAY);
            exerciseGroupId = getArguments().getLong(EXERCISE_GROUP_ID_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.add_exercise_dialog, container, false);

        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        editText = (EditText) rootView.findViewById(R.id.editExerciseText);
        imageView = (ImageView) rootView.findViewById(R.id.imageLogo);
        btnSave = (Button) rootView.findViewById(R.id.btnSave);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
        imageButton = (ImageButton) rootView.findViewById(R.id.btnImage);
        editText.setFilters(new InputFilter[]{new EditTextInputFilter(",./;{}[]|!@#$%^&<>?;")});
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Activity activity = getActivity();
                    if (activity instanceof MainActivity) {
                        if (((MainActivity) getActivity()).hasPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            startIntentGetContent();
                        } else {
                            ((MainActivity) getActivity()).requestPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.PERMISSIONS_REQUEST_R_W_STORAGE);
                        }
                    } else {
                        throw new RuntimeException("Error, not MainActivity!");
                    }
                } else {
                    startIntentGetContent();
                }

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString().trim();
                if (name.equals("")) {
                    Toast.makeText(getActivity(), "Please enter exercise name...", Toast.LENGTH_SHORT).show();
                } else if (doesExerciseExist(name)) {
                    Toast.makeText(getActivity(), "Exercise " + name + " already exist", Toast.LENGTH_SHORT).show();
                } else {
                    saveExercise(name);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioReps:
                        exerciseType = "reps";
                        break;
                    case R.id.radioTime:
                        exerciseType = "time";
                        break;
                }
            }
        });

        return rootView;
    }

    private void saveExercise(String name) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        if (bitmap != null) {
            exercise.setImage("[" + name + ".png");
            BitmapHelper.saveImage("[" + name + ".png", bitmap, getContext());
        }
        exercise.setType(exerciseType);
        exercise.setExercise_group_id(exerciseGroupId);
        mListener.onSaveExerciseClicked(exercise);
        dismiss();

    }

    private void startIntentGetContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    private boolean doesExerciseExist(String name) {
        if (exercises.size() <= 0) {
            return false;
        }
        for (Exercise currentExercise : exercises) {
            if (currentExercise.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayWidthHeight display = new DisplayWidthHeight(getActivity());
        int width = (int) display.getWidth();
        float density = getActivity().getResources().getDisplayMetrics().density;
        int dp = (int) (width / density);
        Window window = getDialog().getWindow();
        assert window != null;
        window.setGravity(Gravity.CENTER);
        if (dp < 600) {
            window.setLayout((9 * width) / 10, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            window.setLayout((5 * width) / 10, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public interface AddExerciseDialogListener {
        void onSaveExerciseClicked(Exercise exercise);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == PICKFILE_RESULT_CODE) {
                Uri uri = data.getData();
                FilePathGetter pathGetter = new FilePathGetter(getContext());
                String path = pathGetter.getPath(uri);
                bitmap = BitmapHelper.decodeBitmapFromPath(path, BitmapHelper.REQUIRED_WIDTH, BitmapHelper.REQUIRED_HEIGTH);
                //bugas ant samsung cyanogenmode
                if (bitmap == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(getActivity(), "Could not load image please try again...", Toast.LENGTH_SHORT).show();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                bitmap = BitmapHelper.getScaledBitmap(bitmap, BitmapHelper.MAX_SIZE);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("BITMAP", bitmap);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            bitmap = savedInstanceState.getParcelable("BITMAP");
            imageView.setImageBitmap(bitmap);
        }
    }
}
