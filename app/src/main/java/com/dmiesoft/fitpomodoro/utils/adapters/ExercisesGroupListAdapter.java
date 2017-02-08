package com.dmiesoft.fitpomodoro.utils.adapters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExercisesGroupListAdapter extends ArrayAdapter<ExercisesGroup> {

    private List<ExercisesGroup> exercisesGroups;

    public ExercisesGroupListAdapter(Context context, int resource, List<ExercisesGroup> exercisesGroups) {
        super(context, resource, exercisesGroups);
        this.exercisesGroups = exercisesGroups;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_exercises_groups, parent, false);
        }

        ExercisesGroup exerciseGroup = exercisesGroups.get(position);

        TextView nameText = (TextView) convertView.findViewById(R.id.nameExerciseGroup);
        nameText.setText(exerciseGroup.getName());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageExerciseGroup);
        Drawable drawable = getDrawableFromAssets(exerciseGroup.getImage());
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
