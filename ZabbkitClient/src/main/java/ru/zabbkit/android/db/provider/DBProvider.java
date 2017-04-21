package ru.zabbkit.android.db.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import ru.zabbkit.android.db.entity.Bookmark;
import ru.zabbkit.android.db.entity.Host;
import ru.zabbkit.android.db.helper.DbHelper;

public class DBProvider extends ContentProvider {


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String AUTHORITY = "ru.zabbkit.android.db.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private static final String TABLE_HOSTS = "hosts";
    private static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String HOSTS_CONTENT_URI = "content://" + AUTHORITY + "/hosts";
    public static final String BOOKMARKS_CONTENT_URI = "content://" + AUTHORITY + "/bookmarks";

    public static final String UNIQUE_BOOKMARK_QUERY = Bookmark.COLUMN_GRAPH_ID + "= ? "
            + " and " + Bookmark.COLUMN_PERIOD + "= ?";

    private static final int HOSTS = 10;
    private static final int HOSTS_ID = 11;
    private static final int BOOKMARKS = 20;

    private DbHelper database;

    static {
        sUriMatcher.addURI(AUTHORITY, TABLE_HOSTS, HOSTS);
        sUriMatcher.addURI(AUTHORITY, TABLE_HOSTS + "/#", HOSTS_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_BOOKMARKS, BOOKMARKS);
    }

    public DBProvider() {

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case HOSTS:
                rowsDeleted = sqlDB.delete(Host.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case HOSTS_ID:
                rowsDeleted = sqlDB.delete(Host.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case BOOKMARKS:
                rowsDeleted = sqlDB.delete(Bookmark.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        String tableName;
        switch (uriType) {
            case HOSTS:
                id = sqlDB.insert(Host.TABLE_NAME, null, values);
                tableName = Host.TABLE_NAME;
                break;
            case BOOKMARKS:
                id = sqlDB.insert(Bookmark.TABLE_NAME, null, values);
                tableName = Bookmark.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(tableName + "/" + id);
    }

    @Override
    public boolean onCreate() {
        database = new DbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case HOSTS:
                tableName = TABLE_HOSTS;
                break;
            case BOOKMARKS:
                tableName = TABLE_BOOKMARKS;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = db.query(tableName,
                null, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case HOSTS:
                rowsUpdated = sqlDB.update(Host.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case BOOKMARKS:
                rowsUpdated = sqlDB.update(Bookmark.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            /*
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(TodoTable.TABLE_TODO,
                            values,
                            TodoTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(TodoTable.TABLE_TODO,
                            values,
                            TodoTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            */
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
