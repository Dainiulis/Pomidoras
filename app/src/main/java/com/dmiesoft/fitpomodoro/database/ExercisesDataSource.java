package com.dmiesoft.fitpomodoro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;

import java.util.ArrayList;
import java.util.List;

public class ExercisesDataSource {

    SQLiteOpenHelper dbHelper;
    SQLiteDatabase database;

    private static String[] exercises_columns = {
            DatabaseContract.ExercisesTable._ID,
            DatabaseContract.ExercisesTable.COLUMN_NAME,
            DatabaseContract.ExercisesTable.COLUMN_TYPE,
            DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION,
            DatabaseContract.ExercisesTable.COLUMN_GROUP_ID,
            DatabaseContract.ExercisesTable.COLUMN_IMAGE,
            DatabaseContract.ExercisesTable.COLUMN_DATE,
    };

    private static String[] exercises_groups_columns = {
            DatabaseContract.ExercisesGroupsTable._ID,
            DatabaseContract.ExercisesGroupsTable.COLUMN_NAME,
            DatabaseContract.ExercisesGroupsTable.COLUMN_DATE
    };

    public ExercisesDataSource (Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        database.close();
    }

    /*
    Manage database "exercises_groups" table
     */

    public ExercisesGroup createExercisesGroup(ExercisesGroup exercisesGroup) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesGroupsTable.COLUMN_NAME, exercisesGroup.getName());

        long newRowId = database.insert(DatabaseContract.ExercisesGroupsTable.TABLE_NAME, null, values);
        exercisesGroup.setId(newRowId);
        return exercisesGroup;
    }

    public List<ExercisesGroup> findAllExerciseGroups() {
        List<ExercisesGroup> exercisesGroups = new ArrayList<>();

        Cursor cursor = database.query(
                DatabaseContract.ExercisesGroupsTable.TABLE_NAME,
                exercises_groups_columns,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ExercisesGroup exercisesGroup = new ExercisesGroup();
                exercisesGroup.setId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesGroupsTable._ID)));
                exercisesGroup.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesGroupsTable.COLUMN_NAME)));
                exercisesGroup.setDate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesGroupsTable.COLUMN_DATE)));
                exercisesGroups.add(exercisesGroup);
            }
        }
        return exercisesGroups;
    }

    /*
    Manage database "exercises" table
     */
    public Exercise createExercise(Exercise exercise) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesTable.COLUMN_NAME, exercise.getName());
        values.put(DatabaseContract.ExercisesTable.COLUMN_TYPE, exercise.getType());
        values.put(DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION, exercise.getDescription());
        values.put(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID, exercise.getExercise_group_id());
        values.put(DatabaseContract.ExercisesTable.COLUMN_IMAGE, exercise.getImage());

        long newRowId = database.insert(DatabaseContract.ExercisesTable.TABLE_NAME, null, values);
        exercise.setId(newRowId);
        return exercise;
    }

    public List<Exercise> findAllExercises() {
        String orderBy = DatabaseContract.ExercisesTable.COLUMN_NAME + " ASC";
        List<Exercise> exercises = new ArrayList<>();

        Cursor cursor = database.query(
                DatabaseContract.ExercisesTable.TABLE_NAME,
                exercises_columns,
                null,
                null,
                null,
                null,
                orderBy
        );

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Exercise exercise = new Exercise();
                exercise.setId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable._ID)));
                exercise.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_NAME)));
                exercise.setType(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_TYPE)));
                exercise.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION)));
                exercise.setExercise_group_id(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID)));
                exercise.setImage(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_IMAGE)));
                exercise.setDate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_DATE)));
                exercises.add(exercise);
            }
        }
        cursor.close();
        return exercises;
    }

}
