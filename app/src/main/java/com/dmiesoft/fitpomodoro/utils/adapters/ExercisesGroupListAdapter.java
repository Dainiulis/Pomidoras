package com.dmiesoft.fitpomodoro.utils.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.BitmapHelper;
import java.util.List;

public class ExercisesGroupListAdapter extends ArrayAdapter<ExercisesGroup> {

    private static final String TAG = "EGLA";
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
        if (exerciseGroup.getImage() != null) {
            Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exerciseGroup.getImage(), true);
            imageView.setImageBitmap(bitmap);
        } else {
            String firstChar = exerciseGroup.getName().substring(0, 1).toUpperCase();
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(exerciseGroup.getName());
            TextDrawable drawable = TextDrawable.builder().buildRound(firstChar, color);
            imageView.setImageDrawable(drawable);
        }


        return convertView;
    }
}
