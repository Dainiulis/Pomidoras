package com.dmiesoft.fitpomodoro.utils.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import com.dmiesoft.fitpomodoro.utils.UtilityFunctions;
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
            Bitmap bitmap = UtilityFunctions.getBitmapFromFiles(getContext(), exerciseGroup.getImage());
//            bitmap = getCroppedBitmap(bitmap);
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

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        final int color = 0xff424242;
        int radius = bitmap.getWidth() / 2;

        Bitmap output = Bitmap.createBitmap(x,
                y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        int x2 = output.getWidth();
        int y2 = output.getHeight();

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, x, y);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawCircle(x / 2, y / 2, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}
