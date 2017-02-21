package com.dmiesoft.fitpomodoro.utils.adapters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.dmiesoft.fitpomodoro.events.DeleteObjects;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.utils.BitmapHelper;

import org.greenrobot.eventbus.EventBus;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_exercises, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            Log.i(TAG, "getting tag:");
            holder = (ViewHolder) convertView.getTag();
        }

        Exercise exercise = exercises.get(position);
        holder.textView.setText(exercise.getName());
        updateChechedState(holder, exercise);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Exercise data = exercises.get(position);
                data.setChecked(!data.isChecked());
                updateChechedState(holder, data);
                EventBus.getDefault().post(new DeleteObjects(position, data.getClass().toString()));
            }
        });

        return convertView;
    }

    private boolean setBitmap (ViewHolder holder, Exercise exercise) {
        if (exercise.getImage() != null) {
            int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.list_img_dimen);
            Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercise.getImage(), true, resourceDimen);
            if (bitmap != null) {
                bitmap = BitmapHelper.getCroppedBitmap(bitmap, BitmapHelper.BORDER_SIZE);
                holder.imageView.setImageBitmap(bitmap);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void updateChechedState(ViewHolder holder, Exercise exercise) {
        if (exercise.isChecked()) {

            TextDrawable drawable = BitmapHelper.getTextDrawable(null);
            holder.imageView.setImageDrawable(drawable);
            holder.view.setBackgroundColor(ExercisesGroupListAdapter.HIGHLIGHT_COLOR);
            holder.checkIcon.setVisibility(View.VISIBLE);
        } else {
            if (!setBitmap(holder, exercise)) {
                TextDrawable drawable = BitmapHelper.getTextDrawable(exercise.getName());
                holder.imageView.setImageDrawable(drawable);
            }
            holder.view.setBackgroundColor(Color.TRANSPARENT);
            holder.checkIcon.setVisibility(View.GONE);
        }
    }

    private static class ViewHolder {
        private View view;
        private ImageView imageView;
        private TextView textView;
        private ImageView checkIcon;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageExercise);
            textView = (TextView) view.findViewById(R.id.nameExercise);
            checkIcon = (ImageView) view.findViewById(R.id.imageChecked);
        }
    }
}
