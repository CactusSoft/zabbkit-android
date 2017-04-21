package ru.zabbkit.android.db.helper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.zabbkit.android.db.entity.Bookmark;
import ru.zabbkit.android.db.entity.Host;
import ru.zabbkit.android.utils.L;

public final class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "databaseName";
    private static final int DATABASE_VERSION = 8;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, Bookmark.TABLE_NAME);
        createTable(db, Host.TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        L.i("Upgrading from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + Bookmark.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Host.TABLE_NAME);
        onCreate(db);
    }

    private void createTable(SQLiteDatabase db, String table) {
        L.i("--- onCreate database ---");
        try {
            if (table.equals(Bookmark.TABLE_NAME)) {
                db.execSQL("CREATE TABLE " + table + " (" + Bookmark.COLUMN_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Bookmark.COLUMN_SERVER_ID
                        + " text, " + Bookmark.COLUMN_GRAPH_ID + " text, " + Bookmark.COLUMN_PARAM_NAME
                        + " text, " + Bookmark.COLUMN_SERVER_NAME + " text, "
                        + Bookmark.COLUMN_PERIOD + " integer" + ");");
            } else if (table.equals(Host.TABLE_NAME)) {
                db.execSQL("CREATE TABLE " + table + " (" + Host.COLUMN_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Host.COLUMN_URL
                        + " text, " + Host.COLUMN_NAME + " text, " + Host.COLUMN_LOGIN
                        + " text, " + Host.COLUMN_PASSWORD + " text, "
                        + Host.COLUMN_SSL + " integer" + ");");
            }
            L.d("Table Created");
        } catch (SQLException e) {
            L.e(e);
        }
    }
}
