package org.geometerplus.android.fbreader.provider;

import org.geometerplus.android.fbreader.annotation.database.AnnotationsTables;

import android.content.ContentProvider;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class BaseProvider extends ContentProvider {
	
	DatabaseHelper dbHelper;

	private static final String DATABASE_NAME = "annotations.db";
	
	private static final int DATABASE_VERSION = 2;

	static class DatabaseHelper extends SQLiteOpenHelper {

		// Database creation SQL statement
		private static final String SEMAPPS_CREATE = 
			"CREATE TABLE SemApps(" +
				"_id TEXT PRIMARY KEY," +
				"name TEXT," +
				"updated_at TEXT)";
		
		private static final String EPUBS_CREATE = 
			"CREATE TABLE EPubs(" +
				"_id TEXT PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"updated_at TEXT NOT NULL," +
				"file_name TEXT NOT NULL," +
				"file_path TEXT UNIQUE NOT NULL," +
				"local_path TEXT UNIQUE," +
				"semapp_id TEXT," +
				"FOREIGN KEY(semapp_id) REFERENCES SemApps(_id))";
		
		private static final String ANNOTATIONS_CREATE = 
			"CREATE TABLE Annotations(" +
				"_id TEXT PRIMARY KEY," +
				"created TEXT," +
				"modified TEXT," +
				"category TEXT," +
				"tags TEXT," +
				"author_name TEXT," +
				"target_bookid INTEGER," +
				"target_markedtext TEXT," +
				"target_annotationid TEXT," +
				"target_documentidentifier_isbn TEXT," +
				"target_documentidentifier_title TEXT," +
				"target_documentidentifier_publicationdate TEXT," +
				"target_range_start_part TEXT," +
				"target_range_start_path_xpath TEXT," +
				"target_range_start_path_charoffset INTEGER," +
				"target_range_end_part TEXT," +
				"target_range_end_path_xpath TEXT," +
				"target_range_end_path_charoffset INTEGER," +
				"highlightcolor INTEGER," +
				"underlined TEXT," +
				"crossout TEXT," +
				"content TEXT," +
				"upb_id TEXT," +
				"updated_at TEXT," +
				"epub_id TEXT," +
				"FOREIGN KEY(epub_id) REFERENCES EPubs(_id))";
		
		private static final String AUTHORS_CREATE = 
			"CREATE TABLE Authors(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"name TEXT," +
				"epub_id TEXT," +
				"FOREIGN KEY(epub_id) REFERENCES Annotations(epub_id))";
		
		DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SEMAPPS_CREATE);
			db.execSQL(EPUBS_CREATE);
			db.execSQL(ANNOTATIONS_CREATE);
			db.execSQL(AUTHORS_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(BaseProvider.class.getName(), "Upgrading database from version "
					+ oldVersion + " to " + newVersion);
			db.execSQL("DROP TABLE IF EXISTS SemApps");
			db.execSQL("DROP TABLE IF EXISTS EPubs");
			db.execSQL("DROP TABLE IF EXISTS Annotations");
			db.execSQL("DROP TABLE IF EXISTS Authors");
			onCreate(db);
		}
		
		@Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(AnnotationsTables.class.getName(), "Downgrading database from version "
					+ oldVersion + " to " + newVersion);
			db.execSQL("DROP TABLE IF EXISTS SemApps");
			db.execSQL("DROP TABLE IF EXISTS EPubs");
			db.execSQL("DROP TABLE IF EXISTS Annotations");
			db.execSQL("DROP TABLE IF EXISTS Authors");
			onCreate(db);
        }
		
	}
	
	@Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return (dbHelper != null);
    }
}
