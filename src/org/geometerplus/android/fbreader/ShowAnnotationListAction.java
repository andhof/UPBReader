package org.geometerplus.android.fbreader;

import java.util.ArrayList;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication.ZLAction;

class ShowAnnotationListAction extends FBAndroidAction {
	ShowAnnotationListAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
    protected void run(Object ... params) {
		BaseActivity.showAnnotationListPanel((Integer) params[0], (Integer) params[1], (ArrayList<Annotation>) params[2]);
	}
}
