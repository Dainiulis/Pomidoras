package com.dmiesoft.fitpomodoro.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public abstract class MultiSelectionHelper {

    private static final String TAG = "MSH";

    public static void removeItemsFromDatabase(TreeMap<Integer, ?> map, Context context, ExercisesDataSource dataSource, String whatToDelete) {
        if (whatToDelete == null) {
            return;
        }
        File[] allImages = new File(context.getFilesDir(), "images").listFiles();
        for (Object o : map.values()) {
            if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                ExercisesGroup eG = ((ExercisesGroup)o);
                File file = BitmapHelper.getFileFromImages(eG.getImage(), context);
                for (int i = 0; i < allImages.length; i++) {
                    String imgWithExerciseId = allImages[i].getName();
                    if (imgWithExerciseId.contains("@" + eG.getId() +"@")){
                        allImages[i].delete();
                    }
                }
                if (file != null) {
                    file.delete();
                }
                dataSource.deleteExercisesGroup(eG.getId());

            } else {
                Exercise e = ((Exercise)o);
                File file = BitmapHelper.getFileFromImages(e.getImage(), context);
                if (file != null) {
                    file.delete();
                }
                dataSource.deleteExercise(e.getId());
            }
        }
    }

    public static TreeMap<Integer, ?> removeItems(List<ExercisesGroup> exercisesGroups, List<Exercise> exercises, List<Integer> deleteIdList, String whatToDelete) {
        TreeMap<Integer, ExercisesGroup> eGMap = null;
        TreeMap<Integer, Exercise> eMap = null;
        if (whatToDelete.equals(ExercisesGroup.class.toString())) {
            eGMap = new TreeMap<>();
        } else {
            eMap = new TreeMap<>();
        }
        int currExId;
        for (int i = 0; deleteIdList.size() > i; i++) {
            currExId = deleteIdList.get(i);
            if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                ExercisesGroup eG = exercisesGroups.get(currExId);
                eG.setChecked(false);
                if (eGMap != null) {
                    eGMap.put(currExId, eG);
                }
            } else {
                Exercise e = exercises.get(currExId);
                e.setChecked(false);
                if (eMap != null) {
                    eMap.put(currExId, e);
                }
            }
        }
        for (int i = 0; deleteIdList.size() > i; i++) {
            currExId = deleteIdList.get(i);
                        /*
                         * Logic:
                         * if currExId (id that is going to be removed) is less than
                         * the next id, then decrement the next id which is going to be removed
                         * because after removing exercisesGroup all id's that are higher
                         * is going to decrease by 1
                         * else do nothing
                         *
                         * after loop remove exercisesGroup
                         */
            for (int j = i + 1; deleteIdList.size() > j; j++) {
                int exGrIdToDecrement = deleteIdList.get(j);
                if (currExId < exGrIdToDecrement) {
                    deleteIdList.set(j, exGrIdToDecrement - 1);
                }
            }
            if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                exercisesGroups.remove(currExId);
            } else {
                exercises.remove(currExId);
            }
        }
        if (whatToDelete.equals(ExercisesGroup.class.toString())) {
            return eGMap;
        } else {
            return eMap;
        }
    }

}
