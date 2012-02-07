package org.geometerplus.android.fbreader.annotation.database;

import org.geometerplus.android.util.UIUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AnnotationsTables {
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
			"annotation_id TEXT," +
			"FOREIGN KEY(annotation_id) REFERENCES Annotations(_id))";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SEMAPPS_CREATE);
		database.execSQL(EPUBS_CREATE);
		database.execSQL(ANNOTATIONS_CREATE);
		database.execSQL(AUTHORS_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(AnnotationsTables.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion);
		database.execSQL("DROP TABLE IF EXISTS SemApps");
		database.execSQL("DROP TABLE IF EXISTS EPubs");
		database.execSQL("DROP TABLE IF EXISTS Annotations");
		database.execSQL("DROP TABLE IF EXISTS Authors");
		onCreate(database);
	}
	
	public static void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(AnnotationsTables.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion);
		database.execSQL("DROP TABLE IF EXISTS SemApps");
		database.execSQL("DROP TABLE IF EXISTS EPubs");
		database.execSQL("DROP TABLE IF EXISTS Annotations");
		database.execSQL("DROP TABLE IF EXISTS Authors");
		onCreate(database);
	}
}