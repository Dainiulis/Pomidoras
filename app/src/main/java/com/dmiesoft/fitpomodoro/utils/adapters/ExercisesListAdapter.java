package com.dmiesoft.fitpomodoro.utils.adapters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.utils.BitmapHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExercisesListAdapter extends ArrayAdapter<Exercise> {

    private static final String TAG = "ELA";
    private List<Exercise> exercises;

    public ExercisesListAdapter(Context context, int resource, List<Exercise> exercises) {
        super(context, resource, exercises);
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_exercises, parent, false);
        }

        Exercise exercise = exercises.get(position);

        TextView nameText = (TextView) convertView.findViewById(R.id.nameExercise);
        nameText.setText(exercise.getName());
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageExercise);

        if (exercise.getImage() != null) {
            int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.list_img_dimen);
            Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercise.getImage(), true, resourceDimen);
            if (bitmap != null) {
                bitmap = BitmapHelper.getCroppedBitmap(bitmap, BitmapHelper.BORDER_SIZE);
                imageView.setImageBitmap(bitmap);
            } else {
                TextDrawable drawable = BitmapHelper.getTextDrawable(exercise.getName());
                imageView.setImageDrawable(drawable);
            }
        } else {
            TextDrawable drawable = BitmapHelper.getTextDrawable(exercise.getName());
            imageView.setImageDrawable(drawable);
        }


        return convertView;
    }
}
