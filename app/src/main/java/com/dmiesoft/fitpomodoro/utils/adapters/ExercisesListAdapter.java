package com.dmiesoft.fitpomodoro.utils.adapters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExercisesListAdapter extends ArrayAdapter<Exercise> {

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

        Drawable drawable = getDrawableFromAssets(exercise.getImage());
        imageView.setImageDrawable(drawable);


        return convertView;
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

}
