package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.annotation.SelectionHighlightActivity;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

class SelectionHighlightAction extends RunActivityAction {
	SelectionHighlightAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader, SelectionHighlightActivity.class);
	}
	
	@Override
	protected void run(Object ... params) {
		if (params.length > 0) {
			BaseActivity.startActivity(
					new Intent(BaseActivity.getApplicationContext(), SelectionHighlightActivity.class)
						.putExtra("annotation", (Annotation) params[0])
			);
		} else {
			BaseActivity.startActivity(new Intent(BaseActivity.getApplicationContext(), SelectionHighlightActivity.class)	);
		}
	}
}