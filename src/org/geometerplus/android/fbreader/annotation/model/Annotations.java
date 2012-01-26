package org.geometerplus.android.fbreader.annotation.model;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

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
		annotations = new ArrayList<Annotation>();
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
	 */
	public void addAnnotation(Annotation annotation) {
		annotations.add(annotation);
	}
	
	/**
	 * Add a new Annotation to the List of Annotations with all Information
	 */
	public void addAnnotation(
			String id,
			long created, 
    		long modified, 
    		String category, 
    		ArrayList<String> tags, 
    		Author author, 
    		AnnotationTarget target, 
    		RenderingInfo renderingInfo, 
    		AnnotationContent content,
    		String epub_id, 
    		String upb_id,
    		String updated_at) {
		annotations.add(new Annotation(id, created, modified, category, tags, author, target, renderingInfo, content, epub_id, upb_id, updated_at));
	}
	
	/**
	 * Add a new Annotation to the List of Annotations with all Information
	 */
	public void addAnnotation(
			String id,
			long created, 
    		long modified, 
    		String category, 
    		ArrayList<String> tags, 
    		String author_name, 
    		String bookid,
    		String targetannotationid,
    		String isbn,
    		String title,
    		ArrayList<String> authors,
    		String publicationdate,
    		String start_part,
    		String start_xpath,
    		int start_charoffset,
    		String end_part,
    		String end_xpath,
    		int end_charoffset,
    		int highlightcolor,
    		boolean underlined,
    		boolean crossout,
    		String content,
    		String epub_id,
    		String upb_id,
    		String updated_at) {
		
		Annotation annotation = new Annotation();
		
		annotation.setId(id);
		annotation.setCreated(created);
		annotation.setModified(modified);
		annotation.setCategory(category);
		annotation.setTags(tags);
		annotation.setCategory(category);
		annotation.setTags(tags);
		annotation.getAuthor().setName(author_name);
		annotation.getAnnotationTarget().setBookId(bookid);
		annotation.getAnnotationTarget().setTargetAnnotationId(targetannotationid);
		annotation.getAnnotationTarget().getDocumentIdentifier().setISBN(isbn);
		annotation.getAnnotationTarget().getDocumentIdentifier().setTitle(title);
		annotation.getAnnotationTarget().getDocumentIdentifier().setAuthors(authors);
		annotation.getAnnotationTarget().getDocumentIdentifier().setPublicationDate(publicationdate);
		annotation.getAnnotationTarget().getRange().getStart().setPart(start_part);
		annotation.getAnnotationTarget().getRange().getStart().getPath().setXPath(start_xpath);
		annotation.getAnnotationTarget().getRange().getStart().getPath().setCharOffset(start_charoffset);
		annotation.getAnnotationTarget().getRange().getEnd().setPart(end_part);
		annotation.getAnnotationTarget().getRange().getEnd().getPath().setXPath(end_xpath);
		annotation.getAnnotationTarget().getRange().getEnd().getPath().setCharOffset(end_charoffset);
		annotation.getRenderingInfo().setHighlightColor(highlightcolor);
		annotation.getRenderingInfo().setUnderlined(underlined);
		annotation.getRenderingInfo().setCrossOut(crossout);
		annotation.getAnnotationContent().setAnnotationText(content);
		annotation.setEPubId(epub_id);
		annotation.setUPBId(upb_id);
		annotation.setUpdatedAt(updated_at);
		
		annotations.add(annotation);
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
	
//	public Annotation getAnnotationById() {
//		
//	}
	
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
	
	/**
	 * remove all annotations
	 */
	public void removeAllAnnotations() {
		annotations.clear();
	}
}