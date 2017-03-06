package com.dmiesoft.fitpomodoro.utils.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.events.DeleteObjects;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ExercisesGroupListAdapter extends ArrayAdapter<ExercisesGroup> {

    private static final String TAG = "EGLA";
    private List<ExercisesGroup> exercisesGroups;
    private List<ViewHolder> viewsToAnimate;
    public static final int HIGHLIGHT_COLOR = Color.parseColor("#ACBABF");

    public ExercisesGroupListAdapter(Context context, int resource, List<ExercisesGroup> exercisesGroups) {
        super(context, resource, exercisesGroups);
        this.exercisesGroups = exercisesGroups;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        viewsToAnimate = new ArrayList<>();
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_exercises_groups, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            ExercisesGroup exercisesGroup = exercisesGroups.get(position);
            holder.textView.setText(exercisesGroup.getName());
            updateCheckedState(holder, exercisesGroup);
        } catch (IndexOutOfBoundsException ignore) {
        }


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExercisesGroup data = exercisesGroups.get(position);
                data.setChecked(!data.isChecked());
                updateCheckedState(holder, data);
                EventBus.getDefault().post(new DeleteObjects(position, data.getClass().toString()));
            }
        });

        return convertView;
    }

    public void clearViewsToAnimate() {
        if (viewsToAnimate != null)
            viewsToAnimate.clear();
    }

    @Override
    public void notifyDataSetChanged() {
        Log.i(TAG, "notifyDataSetChanged: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (viewsToAnimate != null) {
                if (viewsToAnimate.size() > 0) {
                    for (final ViewHolder holder : viewsToAnimate) {
                        holder.view.animate().setDuration(200).alpha(0).translationX(holder.view.getWidth())
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyDataSetChanged();
                                        holder.view.setTranslationX(0);
                                        holder.view.setAlpha(1);
                                        holder.view.clearAnimation();
                                    }
                                });
                    }
                    Log.i(TAG, "clearing list: " + viewsToAnimate.size());
                    viewsToAnimate.clear();
                } else {
                    super.notifyDataSetChanged();
                }
            } else {
                super.notifyDataSetChanged();
            }
        } else {
            super.notifyDataSetChanged();
        }
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
            viewsToAnimate.add(holder);
            TextDrawable drawable = BitmapHelper.getTextDrawable(null);
            holder.imageView.setImageDrawable(drawable);
            holder.view.setBackgroundColor(HIGHLIGHT_COLOR);
            holder.checkIcon.setVisibility(View.VISIBLE);
        } else {
            if (viewsToAnimate.contains(holder)) {
                viewsToAnimate.remove(holder);
            }
            if (!setBitmap(holder, exercisesGroup)) {
                TextDrawable drawable = BitmapHelper.getTextDrawable(exercisesGroup.getName());
                holder.imageView.setImageDrawable(drawable);
            }
            holder.view.setBackgroundColor(Color.TRANSPARENT);
            holder.checkIcon.setVisibility(View.GONE);
        }
    }

}
