package com.dmiesoft.fitpomodoro.ui.fragments.dialogs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
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

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.DisplayWidthHeight;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;

public class AddExerciseGroupDialog extends DialogFragment {

    private static final int PERMISSIONS_REQUEST_R_STORAGE = 22;
    private Button btnSave, btnCancel;
    private EditText editText;
    private AddExerciseGroupDialogListener mListener;
    private ImageView imageView;
    private ImageButton imageButton;
    private String path, imageName;

    public AddExerciseGroupDialog() {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_exercise_group_dialog, container, false);
        editText = (EditText) rootView.findViewById(R.id.editCategoryText);
        btnSave = (Button) rootView.findViewById(R.id.btnSave);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);

        imageView = (ImageView) rootView.findViewById(R.id.imageLogo);
        imageButton = (ImageButton) rootView.findViewById(R.id.btnImage);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_R_STORAGE);
                } else {
                    int PICKFILE_RESULT_CODE = 1;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("files/*");
                    startActivityForResult(intent, PICKFILE_RESULT_CODE);
                }

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageName = copyFile(path);
                ExercisesGroup exercisesGroup = new ExercisesGroup();
                exercisesGroup.setName(editText.getText().toString());
                if (imageName != null || imageName.equals("")) {
                    exercisesGroup.setImage(imageName);
                }
                mListener.onSaveExerciseGroupClicked(exercisesGroup);
                dismiss();
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

    public interface AddExerciseGroupDialogListener {
        void onSaveExerciseGroupClicked(ExercisesGroup exerciseGroupName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                path = getRealPathFromURI(getActivity(), uri);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                imageView.setImageBitmap(bitmap);
            }
        }

    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String copyFile(String filePath) {
        String fileName = new File(filePath).getName();
        try {
            File f = new File(getContext().getFilesDir(), "images");
            if (!f.exists()) {
                f.mkdir();
            }

            File newFile = new File(f, fileName);

            InputStream in = null;
            OutputStream out = null;

            if (f.canRead()) {
                Log.i("AEGD", "copyFile: " + newFile.toString());
                File sourceFile = new File(filePath);
                if (sourceFile.exists()) {
                    Log.i("AEGD", "copyFile: " + sourceFile.toString());
                    in = new FileInputStream(sourceFile);
                    out = new FileOutputStream(newFile);
                    copyFile(in, out);
                    in.close();
                    out.flush();
                    out.close();

                }
            }
        } catch (Exception e) {
            Log.i("AEGD", "copyFile: " + e.getMessage());
        }
        return fileName;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}
