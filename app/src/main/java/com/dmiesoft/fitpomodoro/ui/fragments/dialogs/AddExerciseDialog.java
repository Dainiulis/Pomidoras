package com.dmiesoft.fitpomodoro.ui.fragments.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.ExerciseDetailFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesFragment;
import com.dmiesoft.fitpomodoro.utils.helpers.AlertDialogHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.DisplayHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.EditTextInputFilter;
import com.dmiesoft.fitpomodoro.utils.helpers.FilePathGetter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddExerciseDialog extends DialogFragment {

    private static final int PICKFILE_RESULT_CODE = 2;
    private static final String PACKAGE_EXERCISE_ARRAY = "com.dmiesoft.fitpomodoro.model.Exercise.Array";
    private static final String PACKAGE_EXERCISE = "com.dmiesoft.fitpomodoro.model.Exercise";
    private static final String EXERCISE_GROUP_ID_KEY = "EXERCISE_GROUP_ID_KEY";
    private static final String EDIT_CODE = "EDIT_CODE";
    private static final String TAG = "AddED";
    public static final int NO_EDIT = 1001;
    public static final int EDIT_DESCRIPTION = 1002;
    public static final int EDIT_IMAGE_LAYOUT = 1003;
    private static final String BITMAP = "BITMAP";

    private Button btnSave, btnCancel;
    private EditText editText, editDescription;
    private LinearLayout imageLayout;
    private RadioButton radioReps, radioTime;
    private RadioGroup radioGroup;
    private ImageView imageView;
    private ImageButton imageButton;
    private Bitmap bitmap;
    private View rootView;
    private Exercise exercise;
    private List<Exercise> exercises;
    private String exerciseType = "reps";
    private long exerciseGroupId;
    private int edit;
    private String path;

    public AddExerciseDialog() {
    }

    public static AddExerciseDialog newInstance(List<Exercise> exercises, Exercise exercise, long exerciseGroupId, int edit) {

        Bundle args = new Bundle();
        AddExerciseDialog fragment = new AddExerciseDialog();
        args.putParcelable(PACKAGE_EXERCISE, exercise);
        args.putParcelableArrayList(PACKAGE_EXERCISE_ARRAY, (ArrayList<Exercise>) exercises);
        args.putLong(EXERCISE_GROUP_ID_KEY, exerciseGroupId);
        args.putInt(EDIT_CODE, edit);
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
            exercises = getArguments().getParcelableArrayList(PACKAGE_EXERCISE_ARRAY);
            exerciseGroupId = getArguments().getLong(EXERCISE_GROUP_ID_KEY);
            edit = getArguments().getInt(EDIT_CODE);
            if (edit == EDIT_DESCRIPTION || edit == EDIT_IMAGE_LAYOUT) {
                exercise = getArguments().getParcelable(PACKAGE_EXERCISE);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.add_exercise_dialog, container, false);

        initViews();
        if (edit == EDIT_IMAGE_LAYOUT && exercise != null) {
            initEditImageLayout();
        } else if (edit == EDIT_DESCRIPTION && exercise != null) {
            initEditDescription();
        }
        initListeners();

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return rootView;
    }

    private void initEditDescription() {
        imageLayout.setVisibility(View.GONE);
        editDescription.setVisibility(View.VISIBLE);
        editDescription.setText(exercise.getDescription());
    }

    private void initEditImageLayout() {
        editText.setText(exercise.getName());
        exerciseType = exercise.getType();
        if (exerciseType.equals(getResources().getString(R.string.reps).toLowerCase())) {
            radioReps.setChecked(true);
        } else {
            radioTime.setChecked(true);
        }
        int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.list_img_dimen);
        bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercise.getImage(), false, resourceDimen);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            TextDrawable drawable = BitmapHelper.getTextDrawable(exercise.getName());
            imageView.setImageDrawable(drawable);
        }
    }

    private void initListeners() {
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
                if (edit == EDIT_DESCRIPTION) {
                    String description = editDescription.getText().toString();
                    updateExercise(description);
                    dismiss();
                    return;
                }
                String name = editText.getText().toString().trim();
                if (name.equals("")) {
                    Toast.makeText(getActivity(), "Please enter exercise name...", Toast.LENGTH_SHORT).show();
                } else if (doesExerciseExist(name)) {
                    Toast.makeText(getActivity(), "Exercise " + name + " already exist", Toast.LENGTH_SHORT).show();
                } else if (edit != NO_EDIT) {
                    updateExercise(name);
                    dismiss();
                } else {
                    saveExercise(name);
                    dismiss();
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
                        exerciseType = getResources().getString(R.string.reps).toLowerCase();
                        break;
                    case R.id.radioTime:
                        exerciseType = getResources().getString(R.string.time).toLowerCase();
                        break;
                }
            }
        });
    }

    private void initViews() {
        imageLayout = (LinearLayout) rootView.findViewById(R.id.imageLayout);
        editDescription = (EditText) rootView.findViewById(R.id.editDescription);
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        radioReps = (RadioButton) rootView.findViewById(R.id.radioReps);
        radioTime = (RadioButton) rootView.findViewById(R.id.radioTime);
        editText = (EditText) rootView.findViewById(R.id.editExerciseText);
        imageView = (ImageView) rootView.findViewById(R.id.imageLogo);
        btnSave = (Button) rootView.findViewById(R.id.btnSave);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
        imageButton = (ImageButton) rootView.findViewById(R.id.btnImage);
        editText.setFilters(new InputFilter[]{new EditTextInputFilter(EditTextInputFilter.BLOCK_CHARS)});
    }

    private void updateExercise(String name) {
        if (edit == EDIT_DESCRIPTION) {
            exercise.setDescription(name);
            ExercisesDataSource.updateExercise(getContext(), exercise);
            updateExercisesView();
            return;
        }
        if (exercise.getImage() != null) {
            File oldImage = BitmapHelper.getFileFromImages(exercise.getImage(), getContext());
            boolean deleted = oldImage.delete();
        }
        saveImage(name);
        exercise.setName(name);
        exercise.setType(exerciseType);
        ExercisesDataSource.updateExercise(getContext(), exercise);
        updateExercisesView();
    }

    private void saveImage(String name) {
        if (bitmap != null) {
            exercise.setImage("@" + exercise.getExerciseGroupId() + "@" + name + ".png");
            BitmapHelper.saveImage("@" + exercise.getExerciseGroupId() + "@" + name + ".png", bitmap, getContext());
        }
    }

    private void saveExercise(String name) {
        exercise = new Exercise();
        exercise.setName(name);
        exercise.setExerciseGroupId(exerciseGroupId);
        exercise.setType(exerciseType);
        saveImage(name);
        exercise = ExercisesDataSource.createExercise(getContext(), exercise);
        updateExercisesView();
    }

    private void updateExercisesView() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof ExercisesFragment) {
            ((ExercisesFragment) fragment).updateListView(exercise, false);
        } else {
            ((ExerciseDetailFragment) fragment).setExercise(exercise);
        }
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
                if (edit == EDIT_IMAGE_LAYOUT) {
                    return !name.equalsIgnoreCase(exercise.getName());
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayHelper display = new DisplayHelper(getActivity());
        int width = (int) display.getWidth();
        float density = getActivity().getResources().getDisplayMetrics().density;
        int dp = (int) (width / density);
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setGravity(Gravity.CENTER);
            if (dp < 600) {
                window.setLayout((9 * width) / 10, ViewGroup.LayoutParams.WRAP_CONTENT);
            } else {
                window.setLayout((5 * width) / 10, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == PICKFILE_RESULT_CODE) {
                Uri uri = data.getData();
                FilePathGetter pathGetter = new FilePathGetter(getContext());
                path = pathGetter.getPath(uri);
                getAndSetBitmap();
            }
        }
    }

    private void getAndSetBitmap() {
        new AsyncTask<String, Void, Void>() {
            Bitmap mBitmap;

            @Override
            protected Void doInBackground(String... params) {
                mBitmap = BitmapHelper.decodeBitmapFromPath(params[0], BitmapHelper.getRequiredWidth(getActivity()), BitmapHelper.getRequiredHeight(getActivity()));
                //bugas ant samsung cyanogenmode
                if (mBitmap == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AlertDialogHelper.showErrorDialogWhenNotLoadingImg(getContext());
                }
                mBitmap = BitmapHelper.getScaledBitmap(mBitmap, BitmapHelper.getMaxSize(getActivity()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                imageView.setImageBitmap(mBitmap);
                bitmap = mBitmap;
            }
        }.execute(path);
//        bitmap = BitmapHelper.decodeBitmapFromPath(path, BitmapHelper.getRequiredWidth(getActivity()), BitmapHelper.getRequiredHeight(getActivity()));
//        //bugas ant samsung cyanogenmode
//        if (bitmap == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            AlertDialogHelper.showErrorDialogWhenNotLoadingImg(getContext());
//        }
//        bitmap = BitmapHelper.getScaledBitmap(bitmap, BitmapHelper.getMaxSize(getActivity()));
//        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BITMAP, path);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            path = savedInstanceState.getString(BITMAP);
            if (path != null) {
                getAndSetBitmap();
            }
        }
    }
}
