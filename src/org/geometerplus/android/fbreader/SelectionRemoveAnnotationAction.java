package org.geometerplus.android.fbreader;

import java.util.ArrayList;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.annotation.model.Annotations;
import org.geometerplus.android.fbreader.network.action.RefreshRootCatalogAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class SelectionRemoveAnnotationAction extends FBAndroidAction {
	private FBReaderApp fbreader;
	
	SelectionRemoveAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		this.fbreader = fbreader;
	}

	@Override
	protected void run(Object... params) {
		Annotation annotation = (Annotation) params[0];
		fbreader.Annotations.removeAnnotation(annotation);
		BaseActivity.hideAnnotationSelectionPanel();
		fbreader.BookTextView.removeAnnotationHighlight(annotation);
	}
}
