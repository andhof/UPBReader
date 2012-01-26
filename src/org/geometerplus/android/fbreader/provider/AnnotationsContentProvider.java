package org.geometerplus.android.fbreader.provider;

import java.util.HashMap;

import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;

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
public class AnnotationsContentProvider extends BaseProvider {

    private static final String ANNOTATIONS_TABLE_NAME = "Annotations";

    public static final String AUTHORITY = "org.geometerplus.android.fbreader.provider.AnnotationsContentProvider";

    private static final UriMatcher sUriMatcher;

    private static final int ANNOTATIONS = 1;

    private static HashMap<String, String> annotationsProjectionMap;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case ANNOTATIONS:
                count = db.delete(ANNOTATIONS_TABLE_NAME, where, whereArgs);
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
            case ANNOTATIONS:
                return DBAnnotations.CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != ANNOTATIONS) { throw new IllegalArgumentException("Unknown URI " + uri); }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(ANNOTATIONS_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(DBAnnotations.CONTENT_URI, rowId);
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
            case ANNOTATIONS:
                qb.setTables(ANNOTATIONS_TABLE_NAME);
                qb.setProjectionMap(annotationsProjectionMap);
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
            case ANNOTATIONS:
                count = db.update(ANNOTATIONS_TABLE_NAME, values, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, ANNOTATIONS_TABLE_NAME, ANNOTATIONS);

        annotationsProjectionMap = new HashMap<String, String>();
        annotationsProjectionMap.put(DBAnnotations.ANNOTATION_ID, DBAnnotations.ANNOTATION_ID);
        annotationsProjectionMap.put(DBAnnotations.CREATED, DBAnnotations.CREATED);
        annotationsProjectionMap.put(DBAnnotations.MODIFIED, DBAnnotations.MODIFIED);
        annotationsProjectionMap.put(DBAnnotations.CATEGORY, DBAnnotations.CATEGORY);
        annotationsProjectionMap.put(DBAnnotations.TAGS, DBAnnotations.TAGS);
        annotationsProjectionMap.put(DBAnnotations.AUTHOR_NAME, DBAnnotations.AUTHOR_NAME);
        annotationsProjectionMap.put(DBAnnotations.BOOKID, DBAnnotations.BOOKID);
        annotationsProjectionMap.put(DBAnnotations.TARGET_ANNOTATION_ID, DBAnnotations.TARGET_ANNOTATION_ID);
        annotationsProjectionMap.put(DBAnnotations.ISBN, DBAnnotations.ISBN);
        annotationsProjectionMap.put(DBAnnotations.TITLE, DBAnnotations.TITLE);
        annotationsProjectionMap.put(DBAnnotations.PUBLICATIONDATE, DBAnnotations.PUBLICATIONDATE);
        annotationsProjectionMap.put(DBAnnotations.START_PART, DBAnnotations.START_PART);
        annotationsProjectionMap.put(DBAnnotations.START_PATH_XPATH, DBAnnotations.START_PATH_XPATH);
        annotationsProjectionMap.put(DBAnnotations.START_PATH_CHAROFFSET, DBAnnotations.START_PATH_CHAROFFSET);
        annotationsProjectionMap.put(DBAnnotations.END_PART, DBAnnotations.END_PART);
        annotationsProjectionMap.put(DBAnnotations.END_PATH_XPATH, DBAnnotations.END_PATH_XPATH);
        annotationsProjectionMap.put(DBAnnotations.END_PATH_CHAROFFSET, DBAnnotations.END_PATH_CHAROFFSET);
        annotationsProjectionMap.put(DBAnnotations.HIGHLIGHTCOLOR, DBAnnotations.HIGHLIGHTCOLOR);
        annotationsProjectionMap.put(DBAnnotations.UNDERLINED, DBAnnotations.UNDERLINED);
        annotationsProjectionMap.put(DBAnnotations.CROSSOUT, DBAnnotations.CROSSOUT);
        annotationsProjectionMap.put(DBAnnotations.CONTENT, DBAnnotations.CONTENT);
        annotationsProjectionMap.put(DBAnnotations.UPB_ID, DBAnnotations.UPB_ID);
        annotationsProjectionMap.put(DBAnnotations.UPDATED_AT, DBAnnotations.UPDATED_AT);
        annotationsProjectionMap.put(DBAnnotations.EPUB_ID, DBAnnotations.EPUB_ID);
        
    }
}
