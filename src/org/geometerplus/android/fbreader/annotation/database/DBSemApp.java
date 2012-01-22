package org.geometerplus.android.fbreader.annotation.database;

import org.geometerplus.android.fbreader.provider.SemAppsContentProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public class DBSemApp {

	public DBSemApp() {
	}

	public static final class DBSemApps implements BaseColumns {
		private DBSemApps() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ SemAppsContentProvider.AUTHORITY + "/SemApps");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.upb.semapps";

		public static final String SEMAPP_ID = "_id";

		public static final String NAME = "name";

		public static final String UPDATED_AT = "updated_at";
		
		public static final String[] Projection = new String[] {
			SEMAPP_ID, NAME, UPDATED_AT};
	}

}

