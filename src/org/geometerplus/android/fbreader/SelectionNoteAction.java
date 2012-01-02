package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.annotation.SelectionNoteActivity;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

class SelectionNoteAction extends RunActivityAction {
	protected Annotation myAnnotation;
	
	SelectionNoteAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader, SelectionNoteActivity.class);
	}
	
	@Override
	protected void run(Object ... params) {
		if (params.length > 0) {
			myAnnotation = (Annotation) params[0];
			BaseActivity.startActivityForResult(
					new Intent(BaseActivity.getApplicationContext(), SelectionNoteActivity.class)
						.putExtra("annotation", myAnnotation), FBReader.ANNOTATION_CODE
			);
		} else {
			BaseActivity.startActivity(new Intent(BaseActivity.getApplicationContext(), SelectionNoteActivity.class));
		}
	}
}