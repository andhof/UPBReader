package org.geometerplus.android.fbreader.annotation.database;

import org.geometerplus.android.fbreader.provider.EPubsContentProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public class DBEPub {

	public DBEPub() {
	}

	public static final class DBEPubs implements BaseColumns {
		private DBEPubs() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ EPubsContentProvider.AUTHORITY + "/EPubs");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.upb.epubs";

		public static final String EPUB_ID = "_id";

		public static final String NAME = "name";
		
		public static final String UPDATED_AT = "updated_at";
		
		public static final String FILENAME = "file_name";
		
		public static final String FILEPATH = "file_path";
		
		public static final String LOCALPATH = "local_path";
		
		public static final String SEMAPP_ID = "semapp_id";
		
		public static final String[] Projection = new String[] {
			EPUB_ID, NAME, UPDATED_AT, FILENAME,FILEPATH, LOCALPATH, SEMAPP_ID};
	}

}

