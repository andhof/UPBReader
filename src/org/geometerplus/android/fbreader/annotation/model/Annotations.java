package org.geometerplus.android.fbreader.annotation.model;
 
import java.util.ArrayList;

import java.util.List;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
 
/**
 * The set of Annotations
 */
@Root
public class Annotations {
	
	@ElementList(inline=true, name="annotation")
	protected ArrayList<Annotation> annotations;
	
	/**
	 * Standard empty constructor
	 */
	public Annotations(){
    }
	
	/**
	 * Add a new Annotation to the List of Annotations without any user created Information
	 */
	public Annotation addAnnotation() {
		Annotation annotation = new Annotation();
		this.getAnnotations().add(annotation);
		
		return annotation;
	}
	
	/**
	 * Add a new Annotation to the List of Annotations with all Information
	 * 
	 * @param id
	 * @param created
	 * @param modified
	 * @param category
	 * @param tags
	 * @param author
	 * @param target
	 * @param renderingInfo
	 * @param content
	 */
	public void addAnnotation(
			int id,
			long created, 
    		long modified, 
    		String category, 
    		ArrayList<String> tags, 
    		Author author, 
    		AnnotationTarget target, 
    		RenderingInfo renderingInfo, 
    		AnnotationContent content) {
		this.getAnnotations().add(new Annotation(id, created, modified, category, tags, author, target, renderingInfo, content));
	}
	
	/**
	 * get all annotations as a list
	 * 
	 * @return
	 */
	public ArrayList<Annotation> getAnnotations() {
        if (annotations == null) {
        	annotations = new ArrayList<Annotation>();
        }
        return this.annotations;
    }
	
	/**
	 * remove a specific annotation
	 * 
	 * @param annotation
	 */
	public void removeAnnotation(Annotation annotation) {
		ArrayList<Annotation> annotationList = getAnnotations();
		if (annotationList.contains(annotation)) {
			annotationList.remove(annotation);
		}
	}
}