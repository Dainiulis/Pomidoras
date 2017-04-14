package com.dmiesoft.fitpomodoro.ui.fragments.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.utils.helpers.AlertDialogHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.DisplayHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.EditTextInputFilter;
import com.dmiesoft.fitpomodoro.utils.helpers.FilePathGetter;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddExerciseGroupDialog extends DialogFragment {

    private static final int PICKFILE_RESULT_CODE = 1;
    private static final String PACKAGE_EXERCISES_GROUP_ARRAY = "com.dmiesoft.fitpomodoro.model.ExercisesGroup.Array";
    private static final String PACKAGE_EXERCISES_GROUP = "com.dmiesoft.fitpomodoro.model.ExercisesGroup";
    private static final String IS_EDIT = "IS_EDIT";
    private static final String TAG = "AEGD";
    private Button btnSave, btnCancel;
    private EditText editText;
    private AddExerciseGroupDialogListener mListener;
    private ImageView imageView;
    private ImageButton imageButton;
    private List<ExercisesGroup> exercisesGroups;
    private ExercisesGroup exercisesGroup;
    private Bitmap bitmap;
    private View rootView;
    private boolean edit;
    private String path;

    public AddExerciseGroupDialog() {
    }

    public static AddExerciseGroupDialog newInstance(List<ExercisesGroup> exercisesGroupsNames, ExercisesGroup exercisesGroup, boolean edit) {

        Bundle args = new Bundle();
        AddExerciseGroupDialog fragment = new AddExerciseGroupDialog();

        args.putParcelableArrayList(PACKAGE_EXERCISES_GROUP_ARRAY, (ArrayList<ExercisesGroup>) exercisesGroupsNames);
        args.putParcelable(PACKAGE_EXERCISES_GROUP, exercisesGroup);
        args.putBoolean(IS_EDIT, edit);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddExerciseGroupDialogListener) {
            mListener = (AddExerciseGroupDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AddExerciseGroupDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            exercisesGroups = getArguments().getParcelableArrayList(PACKAGE_EXERCISES_GROUP_ARRAY);
            edit = getArguments().getBoolean(IS_EDIT);
            if (edit) {
                exercisesGroup = getArguments().getParcelable(PACKAGE_EXERCISES_GROUP);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.add_exercise_group_dialog, container, false);

        editText = (EditText) rootView.findViewById(R.id.editCategoryText);
        imageView = (ImageView) rootView.findViewById(R.id.imageLogo);

        /*
         * If editing exercise group
         */
        if (edit && exercisesGroup != null) {
            editText.setText(exercisesGroup.getName());
            int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.list_img_dimen);
            bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercisesGroup.getImage(), false, resourceDimen);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                TextDrawable drawable = BitmapHelper.getTextDrawable(exercisesGroup.getName());
                imageView.setImageDrawable(drawable);
            }
        }
        /***********************************/

        editText.setFilters(new InputFilter[]{new EditTextInputFilter(EditTextInputFilter.BLOCK_CHARS)});
        btnSave = (Button) rootView.findViewById(R.id.btnSave);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);

        imageButton = (ImageButton) rootView.findViewById(R.id.btnImage);
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
                    Toast.makeText(getActivity(), "Please enter category name...", Toast.LENGTH_SHORT).show();
                } else if (doesExercisesGroupExist(name)) {
                    Toast.makeText(getActivity(), "Category " + name + " already exist", Toast.LENGTH_SHORT).show();
                } else if (edit) {
                    updateExercisesGroup(name);
                    dismiss();
                } else {
                    saveExercisesGroup(name);
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

        return rootView;
    }

    private void startIntentGetContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    /**
     * Checks if Category exists
     *
     * @param enteredText
     * @return
     */
    private boolean doesExercisesGroupExist(String enteredText) {
        if (exercisesGroups.size() <= 0) {
            return false;
        }
        for (ExercisesGroup currentExercisesGroup : exercisesGroups) {
            if (currentExercisesGroup.getName().equalsIgnoreCase(enteredText)) {
                if (edit) {
                    return !enteredText.equalsIgnoreCase(exercisesGroup.getName());
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateExercisesGroup(String name) {
        if (exercisesGroup.getImage() != null) {
            File oldImage = BitmapHelper.getFileFromImages(exercisesGroup.getImage(), getContext());
            boolean deleted = oldImage.delete();
        }
        saveImage(name);
        exercisesGroup.setName(name);
        mListener.onUpdateExercisesGroupClicked(exercisesGroup);
    }

    private void saveImage(String name) {
        if (bitmap != null) {
            exercisesGroup.setImage("@" + name + ".png");
            BitmapHelper.saveImage("@" + name + ".png", bitmap, getContext());
        }
    }

    private void saveExercisesGroup(String name) {
        exercisesGroup = new ExercisesGroup();
        exercisesGroup.setName(name);
        saveImage(name);
        mListener.onSaveExercisesGroupClicked(exercisesGroup);
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayHelper display = new DisplayHelper(getActivity());
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

    public interface AddExerciseGroupDialogListener {
        void onSaveExercisesGroupClicked(ExercisesGroup exercisesGroup);

        void onUpdateExercisesGroupClicked(ExercisesGroup exercisesGroup);
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
        bitmap = BitmapHelper.decodeBitmapFromPath(path, BitmapHelper.REQUIRED_WIDTH, BitmapHelper.REQUIRED_HEIGTH);
        //bugas ant samsung cyanogenmode
        if (bitmap == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialogHelper.showErrorDialogWhenNotLoadingImg(getContext());
        }
        try {
            bitmap = BitmapHelper.getScaledBitmap(bitmap, BitmapHelper.MAX_SIZE);
        } catch (NullPointerException e) {
            Log.i(TAG, "getAndSetBitmap: " + e.getMessage());
        }
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("BITMAP_PATH", path);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            path = savedInstanceState.getString("BITMAP_PATH");
            getAndSetBitmap();
        }
    }

}
