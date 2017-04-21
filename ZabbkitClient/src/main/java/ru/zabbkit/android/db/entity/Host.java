package ru.zabbkit.android.db.entity;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Alex.Shimborsky on 04/12/2014.
 */
public class Host {

    public static final String TABLE_NAME = "hosts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LOGIN = "login";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_SSL = "ssl";

    /*
    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_URL
            + " text not null, " + COLUMN_NAME + " text not null, " + COLUMN_LOGIN
            + " text not null, " + COLUMN_PASSWORD + " text not null, "
            + COLUMN_SSL + " integer" + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
    */

}
