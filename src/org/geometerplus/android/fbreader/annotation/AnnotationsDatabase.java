package org.geometerplus.android.fbreader.annotation;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.annotation.model.AnnotationContent;
import org.geometerplus.android.fbreader.annotation.model.AnnotationTarget;
import org.geometerplus.android.fbreader.annotation.model.Author;
import org.geometerplus.android.fbreader.annotation.model.RenderingInfo;

public abstract class AnnotationsDatabase {
	private static AnnotationsDatabase ourInstance;
	
	public static AnnotationsDatabase Instance() {
		return ourInstance;
	}
	
	protected AnnotationsDatabase() {
		AnnotationsDatabase ourInstance = this;
	}
	
	protected Annotation createAnnotation(
			long bookid,
			long id, 
			Author author, 
			String created, 
			String modified, 
			String category, 
			AnnotationTarget target, 
			String tags, 
			RenderingInfo renderinginfo, 
			AnnotationContent content) {

		return new Annotation();
	}
	
	protected abstract void executeAsATransaction(Runnable actions);
	
	protected abstract void updateAnnotationInfo(
			long bookid,
			long id, 
			Author author, 
			String created, 
			String modified, 
			String category, 
			AnnotationTarget target, 
			String tags, 
			RenderingInfo renderinginfo, 
			AnnotationContent content);
	
}