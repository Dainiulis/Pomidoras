package com.dmiesoft.fitpomodoro.utils;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class InitialDatabasePopulation {

    private static final String TAG = "IDP";
    private Context context;
    private ExercisesDataSource dataSource;
    private String[] list;

    public InitialDatabasePopulation(Context context, ExercisesDataSource dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    public List<ExercisesGroup> readJson() throws IOException, JSONException {

        list = context.getAssets().list("");

        InputStream stream = context.getResources().openRawResource(R.raw.json_data);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
        StringBuffer stringBuffer = new StringBuffer();
        while (bufferedInputStream.available() != 0) {
            char c = (char) bufferedInputStream.read();
            stringBuffer.append(c);
        }
        bufferedInputStream.close();
        stream.close();

        JSONArray allData = new JSONArray(stringBuffer.toString());
        List<ExercisesGroup> exercisesGroups = new ArrayList<>();
        for (int i = 0; i < allData.length(); i++) {

            ExercisesGroup exercisesGroup = new ExercisesGroup();
            String exerciseGroupName = allData.getJSONObject(i).getString("exercise_group");
            String exerciseGroupImage = allData.getJSONObject(i).getString("image");
            exercisesGroup.setName(exerciseGroupName);
            exercisesGroup.setImage(exerciseGroupImage);
            exercisesGroup = dataSource.createExercisesGroup(exercisesGroup);
            copyAssets(false, "", exerciseGroupImage);

            JSONArray exercisesData = allData.getJSONObject(i).getJSONArray("exercises");
            for (int j = 0; j < exercisesData.length(); j++) {
                Exercise exercise = new Exercise();
                exercise.setName(exercisesData.getJSONObject(j).getString("name"));
                exercise.setType(exercisesData.getJSONObject(j).getString("type"));
//                exercise.setImage(exercisesData.getJSONObject(j).getString("image") + ".png");
                exercise.setImage("@" + exercisesGroup.getId() + "@" + exercise.getName() + ".png");
                exercise.setDescription(exercisesData.getJSONObject(j).getString("description"));
                exercise.setExerciseGroupId(exercisesGroup.getId());
                exercise = dataSource.createExercise(exercise);
                copyAssets(true, exercise.getImage(), exerciseGroupImage);
            }
            exercisesGroups.add(exercisesGroup);
        }
        return exercisesGroups;
    }

    private void copyAssets(boolean cpExercise, String exerciseImgName, String imageName) {
        File f = new File(context.getExternalFilesDir(null), "images");
        if (!f.exists()) {
            f.mkdir();
        }

        AssetManager assetManager = context.getAssets();
        InputStream stream = null;
        FileOutputStream fous = null;

        try {
            if (list.length > 0) {
//                for (String file : list) {
//                if (file.contains(".png")) {
                String finalFileName = "";
                if (cpExercise) {
                    finalFileName = exerciseImgName;
                } else {
                    finalFileName = imageName;
                }
                fous = new FileOutputStream(f + "/" + finalFileName);
                stream = assetManager.open(imageName);
                copyFile(stream, fous);


                String path = BitmapHelper.getFileFromImages(finalFileName, context).getAbsolutePath();
                Log.i(TAG, "copyAssets: " + path);
                Bitmap bitmap = BitmapHelper.decodeBitmapFromPath(path, BitmapHelper.REQUIRED_WIDTH, BitmapHelper.REQUIRED_HEIGTH);
//                bitmap = BitmapHelper.getScaledBitmap(bitmap, BitmapHelper.MAX_SIZE);
                BitmapHelper.saveImage(finalFileName, bitmap, context);
//                }
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
                fous.flush();
                fous.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


}
