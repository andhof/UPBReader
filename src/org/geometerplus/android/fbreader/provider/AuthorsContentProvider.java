package org.geometerplus.android.fbreader.provider;

import java.util.HashMap;

import org.geometerplus.android.fbreader.annotation.database.DBAuthor.DBAuthors;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * @author Andreas Hoffmann
 * 
 */
public class AuthorsContentProvider extends BaseProvider {

    private static final String AUTHORS_TABLE_NAME = "Authors";

    public static final String AUTHORITY = "org.geometerplus.android.fbreader.provider.AuthorsContentProvider";

    private static final UriMatcher sUriMatcher;

    private static final int AUTHORS = 1;

    private static HashMap<String, String> authorsProjectionMap;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case AUTHORS:
                count = db.delete(AUTHORS_TABLE_NAME, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case AUTHORS:
                return DBAuthors.CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != AUTHORS) { throw new IllegalArgumentException("Unknown URI " + uri); }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(AUTHORS_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(DBAuthors.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case AUTHORS:
                qb.setTables(AUTHORS_TABLE_NAME);
                qb.setProjectionMap(authorsProjectionMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case AUTHORS:
                count = db.update(AUTHORS_TABLE_NAME, values, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, AUTHORS_TABLE_NAME, AUTHORS);

        authorsProjectionMap = new HashMap<String, String>();
        authorsProjectionMap.put(DBAuthors.AUTHOR_ID, DBAuthors.AUTHOR_ID);
        authorsProjectionMap.put(DBAuthors.NAME, DBAuthors.NAME);
        authorsProjectionMap.put(DBAuthors.ANNOTATION_ID, DBAuthors.ANNOTATION_ID);

    }
}
