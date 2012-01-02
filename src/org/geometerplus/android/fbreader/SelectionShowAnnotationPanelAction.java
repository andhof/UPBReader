package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

class SelectionShowAnnotationPanelAction extends FBAndroidAction {
	SelectionShowAnnotationPanelAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
    protected void run(Object ... params) {
		BaseActivity.showAnnotationSelectionPanel((Integer) params[0], (Integer) params[1], (Annotation) params[2]);
	}
}
