package org.geometerplus.android.fbreader.annotation.database;

import org.geometerplus.android.fbreader.provider.EPubsContentProvider;
import org.geometerplus.android.fbreader.provider.ScenariosContentProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public class DBScenario {

	public DBScenario() {
	}

	public static final class DBScenarios implements BaseColumns {
		private DBScenarios() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ ScenariosContentProvider.AUTHORITY + "/Scenarios");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.upb.scenarios";

		public static final String SCENARIO_ID = "_id";
		
		public static final String SEMAPP_ID = "semapp_id";
		
		public static final String EPUB_ID = "epub_id";
		
		public static final String NAME = "name";
		
		public static final String VERSION = "version";
		
		public static final String ACTIVE = "active";
		
		public static final String CREATED_AT = "created_at";
		
		public static final String UPDATED_AT = "updated_at";
		
		public static final String[] Projection = new String[] {
			SCENARIO_ID, SEMAPP_ID, EPUB_ID, NAME, VERSION, ACTIVE, CREATED_AT, UPDATED_AT};
	}

}

