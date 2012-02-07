package org.geometerplus.android.fbreader.annotation.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class AnnotationsDbAdapter {
	
	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_UPDATED_AT = "updated_at";
	public static final String KEY_FILENAME = "file_name";
	public static final String KEY_FILEPATH = "file_path";
	public static final String KEY_SEMAPP_ID = "semapp_id";
	public static final String KEY_LOCAL_PATH = "local_path";
	public static final String KEY_CREATED = "created";
	public static final String KEY_MODIFIED = "modified";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_TAGS = "tags";
	public static final String KEY_AUTHOR_NAME = "author_name";
	public static final String KEY_MARKEDTEXT = "target_markedtext";
	public static final String KEY_TARGETANNOTATIONID = "target_annotationid";
	public static final String KEY_BOOKID = "target_bookid";
	public static final String KEY_ISBN = "target_documentidentifier_isbn";
	public static final String KEY_TITLE = "target_documentidentifier_title";
	public static final String KEY_PUBLICATIONDATE = "target_documentidentifier_publicationdate";
	public static final String KEY_START_PART = "target_range_start_part";
	public static final String KEY_START_PATH_XPATH = "target_range_start_path_xpath";
	public static final String KEY_START_PATH_CHAROFFSET = "target_range_start_path_charoffset";
	public static final String KEY_END_PART = "target_range_end_part";
	public static final String KEY_END_PATH_XPATH = "target_range_end_path_xpath";
	public static final String KEY_END_PATH_CHAROFFSET = "target_range_end_path_charoffset";
	public static final String KEY_HIGHLIGHTCOLOR = "highlightcolor";
	public static final String KEY_UNDERLINED = "underlined";
	public static final String KEY_CROSSOUT = "crossout";
	public static final String KEY_CONTENT = "content";
	public static final String KEY_UPB_ID = "upb_id";
	public static final String KEY_EPUB_ID = "epub_id";
	public static final String KEY_ANNOTATION_ID = "annotation_id";
	private static final String DB_SEMAPPS_TABLE = "SemApps";
	private static final String DB_EPUBS_TABLE = "EPubs";
	private static final String DB_ANNOTATIONS_TABLE = "Annotations";
	private static final String DB_AUTHORS_TABLE = "Authors";
	private Context context;
	private SQLiteDatabase db;
	private AnnotationsDatabaseHelper dbHelper;
	
	public AnnotationsDbAdapter(Context context) {
		this.context = context;
	}
	
	public AnnotationsDbAdapter open() throws SQLException {
		dbHelper = new AnnotationsDatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		dbHelper.close();
	}
	
	/**
	 * Create a new SemApp. If the SemApp is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	public long createSemApp(String id, String name, String updated_at) {
		ContentValues values = createSemAppContentValues(id, name, updated_at);
		
		return db.insert(DB_SEMAPPS_TABLE, null, values);
	}
	
	/**
	 * Update the SemApp 
	 */
	public boolean updateSemApp(String id, String name, String updated_at) {
		ContentValues values = createSemAppContentValues(id, name, updated_at);
		
		return db.update(DB_SEMAPPS_TABLE, values, KEY_ROWID + "=\"" + id + "\"", null) > 0;
	}
	
	/**
	 * Deletes SemApp
	 */
	public boolean deleteSemApp(String id) {
		return db.delete(DB_SEMAPPS_TABLE, KEY_ROWID + "=\"" + id + "\"", null) > 0;
	}
	
	/**
	 * Create a new EPub. If the EPub is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	public long createEPub(String id, String name, String updated_at, 
			String file_name, String file_path, String semapp_id, String local_path) {
		ContentValues values = 
			createEPubContentValues(id, name, updated_at, file_name, file_path, semapp_id, local_path);
		
		return db.insert(DB_EPUBS_TABLE, null, values);
	}
	
	/**
	 * Update the EPub 
	 */
	public boolean updateEPub(String id, String name, String updated_at, 
			String file_name, String file_path, String semapp_id, String local_path) {
		ContentValues values = 
			createEPubContentValues(id, name, updated_at, file_name, file_path, semapp_id, local_path);
		
		return db.update(DB_EPUBS_TABLE, values, KEY_ROWID + "=\"" + id + "\"", null) > 0;
	}
	
	/**
	 * Deletes EPub
	 */
	public boolean deleteEPub(String id) {
		return db.delete(DB_EPUBS_TABLE, KEY_ROWID + "=\"" + id + "\"", null) > 0;
	}
	
	/**
	 * Create a new Annotation. If the Annotation is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	public long createAnnotation(String id, long created, long modified, 
			String category, String tags, String author_name, String bookid, String marked_text, 
			String target_annotation_id, String isbn, String title, String publicationdate, 
			String start_part, String start_xpath, long start_charoffset, String end_part, 
			String end_xpath, long end_charoffset, long highlightcolor, String underlined, 
			String crossout, String content, String upb_id, String updated_at, String epub_id) {
		
		ContentValues values = 
			createAnnotationContentValues(id, created, modified, category, tags, author_name, 
					bookid, marked_text, target_annotation_id, isbn, title, publicationdate, start_part, start_xpath, start_charoffset, 
					end_part, end_xpath, end_charoffset, highlightcolor, underlined, crossout, 
					content, upb_id, updated_at, epub_id);
		
		return db.insert(DB_ANNOTATIONS_TABLE, null, values);
	}
	
	/**
	 * Update the Annotation 
	 */
	public boolean updateAnnotation(String id, long created, long modified, 
			String category, String tags, String author_name, String bookid, String marked_text, 
			String target_annotation_id, String isbn, String title, String publicationdate, 
			String start_part, String start_xpath, long start_charoffset, String end_part, 
			String end_xpath, long end_charoffset, long highlightcolor, String underlined, 
			String crossout, String content, String upb_id, String updated_at, String epub_id) {
		
		ContentValues values = 
			createAnnotationContentValues(id, created, modified, category, tags, author_name, 
					bookid, marked_text, target_annotation_id, isbn, title, publicationdate, start_part, start_xpath, start_charoffset, 
					end_part, end_xpath, end_charoffset, highlightcolor, underlined, crossout, 
					content, upb_id, updated_at, epub_id);
		
		return db.update(DB_ANNOTATIONS_TABLE, values, KEY_ROWID + "=\"" + id + "\"", null) > 0;
	}
	
	/**
	 * Deletes Annotation
	 */
	public boolean deleteAnnotation(String id) {
		return db.delete(DB_ANNOTATIONS_TABLE, KEY_ROWID + "=\"" + id + "\"", null) > 0;
	}
	
	/**
	 * Create a new Author. If the Author is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	public long createAuthor(String name, String annotation_id) {
		ContentValues values = createAuthorContentValues(name, annotation_id);
		
		return db.insert(DB_AUTHORS_TABLE, null, values);
	}
	
	/**
	 * Update the Author 
	 */
	public boolean updateAuthor(String name, String annotation_id) {
		ContentValues values = createAuthorContentValues(name, annotation_id);
		
		return db.update(DB_AUTHORS_TABLE, values, KEY_ANNOTATION_ID + "=\"" + annotation_id + "\"", null) > 0;
	}
	
	/**
	 * Deletes Author
	 */
	public boolean deleteAuthor(String annotation_id) {
		return db.delete(DB_AUTHORS_TABLE, KEY_ANNOTATION_ID + "=\"" + annotation_id + "\"", null) > 0;
	}
	
	/**
	 * Return a Cursor over the list of all SemApps in the database
	 */
	public Cursor fetchAllSemApps() throws SQLException {
		return db.query(DB_SEMAPPS_TABLE, new String[] {KEY_ROWID, KEY_NAME, 
				KEY_UPDATED_AT}, null, null, null, null, null);
	}
	
	/**
	 * Return a Cursor positioned at the defined SemApp 
	 * @throws SQLException
	 */
	public Cursor fetchSemApp(String id) throws SQLException {
		final Cursor mCursor = db.query(true, DB_SEMAPPS_TABLE, new String[] {KEY_ROWID, 
				KEY_NAME, KEY_UPDATED_AT}, KEY_ROWID + "=\"" + id + "\"", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * Return a Cursor over the list of all EPubs in the database
	 */
	public Cursor fetchAllEPubs() throws SQLException {
		return db.query(DB_EPUBS_TABLE, new String[] {KEY_ROWID, 
				KEY_NAME, KEY_UPDATED_AT, KEY_FILENAME, KEY_FILEPATH, 
				KEY_SEMAPP_ID, KEY_LOCAL_PATH}, null, null, null, null, null);
	}
	
	/**
	 * Return a Cursor positioned at the defined EPub 
	 * @throws SQLException
	 */
	public Cursor fetchEPub(String id) throws SQLException {
		Cursor mCursor = db.query(true, DB_EPUBS_TABLE, new String[] {KEY_ROWID, 
				KEY_NAME, KEY_UPDATED_AT, KEY_FILENAME, KEY_FILEPATH, KEY_SEMAPP_ID, 
				KEY_LOCAL_PATH}, KEY_ROWID + "=\"" + id + "\"", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * Return a Cursor positioned at the defined EPub 
	 * @throws SQLException
	 */
	public Cursor fetchEPubByPath(String local_path) throws SQLException {
		Cursor mCursor = db.query(true, DB_EPUBS_TABLE, new String[] {KEY_ROWID, 
				KEY_NAME, KEY_UPDATED_AT, KEY_FILENAME, KEY_FILEPATH, KEY_SEMAPP_ID, 
				KEY_LOCAL_PATH}, KEY_LOCAL_PATH + "=\"" + local_path + "\"", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * Return a Cursor over the list of all Annotations in the database
	 */
	public Cursor fetchAllAnnotations() throws SQLException {
		return db.query(DB_ANNOTATIONS_TABLE, new String[] {KEY_ROWID, 
				KEY_CREATED, KEY_MODIFIED, KEY_CATEGORY, KEY_TAGS, KEY_AUTHOR_NAME, 
				KEY_BOOKID, KEY_MARKEDTEXT, KEY_TARGETANNOTATIONID, KEY_ISBN, KEY_TITLE, 
				KEY_PUBLICATIONDATE, KEY_START_PART, KEY_START_PATH_XPATH, 
				KEY_START_PATH_CHAROFFSET, KEY_END_PART, KEY_END_PATH_XPATH, 
				KEY_END_PATH_CHAROFFSET, KEY_HIGHLIGHTCOLOR, KEY_UNDERLINED, 
				KEY_CROSSOUT, KEY_CONTENT, KEY_UPB_ID, KEY_UPDATED_AT, 
				KEY_EPUB_ID}, null, null, null, null, null);
	}
	
	/**
	 * Return a Cursor positioned at the defined Annotation 
	 * @throws SQLException
	 */
	public Cursor fetchAnnotation(String id) throws SQLException {
		Cursor mCursor = db.query(true, DB_ANNOTATIONS_TABLE, new String[] {KEY_ROWID, 
				KEY_CREATED, KEY_MODIFIED, KEY_CATEGORY, KEY_TAGS, KEY_AUTHOR_NAME, 
				KEY_BOOKID, KEY_MARKEDTEXT, KEY_TARGETANNOTATIONID, KEY_ISBN, KEY_TITLE, 
				KEY_PUBLICATIONDATE, KEY_START_PART, KEY_START_PATH_XPATH, 
				KEY_START_PATH_CHAROFFSET, KEY_END_PART, KEY_END_PATH_XPATH, 
				KEY_END_PATH_CHAROFFSET, KEY_HIGHLIGHTCOLOR, KEY_UNDERLINED, 
				KEY_CROSSOUT, KEY_CONTENT, KEY_UPB_ID, KEY_UPDATED_AT, 
				KEY_EPUB_ID}, KEY_ROWID + "=\"" + id + "\"", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	
	
	/**
	 * Return a Cursor positioned at the defined Annotation 
	 * @throws SQLException
	 */
	public Cursor fetchAnnotationsByEPubId(String ePubId) throws SQLException {
		Cursor mCursor = db.query(true, DB_ANNOTATIONS_TABLE, new String[] {KEY_ROWID, 
				KEY_CREATED, KEY_MODIFIED, KEY_CATEGORY, KEY_TAGS, KEY_AUTHOR_NAME, 
				KEY_BOOKID, KEY_MARKEDTEXT, KEY_TARGETANNOTATIONID, KEY_ISBN, KEY_TITLE, 
				KEY_PUBLICATIONDATE, KEY_START_PART, KEY_START_PATH_XPATH, 
				KEY_START_PATH_CHAROFFSET, KEY_END_PART, KEY_END_PATH_XPATH, 
				KEY_END_PATH_CHAROFFSET, KEY_HIGHLIGHTCOLOR, KEY_UNDERLINED, 
				KEY_CROSSOUT, KEY_CONTENT, KEY_UPB_ID, KEY_UPDATED_AT, 
				KEY_EPUB_ID}, KEY_EPUB_ID + "=\"" + ePubId + "\"", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * Return a Cursor over the list of all Authors in the database
	 */
	public Cursor fetchAllAuthors() {
		return db.query(DB_AUTHORS_TABLE, new String[] {KEY_ROWID, KEY_NAME, 
				KEY_ANNOTATION_ID}, null, null, null, null, null);
	}
	
	/**
	 * Return a Cursor positioned at the defined Author 
	 * @throws SQLException
	 */
	public Cursor fetchAuthor(String annotation_id) throws SQLException {
		final Cursor mCursor = db.query(true, DB_AUTHORS_TABLE, new String[] {KEY_ROWID, 
				KEY_NAME, KEY_ANNOTATION_ID}, KEY_ANNOTATION_ID + "=\"" + annotation_id + "\"", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	private ContentValues createSemAppContentValues(String id, String name, String updated_at) {
		ContentValues values = new ContentValues();
		values.put(KEY_ROWID, id);
		values.put(KEY_NAME, name);
		values.put(KEY_UPDATED_AT, updated_at);
		return values;
	}
	
	private ContentValues createEPubContentValues(String id, String name, String updated_at, 
			String file_name, String file_path, String semapp_id, String local_path) {
		ContentValues values = new ContentValues();
		values.put(KEY_ROWID, id);
		values.put(KEY_NAME, name);
		values.put(KEY_UPDATED_AT, updated_at);
		values.put(KEY_FILENAME, file_name);
		values.put(KEY_FILEPATH, file_path);
		values.put(KEY_SEMAPP_ID, semapp_id);
		values.put(KEY_LOCAL_PATH, local_path);
		return values;
	}
	
	private ContentValues createAnnotationContentValues(String id, long created, long modified, 
			String category, String tags, String author_name, String bookid, String marked_text, 
			String target_annotation_id, String isbn, String title, String publicationdate, String start_part, 
			String start_xpath, long start_charoffset, String end_part, String end_xpath, long end_charoffset, 
			long highlightcolor, String underlined, String crossout, String content, String upb_id, 
			String updated_at, String epub_id) {
		ContentValues values = new ContentValues();
		values.put(KEY_ROWID, id);
		values.put(KEY_CREATED, created);
		values.put(KEY_MODIFIED, modified);
		values.put(KEY_CATEGORY, category);
		values.put(KEY_TAGS, tags);
		values.put(KEY_AUTHOR_NAME, author_name);
		values.put(KEY_BOOKID, bookid);
		values.put(KEY_MARKEDTEXT, marked_text);
		values.put(KEY_TARGETANNOTATIONID, target_annotation_id);
		values.put(KEY_ISBN, isbn);
		values.put(KEY_TITLE, title);
		values.put(KEY_PUBLICATIONDATE, publicationdate);
		values.put(KEY_START_PART, start_part);
		values.put(KEY_START_PATH_XPATH, start_xpath);
		values.put(KEY_START_PATH_CHAROFFSET, start_charoffset);
		values.put(KEY_END_PART, end_part);
		values.put(KEY_END_PATH_XPATH, end_xpath);
		values.put(KEY_END_PATH_CHAROFFSET, end_charoffset);
		values.put(KEY_HIGHLIGHTCOLOR, highlightcolor);
		values.put(KEY_UNDERLINED, underlined);
		values.put(KEY_CROSSOUT, crossout);
		values.put(KEY_CONTENT, content);
		values.put(KEY_UPB_ID, upb_id);
		values.put(KEY_UPDATED_AT, updated_at);
		values.put(KEY_EPUB_ID, epub_id);
		return values;
	}
	
	private ContentValues createAuthorContentValues(String name, String annotation_id) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_ANNOTATION_ID, annotation_id);
		return values;
	}
}
