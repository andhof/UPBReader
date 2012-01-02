package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.semapps.UPBLibraryLoginActivity;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication.ZLAction;

class SelectionShowAnnotationAction extends RunActivityAction {
	SelectionShowAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		/** 
		 * @todo
		 */
		super(baseActivity, fbreader, UPBLibraryLoginActivity.class);
	}
}