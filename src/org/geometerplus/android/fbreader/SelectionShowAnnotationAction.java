package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.annotation.SelectionNoteActivity;
import org.geometerplus.android.fbreader.annotation.SelectionShowNoteActivity;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.semapps.UPBLibraryLoginActivity;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

class SelectionShowAnnotationAction extends RunActivityAction {
	Annotation myAnnotation;
	
	SelectionShowAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader, SelectionShowNoteActivity.class);
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
	}
}