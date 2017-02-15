package com.dmiesoft.fitpomodoro.utils;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.DocumentsContract;
import android.util.Log;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;

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

    public InitialDatabasePopulation(Context context, ExercisesDataSource dataSource){
        this.context = context;
        this.dataSource = dataSource;
    }

    public List<ExercisesGroup> readJson() throws IOException, JSONException {

        copyAssets();

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

            JSONArray exercisesData = allData.getJSONObject(i).getJSONArray("exercises");
            for (int j = 0; j < exercisesData.length(); j++) {
                Exercise exercise = new Exercise();
                exercise.setName(exercisesData.getJSONObject(j).getString("name"));
                exercise.setType(exercisesData.getJSONObject(j).getString("type"));
                exercise.setImage(exercisesData.getJSONObject(j).getString("image"));
                exercise.setDescription(exercisesData.getJSONObject(j).getString("description"));
                exercise.setExercise_group_id(exercisesGroup.getId());
                dataSource.createExercise(exercise);
            }
            exercisesGroups.add(exercisesGroup);
        }
        return exercisesGroups;
    }

    private void copyAssets() {
        File f = new File(context.getFilesDir(), "images");
        if (!f.exists()) {
            f.mkdir();
        }

        AssetManager assetManager = context.getAssets();
        InputStream stream = null;
        FileOutputStream fous = null;

        String[] list;

        try {
            list = context.getAssets().list("");
            if (list.length > 0) {
                for (String file : list) {
                    if (file.contains(".png")) {
                        fous = new FileOutputStream(f + "/" + file);
                        stream = assetManager.open(file);

                        Log.i(TAG, "copyAssets: " + file);
                        copyFile(stream, fous);
                    }
                }
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

    private void copyFile (InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}
