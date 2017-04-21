package ru.zabbkit.android.db.entity;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Alex.Shimborsky on 04/12/2014.
 */
public class Bookmark {

    public static final String TABLE_NAME = "bookmarks";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PERIOD = "period";
    public static final String COLUMN_SERVER_ID = "serverId";
    public static final String COLUMN_GRAPH_ID = "graphId";
    public static final String COLUMN_PARAM_NAME = "paramName";
    public static final String COLUMN_SERVER_NAME = "serverName";

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_SERVER_ID
            + " text, " + COLUMN_GRAPH_ID + " text, " + COLUMN_PARAM_NAME
            + " text, " + COLUMN_SERVER_NAME + " text, "
            + COLUMN_PERIOD + " integer" + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
}
