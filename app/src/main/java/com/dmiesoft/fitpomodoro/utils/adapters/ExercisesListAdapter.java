package com.dmiesoft.fitpomodoro.utils.adapters;

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
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.events.DeleteObjects;
import com.dmiesoft.fitpomodoro.events.UnfavoriteObjects;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ExercisesListAdapter extends ArrayAdapter<Exercise> {

    private static final String TAG = "ELA";
    private List<Exercise> exercises;
    private List<ViewHolder> viewsToAnimate;
    private Context context;


    public ExercisesListAdapter(Context context, int resource, List<Exercise> exercises) {
        super(context, resource, exercises);
        this.exercises = exercises;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        viewsToAnimate = new ArrayList<>();
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_exercises, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Exercise exercise = exercises.get(position);
        holder.textView.setText(exercise.getName());
        updateChechedState(holder, exercise);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Exercise data = exercises.get(position);
                data.setChecked(!data.isChecked());
                updateChechedState(holder, data);
                if (context instanceof MainActivity) {
                    EventBus.getDefault().post(new DeleteObjects(position, data.getClass().toString()));
                } else {
//                    EventBus.getDefault().post(new UnfavoriteObjects(exercise.getId()));
                    EventBus.getDefault().post(new UnfavoriteObjects(position));
                }
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
            viewsToAnimate.add(holder);
            TextDrawable drawable = BitmapHelper.getTextDrawable(null);
            holder.imageView.setImageDrawable(drawable);
            holder.view.setBackgroundColor(ExercisesGroupListAdapter.HIGHLIGHT_COLOR);
            holder.checkIcon.setVisibility(View.VISIBLE);
        } else {
            if (viewsToAnimate.contains(holder)) {
                viewsToAnimate.remove(holder);
            }
            if (!setBitmap(holder, exercise)) {
                TextDrawable drawable = BitmapHelper.getTextDrawable(exercise.getName());
                holder.imageView.setImageDrawable(drawable);
            }
            holder.view.setBackgroundColor(Color.TRANSPARENT);
            holder.checkIcon.setVisibility(View.GONE);
        }
    }

    public void clearViewsToAnimate() {
        if (viewsToAnimate != null)
            viewsToAnimate.clear();
    }

    @Override
    public void notifyDataSetChanged() {
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
