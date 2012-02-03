package org.geometerplus.android.fbreader.annotation.database;

import org.geometerplus.android.fbreader.provider.AuthorsContentProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public class DBAuthor {

	public DBAuthor() {
	}

	public static final class DBAuthors implements BaseColumns {
		private DBAuthors() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AuthorsContentProvider.AUTHORITY + "/Authors");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.upb.authors";

		public static final String AUTHOR_ID = "_id";

		public static final String NAME = "name";

		public static final String EPUB_ID = "epub_id";
		
		public static final String[] Projection = new String[] {
			AUTHOR_ID, NAME, EPUB_ID};
	}

}

