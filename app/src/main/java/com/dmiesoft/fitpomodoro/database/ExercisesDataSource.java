package com.dmiesoft.fitpomodoro.database;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExerciseHistory;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.model.Favorite;
import com.dmiesoft.fitpomodoro.utils.DatabaseQueryHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExercisesDataSource {

    private static final String TAG = "EDS";

    private static String[] exercises_columns = {
            DatabaseContract.ExercisesTable._ID,
            DatabaseContract.ExercisesTable.COLUMN_NAME,
            DatabaseContract.ExercisesTable.COLUMN_TYPE,
            DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION,
            DatabaseContract.ExercisesTable.COLUMN_GROUP_ID,
            DatabaseContract.ExercisesTable.COLUMN_IMAGE,
            DatabaseContract.ExercisesTable.COLUMN_DATE,
            DatabaseContract.ExercisesTable.COLUMN_HOW_MANY_TIMES_DONE,
            DatabaseContract.ExercisesTable.COLUMN_TOTAL_REPS
    };

    private static String[] exercises_groups_columns = {
            DatabaseContract.ExercisesGroupsTable._ID,
            DatabaseContract.ExercisesGroupsTable.COLUMN_NAME,
            DatabaseContract.ExercisesGroupsTable.COLUMN_IMAGE,
            DatabaseContract.ExercisesGroupsTable.COLUMN_DATE
    };

    private static String[] favorites_columns = {
            DatabaseContract.FavoritesTable._ID,
            DatabaseContract.FavoritesTable.COLUMN_NAME,
            DatabaseContract.FavoritesTable.COLUMN_DATE
    };

    /*
    Manage database "exercises_groups" table
     */

    public static ExercisesGroup createExercisesGroup(Context context, ExercisesGroup exercisesGroup) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesGroupsTable.COLUMN_NAME, exercisesGroup.getName());
        values.put(DatabaseContract.ExercisesGroupsTable.COLUMN_IMAGE, exercisesGroup.getImage());

        Uri uri = context.getContentResolver().insert(DatabaseContract.ExercisesGroupsTable.CONTENT_URI, values);
        exercisesGroup.setId(ContentUris.parseId(uri));
        return exercisesGroup;
    }

    public static List<ExercisesGroup> findExerciseGroups(Context context, String selection, String[] selectionArgs) {
        List<ExercisesGroup> exercisesGroups = new ArrayList<>();
        String orderBy = DatabaseContract.ExercisesGroupsTable.COLUMN_NAME + " ASC";

        Cursor cursor = context.getContentResolver().query(DatabaseContract.ExercisesGroupsTable.CONTENT_URI,
                exercises_groups_columns, selection, selectionArgs, orderBy);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ExercisesGroup exercisesGroup = new ExercisesGroup();
                exercisesGroup.setId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesGroupsTable._ID)));
                exercisesGroup.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesGroupsTable.COLUMN_NAME)));
                exercisesGroup.setImage(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesGroupsTable.COLUMN_IMAGE)));
                exercisesGroup.setDate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesGroupsTable.COLUMN_DATE)));
                exercisesGroups.add(exercisesGroup);
            }
        }
        cursor.close();
        return exercisesGroups;
    }

    public static void updateExercisesGroup(Context context, ExercisesGroup exercisesGroup) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesGroupsTable.COLUMN_NAME, exercisesGroup.getName());
        values.put(DatabaseContract.ExercisesGroupsTable.COLUMN_IMAGE, exercisesGroup.getImage());
        Uri uri = Uri.withAppendedPath(DatabaseContract.ExercisesGroupsTable.CONTENT_URI, String.valueOf(exercisesGroup.getId()));
        context.getContentResolver().update(uri, values, null, null);
    }


    public static void deleteExercisesGroup(Context context, long id) {
        Uri uri = Uri.withAppendedPath(DatabaseContract.ExercisesGroupsTable.CONTENT_URI, String.valueOf(id));
        context.getContentResolver().delete(uri, null, null);
    }


    /*
    Manage database "exercises" table
     */

    public static Exercise createExercise(Context context, Exercise exercise) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesTable.COLUMN_NAME, exercise.getName());
        values.put(DatabaseContract.ExercisesTable.COLUMN_TYPE, exercise.getType());
        values.put(DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION, exercise.getDescription());
        values.put(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID, exercise.getExerciseGroupId());
        values.put(DatabaseContract.ExercisesTable.COLUMN_IMAGE, exercise.getImage());

        Uri uri = context.getContentResolver().insert(DatabaseContract.ExercisesTable.CONTENT_URI, values);
        exercise.setId(ContentUris.parseId(uri));
        return exercise;
    }

    public static void updateExercise(Context context, Exercise exercise) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesTable.COLUMN_NAME, exercise.getName());
        values.put(DatabaseContract.ExercisesTable.COLUMN_TYPE, exercise.getType());
        values.put(DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION, exercise.getDescription());
        values.put(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID, exercise.getExerciseGroupId());
        values.put(DatabaseContract.ExercisesTable.COLUMN_IMAGE, exercise.getImage());
        Uri uri = Uri.withAppendedPath(DatabaseContract.ExercisesTable.CONTENT_URI, String.valueOf(exercise.getId()));
        context.getContentResolver().update(uri, values, null, null);
    }

    public static List<Exercise> findExercises(Context context, String selection, String[] selectionArgs) {
        String orderBy = DatabaseContract.ExercisesTable.COLUMN_NAME + " ASC";
        List<Exercise> exercises = new ArrayList<>();

        Uri uri = DatabaseContract.ExercisesTable.CONTENT_URI;
        Cursor cursor = context.getContentResolver()
                .query(uri,
                        exercises_columns, selection, selectionArgs, orderBy);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Exercise exercise = new Exercise();
                exercise.setId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable._ID)));
                exercise.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_NAME)));
                exercise.setType(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_TYPE)));
                exercise.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION)));
                exercise.setExerciseGroupId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID)));
                exercise.setImage(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_IMAGE)));
                exercise.setDate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_DATE)));
                exercise.setHowManyTimesDone(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ExercisesTable.COLUMN_HOW_MANY_TIMES_DONE)));
                exercise.setTotalRepsDone(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ExercisesTable.COLUMN_TOTAL_REPS)));
                exercises.add(exercise);
            }
        }
        cursor.close();
        return exercises;
    }

    /**
     * @param context
     * @param selection
     * @param selectionArgs
     * @param favoriteId    pass -1 if not looking from favorites
     * @return
     */
    public static List<Long> getExercisesIds(Context context, String selection, String[] selectionArgs, long favoriteId) {
        String[] projection = {DatabaseContract.ExercisesTable._ID};
        List<Long> idList = new ArrayList<>();
        Cursor cursor;
        if (favoriteId == -1) {
            Uri uri = DatabaseContract.ExercisesTable.CONTENT_URI;
            cursor = context.getContentResolver()
                    .query(uri, projection, selection, selectionArgs, null);
        } else {
            Uri uri = DatabaseContract.ExercisesTable.CONTENT_URI;
            selection = DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID + "=?";
            selectionArgs = new String[]{String.valueOf(favoriteId)};
            cursor = context.getContentResolver()
                    .query(uri, projection, selection, selectionArgs, null);
        }

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable._ID));
                idList.add(id);
            }
        }
        cursor.close();
        return idList;
    }

    public static void deleteExercise(Context context, long id) {
        Uri uri = Uri.withAppendedPath(DatabaseContract.ExercisesTable.CONTENT_URI, String.valueOf(id));
        context.getContentResolver().delete(uri, null, null);
    }

    /* Favorites management */

    public static void createFavorite(Context context, String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.FavoritesTable.COLUMN_NAME, name);
        context.getContentResolver().insert(DatabaseContract.FavoritesTable.CONTENT_URI, values);
    }


    public static List<Favorite> getAllFavorites(Context context) {
        List<Favorite> favorites = new ArrayList<>();
        String orderBy = DatabaseContract.FavoritesTable.COLUMN_NAME + " ASC";

        Cursor cursor = context.getContentResolver()
                .query(DatabaseContract.FavoritesTable.CONTENT_URI, favorites_columns,
                        null, null, orderBy);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Favorite favorite = new Favorite();
                favorite.setId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.FavoritesTable._ID)));
                favorite.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.FavoritesTable.COLUMN_NAME)));
                favorite.setDate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.FavoritesTable.COLUMN_DATE)));
                favorites.add(favorite);
            }
        }
        cursor.close();
        return favorites;
    }

    public static void createFavExIds(Context context, HashMap<Long, Long> favExIds) {
        Log.i(TAG, "createFavExIds: " + favExIds);
        for (Map.Entry<Long, Long> entry : favExIds.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.FavExIdsTable.COLUMN_EXERCISE_ID, entry.getKey());
            values.put(DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID, entry.getValue());
            context.getContentResolver().insert(DatabaseContract.FavExIdsTable.CONTENT_URI, values);
        }
    }

    public static List<Exercise> findFavoriteExercises(Context context, long favoriteId) {
        List<Exercise> exercises = new ArrayList<>();

        String selection = DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(favoriteId)};
        String sortOrder = DatabaseContract.ExercisesTable.COLUMN_GROUP_ID + " ASC";

        Uri uri = DatabaseContract.ExercisesTable.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri,
                exercises_columns, selection, selectionArgs, sortOrder);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Exercise exercise = new Exercise();
                exercise.setId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable._ID)));
                exercise.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_NAME)));
                exercise.setType(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_TYPE)));
                exercise.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION)));
                exercise.setExerciseGroupId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID)));
                exercise.setImage(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_IMAGE)));
                exercise.setDate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_DATE)));
                exercise.setHowManyTimesDone(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ExercisesTable.COLUMN_HOW_MANY_TIMES_DONE)));
                exercise.setTotalRepsDone(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ExercisesTable.COLUMN_TOTAL_REPS)));
                exercises.add(exercise);
            }
        }
        cursor.close();
        return exercises;
    }

    public static void removeExercisesFromFavorites(Context context, List<Exercise> exercises, List<Integer> unfavoriteIdList, long favoriteId) {
        for (int i = 0; i < unfavoriteIdList.size(); i++) {
            String where = DatabaseContract.FavExIdsTable.COLUMN_EXERCISE_ID + "=? AND " +
                    DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID + "=?";
            String[] whereArgs = {String.valueOf(exercises.get(unfavoriteIdList.get(i)).getId()), String.valueOf(favoriteId)};
            context.getContentResolver().delete(DatabaseContract.FavExIdsTable.CONTENT_URI, where, whereArgs);
        }
    }

    public static void deleteFavorite(Context context, long favoriteId) {
        Uri uri = Uri.withAppendedPath(DatabaseContract.FavoritesTable.CONTENT_URI, String.valueOf(favoriteId));
        context.getContentResolver().delete(uri, null, null);
    }

    public static void saveExerciseHistory(Context context, int howMany, long exerciseId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExerciseHistoryTable.COLUMN_EXERCISE_ID, exerciseId);
        values.put(DatabaseContract.ExerciseHistoryTable.COLUMN_HOW_MANY_REPS_TIME, howMany);
        context.getContentResolver().insert(DatabaseContract.ExerciseHistoryTable.CONTENT_URI, values);

        String where = DatabaseContract.ExercisesTable._ID + " = ?";
        String[] whereArgs = {String.valueOf(exerciseId)};
        Exercise exercise = findExercises(context, where, whereArgs).get(0);
        int howManyTimesDone = exercise.getHowManyTimesDone() + 1;
        int totalReps = exercise.getTotalRepsDone() + howMany;
        values.clear();
        values.put(DatabaseContract.ExercisesTable.COLUMN_HOW_MANY_TIMES_DONE, howManyTimesDone);
        values.put(DatabaseContract.ExercisesTable.COLUMN_TOTAL_REPS, totalReps);
        Uri uri = ContentUris.withAppendedId(DatabaseContract.ExercisesTable.CONTENT_URI, exerciseId);
        context.getContentResolver().update(uri, values, null, null);
    }

    public static List<ExerciseHistory> getExerciseHistory(Context context, long exerciseId) {
        List<ExerciseHistory> exercisesHistory = new ArrayList<>();
        String[] projection =
                {
                        DatabaseContract.ExercisesTable.COLUMN_NAME,
                        DatabaseContract.ExerciseHistoryTable.COLUMN_HOW_MANY_REPS_TIME,
                        DatabaseContract.ExerciseHistoryTable.TABLE_NAME +
                                "." + DatabaseContract.ExerciseHistoryTable.COLUMN_DATE
                };

        String selection = DatabaseContract.ExerciseHistoryTable.COLUMN_EXERCISE_ID + "=?";
        String[] selectionArgs = {String.valueOf(exerciseId)};
        String sortOrder = DatabaseContract.ExerciseHistoryTable.TABLE_NAME + "." + DatabaseContract.ExerciseHistoryTable.COLUMN_DATE + " DESC";

        if (exerciseId == -1) {
            selection = null;
            selectionArgs = null;
        }

        Cursor cursor = context.getContentResolver()
                .query(DatabaseContract.ExerciseHistoryTable.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ExerciseHistory exerciseHistory = new ExerciseHistory();
                exerciseHistory.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_NAME)));
                exerciseHistory.setHowMany(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ExerciseHistoryTable.COLUMN_HOW_MANY_REPS_TIME)));
                exerciseHistory.setDate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExerciseHistoryTable.COLUMN_DATE)));
                exercisesHistory.add(exerciseHistory);
            }
        }
        cursor.close();
        return exercisesHistory;
    }

}
