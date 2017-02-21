package com.dmiesoft.fitpomodoro.utils.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.BitmapHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ExercisesGroupListAdapter extends ArrayAdapter<ExercisesGroup> {

    private static final String TAG = "EGLA";
    private List<ExercisesGroup> exercisesGroups;
    public static final int HIGHLIGHT_COLOR = Color.parseColor("#ACBABF");

    public ExercisesGroupListAdapter(Context context, int resource, List<ExercisesGroup> exercisesGroups) {
        super(context, resource, exercisesGroups);
        this.exercisesGroups = exercisesGroups;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_exercises_groups, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final ExercisesGroup exercisesGroup = exercisesGroups.get(position);
        holder.textView.setText(exercisesGroup.getName());

        updateCheckedState(holder, exercisesGroup);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExercisesGroup data = exercisesGroups.get(position);
                data.setChecked(!data.isChecked());
                updateCheckedState(holder, data);
                EventBus.getDefault().post(new DeleteObjects(position, data.getClass().toString()));
            }
        });

        Log.i(TAG, "Exercises gorups: " + exercisesGroups);
        return convertView;
    }

    private boolean setBitmap(ViewHolder holder, ExercisesGroup exercisesGroup) {
        int resourceDimen = (int) getContext().getResources().getDimension(R.dimen.list_img_dimen);
        if (exercisesGroup.getImage() != null) {
            Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercisesGroup.getImage(), true, resourceDimen);
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

    private static class ViewHolder {
        private View view;
        private ImageView imageView;
        private TextView textView;
        private ImageView checkIcon;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageExerciseGroup);
            textView = (TextView) view.findViewById(R.id.nameExerciseGroup);
            checkIcon = (ImageView) view.findViewById(R.id.imageChecked);
        }
    }

    private void updateCheckedState(ViewHolder holder, ExercisesGroup exercisesGroup) {
        if (exercisesGroup.isChecked()) {
            TextDrawable drawable = BitmapHelper.getTextDrawable(null);
            holder.imageView.setImageDrawable(drawable);
            holder.view.setBackgroundColor(HIGHLIGHT_COLOR);
            holder.checkIcon.setVisibility(View.VISIBLE);
        } else {
            if (!setBitmap(holder, exercisesGroup)) {
                TextDrawable drawable = BitmapHelper.getTextDrawable(exercisesGroup.getName());
                holder.imageView.setImageDrawable(drawable);
            }
            holder.view.setBackgroundColor(Color.TRANSPARENT);
            holder.checkIcon.setVisibility(View.GONE);
        }
    }

}
