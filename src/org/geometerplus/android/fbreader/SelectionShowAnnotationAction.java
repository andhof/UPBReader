package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.annotation.SelectionNoteActivity;
import org.geometerplus.android.fbreader.annotation.SelectionShowNoteActivity;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.semapps.UPBLibraryLoginActivity;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

class SelectionShowAnnotationAction extends RunActivityAction {
	private Annotation myAnnotation;
	private FBReaderApp fbreader;
	
	SelectionShowAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader, SelectionShowNoteActivity.class);
		this.fbreader = fbreader;
	}
	
	@Override
	protected void run(Object ... params) {
		if (params.length > 0) {
			myAnnotation = (Annotation) params[0];
			BaseActivity.startActivityForResult(
					new Intent(BaseActivity.getApplicationContext(), SelectionShowNoteActivity.class)
						.putExtra("annotation", myAnnotation), FBReader.ANNOTATION_CODE
			);
		} else {
			BaseActivity.startActivity(new Intent(BaseActivity.getApplicationContext(), SelectionShowNoteActivity.class));
		}
		fbreader.BookTextView.clearSelectionHighlight();
		fbreader.BookTextView.repaintAll();
	}
}