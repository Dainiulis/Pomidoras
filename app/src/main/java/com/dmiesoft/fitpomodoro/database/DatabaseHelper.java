package com.dmiesoft.fitpomodoro.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.ExercisesGroupsTable.CREATE_TABLE);
        db.execSQL(DatabaseContract.ExercisesTable.CREATE_TABLE);
        db.execSQL(DatabaseContract.FavoritesTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.ExercisesGroupsTable.DELETE_TABLE);
        db.execSQL(DatabaseContract.ExercisesTable.DELETE_TABLE);
        db.execSQL(DatabaseContract.FavoritesTable.DELETE_TABLE);
        onCreate(db);
    }
}
