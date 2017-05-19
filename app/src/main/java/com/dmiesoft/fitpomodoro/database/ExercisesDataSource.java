package com.dmiesoft.fitpomodoro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExerciseHistory;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.model.Favorite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExercisesDataSource {

    private static final String TAG = "EDS";
    SQLiteOpenHelper dbHelper;
    SQLiteDatabase database;

    private static String[] exercises_columns = {
            DatabaseContract.ExercisesTable._ID,
            DatabaseContract.ExercisesTable.COLUMN_NAME,
            DatabaseContract.ExercisesTable.COLUMN_TYPE,
            DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION,
            DatabaseContract.ExercisesTable.COLUMN_GROUP_ID,
            DatabaseContract.ExercisesTable.COLUMN_IMAGE,
            DatabaseContract.ExercisesTable.COLUMN_DATE
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

    public ExercisesDataSource(Context context) {
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
        values.put(DatabaseContract.ExercisesGroupsTable.COLUMN_IMAGE, exercisesGroup.getImage());

        long newRowId = database.insert(DatabaseContract.ExercisesGroupsTable.TABLE_NAME, null, values);
        exercisesGroup.setId(newRowId);
        return exercisesGroup;
    }

    public List<ExercisesGroup> findExerciseGroups(String selection, String[] selectionArgs) {
        List<ExercisesGroup> exercisesGroups = new ArrayList<>();
        String orderBy = DatabaseContract.ExercisesGroupsTable.COLUMN_NAME + " ASC";

        Cursor cursor = database.query(
                DatabaseContract.ExercisesGroupsTable.TABLE_NAME,
                exercises_groups_columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy);

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

    public void updateExercisesGroup(ExercisesGroup exercisesGroup) {
        String where = DatabaseContract.ExercisesGroupsTable._ID + "=?";
        String[] whereArgs = {String.valueOf(exercisesGroup.getId())};

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesGroupsTable.COLUMN_NAME, exercisesGroup.getName());
        values.put(DatabaseContract.ExercisesGroupsTable.COLUMN_IMAGE, exercisesGroup.getImage());
        database.update(DatabaseContract.ExercisesGroupsTable.TABLE_NAME, values, where, whereArgs);
    }

    public void deleteExercisesGroup(long id) {
        String where = DatabaseContract.ExercisesGroupsTable._ID + "=?";
        String[] whereArgs = {String.valueOf(id)};
        database.delete(DatabaseContract.ExercisesGroupsTable.TABLE_NAME, where, whereArgs);
    }

    /*
    Manage database "exercises" table
     */
    public Exercise createExercise(Exercise exercise) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesTable.COLUMN_NAME, exercise.getName());
        values.put(DatabaseContract.ExercisesTable.COLUMN_TYPE, exercise.getType());
        values.put(DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION, exercise.getDescription());
        values.put(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID, exercise.getExerciseGroupId());
        values.put(DatabaseContract.ExercisesTable.COLUMN_IMAGE, exercise.getImage());

        long newRowId = database.insert(DatabaseContract.ExercisesTable.TABLE_NAME, null, values);
        exercise.setId(newRowId);
        return exercise;
    }

    public void updateExercise(Exercise exercise) {
        String where = DatabaseContract.ExercisesTable._ID + "=?";
        String[] whereArgs = {String.valueOf(exercise.getId())};

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExercisesTable.COLUMN_NAME, exercise.getName());
        values.put(DatabaseContract.ExercisesTable.COLUMN_TYPE, exercise.getType());
        values.put(DatabaseContract.ExercisesTable.COLUMN_DESCRIPTION, exercise.getDescription());
        values.put(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID, exercise.getExerciseGroupId());
        values.put(DatabaseContract.ExercisesTable.COLUMN_IMAGE, exercise.getImage());

        database.update(DatabaseContract.ExercisesTable.TABLE_NAME, values, where, whereArgs);
    }

    public List<Exercise> findExercises(String selection, String[] selectionArgs) {
        String orderBy = DatabaseContract.ExercisesTable.COLUMN_NAME + " ASC";
        List<Exercise> exercises = new ArrayList<>();

        Cursor cursor = database.query(
                DatabaseContract.ExercisesTable.TABLE_NAME,
                exercises_columns,
                selection,
                selectionArgs,
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
                exercise.setExerciseGroupId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_GROUP_ID)));
                exercise.setImage(cursor.getString(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_IMAGE)));
                exercise.setDate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.ExercisesTable.COLUMN_DATE)));
                exercises.add(exercise);
            }
        }
        cursor.close();
        return exercises;
    }

    /**
     * @param selection
     * @param selectionArgs
     * @param favoriteId    pass -1 if not looking from favorites
     * @return
     */
    public List<Long> getExercisesIds(String selection, String[] selectionArgs, long favoriteId) {
        String[] exercise_id_column_name = {DatabaseContract.ExercisesTable._ID};
        List<Long> idList = new ArrayList<>();
        Cursor cursor;
        if (favoriteId == -1) {
            cursor = database.query(
                    DatabaseContract.ExercisesTable.TABLE_NAME,
                    exercise_id_column_name,
                    selection, selectionArgs, null, null, null);
        } else {
            cursor = database.rawQuery(
                    "SELECT " + DatabaseContract.ExercisesTable._ID +
                            " FROM " + DatabaseContract.ExercisesTable.TABLE_NAME +
                            " INNER JOIN " + DatabaseContract.FavExIdsTable.TABLE_NAME +
                            " ON " + DatabaseContract.ExercisesTable._ID + " = " + DatabaseContract.FavExIdsTable.COLUMN_EXERCISE_ID +
                            " WHERE " + DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID + " = " + String.valueOf(favoriteId),
                    null
            );
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

    public void deleteExercise(long id) {
        String where = DatabaseContract.ExercisesGroupsTable._ID + "=?";
        String[] whereArgs = {String.valueOf(id)};
        database.delete(DatabaseContract.ExercisesTable.TABLE_NAME, where, whereArgs);
    }

    // Favorites management
    public void createFavorite(String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.FavoritesTable.COLUMN_NAME, name);
        database.insert(DatabaseContract.FavoritesTable.TABLE_NAME, null, values);
    }

    public List<Favorite> getAllFavorites() {
        List<Favorite> favorites = new ArrayList<>();
        String orderBy = DatabaseContract.FavoritesTable.COLUMN_NAME + " ASC";

        Cursor cursor = database.query(
                DatabaseContract.FavoritesTable.TABLE_NAME,
                favorites_columns,
                null, null, null, null,
                orderBy);

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

    public void createFavExIds(HashMap<Long, Long> favExIds) {
        for (Map.Entry<Long, Long> entry : favExIds.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.FavExIdsTable.COLUMN_EXERCISE_ID, entry.getKey());
            values.put(DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID, entry.getValue());
            long rowID = database.insert(DatabaseContract.FavExIdsTable.TABLE_NAME, null, values);
        }
    }

    public List<Exercise> findFavoriteExercises(long favoriteId) {
        List<Exercise> exercises = new ArrayList<>();

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + DatabaseContract.ExercisesTable.TABLE_NAME +
                        " INNER JOIN " + DatabaseContract.FavExIdsTable.TABLE_NAME +
                        " ON " + DatabaseContract.ExercisesTable._ID +
                        " = " + DatabaseContract.FavExIdsTable.COLUMN_EXERCISE_ID +
                        " WHERE " + DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID +
                        " = " + String.valueOf(favoriteId) +
                        " ORDER BY " + DatabaseContract.ExercisesTable.COLUMN_GROUP_ID + " ASC",
                null
        );

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
                exercises.add(exercise);
            }
        }
        cursor.close();
        return exercises;
    }

    public void removeExercisesFromFavorites(List<Exercise> exercises, List<Integer> unfavoriteIdList, long favoriteId) {
        for (int i = 0; i < unfavoriteIdList.size(); i++) {
            String where = DatabaseContract.FavExIdsTable.COLUMN_EXERCISE_ID + "=? AND " +
                    DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID + "=?";
            String[] whereArgs = {String.valueOf(exercises.get(unfavoriteIdList.get(i)).getId()), String.valueOf(favoriteId)};
            database.delete(DatabaseContract.FavExIdsTable.TABLE_NAME, where, whereArgs);
        }
    }

    public void deleteFavorite(long favoriteId) {
        String where = DatabaseContract.FavoritesTable._ID + "=?";
        String[] whereArgs = {String.valueOf(favoriteId)};
        database.delete(DatabaseContract.FavoritesTable.TABLE_NAME, where, whereArgs);
    }

    public void saveExerciseHistory(int howMany, long exerciseId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ExerciseHistoryTable.COLUMN_EXERCISE_ID, exerciseId);
        values.put(DatabaseContract.ExerciseHistoryTable.COLUMN_HOW_MANY_REPS_TIME, howMany);
        database.insert(DatabaseContract.ExerciseHistoryTable.TABLE_NAME, null, values);
    }

    public List<ExerciseHistory> getExerciseHistory(long exerciseId) {
        List<ExerciseHistory> exercisesHistory = new ArrayList<>();
        String commaSep = ", ";
        String selection = DatabaseContract.ExercisesTable.COLUMN_NAME + commaSep +
                DatabaseContract.ExerciseHistoryTable.COLUMN_HOW_MANY_REPS_TIME + commaSep +
                DatabaseContract.ExerciseHistoryTable.TABLE_NAME + "." + DatabaseContract.ExerciseHistoryTable.COLUMN_DATE;
        Cursor cursor = database.rawQuery(
                "SELECT " + selection + " FROM " + DatabaseContract.ExercisesTable.TABLE_NAME +
                        " INNER JOIN " + DatabaseContract.ExerciseHistoryTable.TABLE_NAME +
                        " ON " + DatabaseContract.ExercisesTable.TABLE_NAME + "." + DatabaseContract.ExercisesTable._ID +
                        " = " + DatabaseContract.ExerciseHistoryTable.COLUMN_EXERCISE_ID +
                        " WHERE " + DatabaseContract.ExerciseHistoryTable.COLUMN_EXERCISE_ID +
                        " = " + String.valueOf(exerciseId) +
                        " ORDER BY " + DatabaseContract.ExerciseHistoryTable.TABLE_NAME + "." + DatabaseContract.ExerciseHistoryTable.COLUMN_DATE + " DESC",
                null
        );

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
