package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.semapps.UPBLibraryLoginActivity;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

class ShowUPBLibraryLoginAction extends RunActivityAction {
	ShowUPBLibraryLoginAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader, UPBLibraryLoginActivity.class);
	}
}