package org.geometerplus.android.fbreader.annotation.database;

import org.geometerplus.android.fbreader.provider.AnnotationsContentProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public class DBAnnotation {

	public DBAnnotation() {
	}

	public static final class DBAnnotations implements BaseColumns {
		private DBAnnotations() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AnnotationsContentProvider.AUTHORITY + "/Annotations");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.upb.annotations";

		public static final String ANNOTATION_ID = "_id";

		public static final String CREATED = "created";
		
		public static final String MODIFIED = "modified";
		
		public static final String CATEGORY = "category";
		
		public static final String TAGS = "tags";
		
		public static final String AUTHOR_NAME = "author_name";
		
		public static final String BOOKID = "target_bookid";
		
		public static final String TARGET_ANNOTATION_ID = "target_annotationid";
		
		public static final String ISBN = "target_documentidentifier_isbn";
		
		public static final String TITLE = "target_documentidentifier_title";
		
		public static final String PUBLICATIONDATE = "target_documentidentifier_publicationdate";
		
		public static final String START_PART = "target_range_start_part";
		
		public static final String START_PATH_XPATH = "target_range_start_path_xpath";
		
		public static final String START_PATH_CHAROFFSET = "target_range_start_path_charoffset";
		
		public static final String END_PART = "target_range_end_part";
		
		public static final String END_PATH_XPATH = "target_range_end_path_xpath";
		
		public static final String END_PATH_CHAROFFSET = "target_range_end_path_charoffset";
		
		public static final String HIGHLIGHTCOLOR = "highlightcolor";
		
		public static final String UNDERLINED = "underlined";
		
		public static final String CROSSOUT = "crossout";
		
		public static final String CONTENT = "content";
		
		public static final String UPB_ID = "upb_id";
		
		public static final String UPDATED_AT = "updated_at";
	
		public static final String EPUB_ID = "epub_id";
		
		public static final String[] Projection = new String[] {
			ANNOTATION_ID, CREATED, MODIFIED, CATEGORY, TAGS, AUTHOR_NAME, BOOKID,
			TARGET_ANNOTATION_ID, ISBN, TITLE, PUBLICATIONDATE, START_PART, START_PATH_XPATH, 
			START_PATH_CHAROFFSET, END_PART, END_PATH_XPATH, END_PATH_CHAROFFSET, 
			HIGHLIGHTCOLOR, UNDERLINED, CROSSOUT, CONTENT, UPB_ID, UPDATED_AT, EPUB_ID};
	}

}

