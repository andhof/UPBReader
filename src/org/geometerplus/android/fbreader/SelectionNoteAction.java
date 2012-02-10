package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.annotation.SelectionNoteActivity;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import de.upb.android.reader.R;

import android.content.Intent;
import android.content.SharedPreferences;

class SelectionNoteAction extends RunActivityAction {
	protected Annotation myAnnotation;
	private FBReaderApp fbreader;
	
	SelectionNoteAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader, SelectionNoteActivity.class);
		this.fbreader = fbreader;
	}
	
	@Override
	protected void run(Object ... params) {
		if (params.length > 0) {
			myAnnotation = (Annotation) params[0];
			// Only delete if the user has the rights to do it
			SharedPreferences settings = BaseActivity.getSharedPreferences("upblogin", 0);
			String username = settings.getString("user", "Localuser");
			if (!myAnnotation.getAuthor().getName().equals(username)) {
				UIUtil.createDialog(BaseActivity, "Error", BaseActivity.getString(R.string.not_allowed));
				return;
			}
			BaseActivity.startActivityForResult(
					new Intent(BaseActivity.getApplicationContext(), SelectionNoteActivity.class)
						.putExtra("annotation", myAnnotation), FBReader.ANNOTATION_CODE
			);
		} else {
			BaseActivity.startActivity(new Intent(BaseActivity.getApplicationContext(), SelectionNoteActivity.class));
		}
		fbreader.BookTextView.clearSelectionHighlight();
		fbreader.BookTextView.repaintAll();
	}
}