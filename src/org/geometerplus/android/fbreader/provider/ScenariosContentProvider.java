package org.geometerplus.android.fbreader.provider;

import java.util.HashMap;

import org.geometerplus.android.fbreader.annotation.database.DBEPub.DBEPubs;
import org.geometerplus.android.fbreader.annotation.database.DBScenario.DBScenarios;

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
public class ScenariosContentProvider extends BaseProvider {

    private static final String SCENARIOS_TABLE_NAME = "Scenarios";

    public static final String AUTHORITY = "org.geometerplus.android.fbreader.provider.ScenariosContentProvider";

    private static final UriMatcher sUriMatcher;

    private static final int SCENARIOS = 1;

    private static HashMap<String, String> ePubsProjectionMap;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case SCENARIOS:
                count = db.delete(SCENARIOS_TABLE_NAME, where, whereArgs);
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
            case SCENARIOS:
                return DBEPubs.CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != SCENARIOS) { throw new IllegalArgumentException("Unknown URI " + uri); }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(SCENARIOS_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(DBEPubs.CONTENT_URI, rowId);
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
            case SCENARIOS:
                qb.setTables(SCENARIOS_TABLE_NAME);
                qb.setProjectionMap(ePubsProjectionMap);
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
            case SCENARIOS:
                count = db.update(SCENARIOS_TABLE_NAME, values, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, SCENARIOS_TABLE_NAME, SCENARIOS);

        ePubsProjectionMap = new HashMap<String, String>();
        ePubsProjectionMap.put(DBScenarios.SCENARIO_ID, DBScenarios.SCENARIO_ID);
        ePubsProjectionMap.put(DBScenarios.SEMAPP_ID, DBScenarios.SEMAPP_ID);
        ePubsProjectionMap.put(DBScenarios.EPUB_ID, DBScenarios.EPUB_ID);
        ePubsProjectionMap.put(DBScenarios.NAME, DBScenarios.NAME);
        ePubsProjectionMap.put(DBScenarios.VERSION, DBScenarios.VERSION);
        ePubsProjectionMap.put(DBScenarios.ACTIVE, DBScenarios.ACTIVE);
        ePubsProjectionMap.put(DBScenarios.CREATED_AT, DBScenarios.CREATED_AT);
        ePubsProjectionMap.put(DBScenarios.UPDATED_AT, DBScenarios.UPDATED_AT);

    }
}
