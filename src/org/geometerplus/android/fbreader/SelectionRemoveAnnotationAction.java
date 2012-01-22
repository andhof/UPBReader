package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.annotation.database.AnnotationsDbAdapter;
import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.database.Cursor;
import android.net.Uri;

public class SelectionRemoveAnnotationAction extends FBAndroidAction {
	private FBReaderApp fbreader;
	private AnnotationsDbAdapter dbHelper;
	private Cursor cursor;
	
	SelectionRemoveAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		this.fbreader = fbreader;
		
	}

	@Override
	protected void run(Object... params) {
		final Annotation annotation = (Annotation) params[0];
		fbreader.Annotations.removeAnnotation(annotation);
		BaseActivity.hideAnnotationSelectionPanel();
		fbreader.BookTextView.removeAnnotationHighlight(annotation);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Uri uri = DBAnnotations.CONTENT_URI;
				String selection = DBAnnotations.ANNOTATION_ID + "=\"" + annotation.getId() + "\"";
				BaseActivity.getContentResolver().delete(uri, selection, null);
//				dbHelper.open();
//				dbHelper.deleteAnnotation(annotation.getId());
//				dbHelper.close();
			}
		}).start();
	}
}
