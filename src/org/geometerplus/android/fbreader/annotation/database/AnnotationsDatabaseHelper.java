package org.geometerplus.android.fbreader.annotation.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AnnotationsDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "annotations.db";
	
	private static final int DATABASE_VERSION = 2;
	
	public AnnotationsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		AnnotationsTables.onCreate(database);
	}

	// Method is called during an upgrade of the database (increasing the database version)
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		AnnotationsTables.onUpgrade(database, oldVersion, newVersion);
	}
	
	// Method is called during an downgrade of the database (decreasing the database version)
	@Override
	public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		AnnotationsTables.onDowngrade(database, oldVersion, newVersion);
	}

}
