package com.dmiesoft.fitpomodoro.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class ExercisesContentProvider extends ContentProvider {

    private static final String TAG = "ECP";

    private static final int
            EXERCISES = 1,
            EXERCISE_ID = 2,
            EXERCISES_GROUPS = 3,
            EXERCISES_GROUP_ID = 4,
            FAVORITES = 5,
            FAVORITES_ID = 6,
            EXERCISE_HISTORY = 7,
            EXERCISE_HISTORY_ID = 8,
            FAVORITE_EXERCISES = 9;

    public static final String CONTENT_AUTHORITY = "com.dmiesoft.fitpomodoro.database.databasecontract";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_EXERCISES = DatabaseContract.ExercisesTable.TABLE_NAME;
    public static final String PATH_EXERCISES_GROUPS = DatabaseContract.ExercisesGroupsTable.TABLE_NAME;
    public static final String PATH_FAVORITES = DatabaseContract.FavoritesTable.TABLE_NAME;
    public static final String PATH_FAVORITE_EXERCISES = DatabaseContract.FavExIdsTable.TABLE_NAME;
    public static final String PATH_EXERCISE_HISTORY = DatabaseContract.ExerciseHistoryTable.TABLE_NAME;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISES, EXERCISES);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISES + "/#", EXERCISE_ID);

        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISES_GROUPS, EXERCISES_GROUPS);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISES_GROUPS + "/#", EXERCISES_GROUP_ID);

        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_FAVORITES, FAVORITES);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_FAVORITES + "/#", FAVORITES_ID);

        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISE_HISTORY, EXERCISE_HISTORY);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISE_HISTORY + "/#", EXERCISE_HISTORY_ID);

        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_FAVORITE_EXERCISES, FAVORITE_EXERCISES);
    }

    private SQLiteDatabase mDatabase;
    private DatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        mDatabase = mDbHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        int match = uriMatcher.match(uri);

        String inExercisesFavoritesTBL = DatabaseContract.ExercisesTable.TABLE_NAME +
                " INNER JOIN " + DatabaseContract.FavExIdsTable.TABLE_NAME +
                " ON " + DatabaseContract.ExercisesTable._ID + " = " + DatabaseContract.FavExIdsTable.COLUMN_EXERCISE_ID;

        String inExercisesExercisesHistoryTable = DatabaseContract.ExercisesTable.TABLE_NAME +
                " INNER JOIN " + DatabaseContract.ExerciseHistoryTable.TABLE_NAME +
                " ON " + DatabaseContract.ExercisesTable.TABLE_NAME + "." + DatabaseContract.ExercisesTable._ID +
                " = " + DatabaseContract.ExerciseHistoryTable.COLUMN_EXERCISE_ID;


        SQLiteQueryBuilder queryBuilder;

        switch (match) {
            case EXERCISES:
                if (selection == null) {
                    cursor = mDatabase.query(DatabaseContract.ExercisesTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                }
                if (selection.contains(DatabaseContract.FavExIdsTable.COLUMN_FAVORITE_ID)) {
                    queryBuilder = new SQLiteQueryBuilder();
                    queryBuilder.setTables(inExercisesFavoritesTBL);
                    cursor = queryBuilder.query(mDatabase, projection, selection, selectionArgs, null, null, sortOrder);
                } else {
                    cursor = mDatabase.query(DatabaseContract.ExercisesTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                }
                break;
            case EXERCISE_ID:
                break;

            case EXERCISES_GROUPS:
                cursor = mDatabase.query(
                        DatabaseContract.ExercisesGroupsTable.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVORITES:
                cursor = mDatabase.query(
                        DatabaseContract.FavoritesTable.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVORITES_ID:
                break;

            case EXERCISE_HISTORY:
                queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables(inExercisesExercisesHistoryTable);
                cursor = queryBuilder.query(mDatabase, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case EXERCISE_HISTORY_ID:
                break;

            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case EXERCISES:
                return insertData(uri, values, DatabaseContract.ExercisesTable.TABLE_NAME);
            case EXERCISES_GROUPS:
                return insertData(uri, values, DatabaseContract.ExercisesGroupsTable.TABLE_NAME);
            case FAVORITES:
                return insertData(uri, values, DatabaseContract.FavoritesTable.TABLE_NAME);
            case FAVORITE_EXERCISES:
                Log.i(TAG, "insert: ");
                return insertData(uri, values, DatabaseContract.FavExIdsTable.TABLE_NAME);
            case EXERCISE_HISTORY:
                return insertData(uri, values, DatabaseContract.ExerciseHistoryTable.TABLE_NAME);
        }
        return null;
    }

    private Uri insertData(Uri uri, ContentValues values, String tableName) {
        long id = mDatabase.insert(tableName, null, values);
        if (id == -1) {
            Log.e(TAG, "insertData error for URI " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case EXERCISE_ID:
                selection = DatabaseContract.ExercisesTable._ID + "=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                return deleteData(uri, selection, selectionArgs, DatabaseContract.ExercisesTable.TABLE_NAME);
            case EXERCISES_GROUP_ID:
                selection = DatabaseContract.ExercisesGroupsTable._ID + "=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                return deleteData(uri, selection, selectionArgs, DatabaseContract.ExercisesGroupsTable.TABLE_NAME);
            case FAVORITE_EXERCISES:
                return deleteData(uri, selection, selectionArgs, DatabaseContract.FavExIdsTable.TABLE_NAME);
            case FAVORITES_ID:
                selection = DatabaseContract.FavoritesTable._ID + "=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                return deleteData(uri, selection, selectionArgs, DatabaseContract.FavoritesTable.TABLE_NAME);
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    private int deleteData(Uri uri, String selection, String[] selectionArgs, String tableName) {
        int id = mDatabase.delete(tableName, selection, selectionArgs);
        if (id == -1) {
            Log.e(TAG, "insertData error for URI " + uri);
        }
        return id;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case EXERCISE_ID:
                selection = DatabaseContract.ExercisesTable._ID + "=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                return updateData(uri, values, selection, selectionArgs, DatabaseContract.ExercisesTable.TABLE_NAME);
            case EXERCISES_GROUP_ID:
                selection = DatabaseContract.ExercisesGroupsTable._ID + "=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                return updateData(uri, values, selection, selectionArgs, DatabaseContract.ExercisesGroupsTable.TABLE_NAME);
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    private int updateData(Uri uri, ContentValues values, String selection, String[] selectionArgs, String tableName) {
        int id = mDatabase.update(tableName, values, selection, selectionArgs);
        if (id == -1) {
            Log.e(TAG, "updateData error for URI " + uri);
        }
        return id;
    }
}
