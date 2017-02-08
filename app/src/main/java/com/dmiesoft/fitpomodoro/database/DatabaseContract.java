package com.dmiesoft.fitpomodoro.database;

import android.provider.BaseColumns;

public class DatabaseContract {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pomidoras.db";
    private static final String TEXT = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String CREATE_TABLE_STRING = "CREATE TABLE ";

    private DatabaseContract() {}

    public static abstract class ExercisesTable implements BaseColumns {
        public static final String TABLE_NAME = "exercises";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_GROUP_ID = "exercise_group_id";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_DATE = "date_timestamp";

        public static final String CREATE_TABLE =
                CREATE_TABLE_STRING + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + TEXT + COMMA_SEP +
                        COLUMN_TYPE + TEXT + COMMA_SEP +
                        COLUMN_DESCRIPTION + TEXT + COMMA_SEP +
                        COLUMN_IMAGE + TEXT + COMMA_SEP +
                        COLUMN_DATE + " TIMESTAMP NOT NULL DEFAULT current_timestamp, " +
                        COLUMN_GROUP_ID + " INTEGER," +
                        " FOREIGN KEY(" + COLUMN_GROUP_ID + ") REFERENCES " + ExercisesGroupsTable.TABLE_NAME + "(" + ExercisesGroupsTable._ID + ") ON DELETE CASCADE" +
                        ");";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class ExercisesGroupsTable implements BaseColumns {
        public static final String TABLE_NAME = "exercises_groups";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_DATE = "date_timestamp";

        public static final String CREATE_TABLE =
                CREATE_TABLE_STRING + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + TEXT + COMMA_SEP +
                        COLUMN_IMAGE + TEXT + COMMA_SEP +
                        COLUMN_DATE + " TIMESTAMP NOT NULL DEFAULT current_timestamp" +
                        ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
