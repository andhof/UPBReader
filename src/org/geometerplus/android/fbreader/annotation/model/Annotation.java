package org.geometerplus.android.fbreader.annotation.model;
 
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;
 
 
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "annotation", propOrder = {
//    "author",
//    "created",
//    "modified",
//    "category",
//    "tags",
//    "annotationtargets",
//    "annotationcontent"
//})
/**
 * Annotation Object
 */
@Root
@Order(elements={"id", "created", "modified", "category", "tags", "author", "annotationTarget", "renderingInfo", "annotationContent"})
public class Annotation implements Parcelable {
 
	@Element(required=false)
	protected String id;
	@Element(required=false)
    protected long created;
	@Element(required=false)
    protected long modified;
	@Element(required=false)
    protected String category;
	@ElementList(required=false, name="tags", entry="tag")
    protected ArrayList<String> tags;
    @Element(required=false)
    protected Author author;
    @Element(required=false)
    protected AnnotationTarget annotationTarget;
    @Element(required=false)
    protected RenderingInfo renderingInfo;
    @Element(required=false)
    protected AnnotationContent annotationContent;
    
    protected String upb_id;
    protected String updated_at;
    
    /**
     * Full constructor with all Information
     */
    public Annotation(
    		String id,
    		long created, 
    		long modified, 
    		String category, 
    		ArrayList<String> tags, 
    		Author author, 
    		AnnotationTarget target, 
    		RenderingInfo renderingInfo, 
    		AnnotationContent annotationContent,
    		String upb_id,
    		String updated_at) {
    	this.id = id;
        this.created = created;
        this.modified = modified;
        this.category = category;
        this.tags = tags;
        this.author = author;
        this.annotationTarget = target;
        this.renderingInfo = renderingInfo;
        this.annotationContent = annotationContent;
        this.upb_id = upb_id;
        this.updated_at = updated_at;
        
//        author = new Author();
//        annotationtarget = new AnnotationTarget();
//        annotationcontent = new AnnotationContent();
//        renderingInfo = new RenderingInfo();
    }
    
    public Annotation(){
    	id = "";
    	created = 0;
    	modified = 0;
    	category = "";
    	author = new Author();
    	tags = new ArrayList<String>();
    	annotationTarget = new AnnotationTarget();
        renderingInfo = new RenderingInfo();
        annotationContent = new AnnotationContent();
        upb_id = "";
        updated_at = "";
    }
    
    /**
     *	Constructor for Parcel Object
     * 
     * @param in
     */
    private Annotation(Parcel in) {
    	readFromParcel(in);
    }
    
    public void setId(String id) {
    	this.id = id;
    }
    
    public void setCreated(long created) {
    	this.created = created;
    }

    public void setModified(long modified) {
    	this.modified = modified;
    }

    public void setCategory(String category) {
    	this.category = category;
    }
    
    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
    
    public void setAuthor(Author author) {
    	this.author = author;
    }
    
    public void setAnnotationTarget(AnnotationTarget annotationTarget) {
    	this.annotationTarget = annotationTarget;
    }
    
    public void setAnnotationContent(AnnotationContent annotationContent) {
    	this.annotationContent = annotationContent;
    }
    
    public void setRenderingInfo(RenderingInfo renderingInfo) {
    	this.renderingInfo = renderingInfo;
    }
    
    public void setUPBId(String upb_id) {
    	this.upb_id = upb_id;
    }
    
    public void setUpdatedAt(String updated_at) {
    	this.updated_at = updated_at;
    }
    
    public String getId() {
    	return id;
    }
    
    public long getCreated() {
    	return created;
    }
    
    public long getModified() {
    	return modified;
    }
    
    public String getCategory() {
    	return category;
    }
    
    public Author getAuthor() {
    	return author;
    }
    
    public List<String> getTags() {
        if (tags == null) {
        	tags = new ArrayList<String>();
        }
        return this.tags;
    }
    
    public AnnotationTarget getAnnotationTarget() {
    	return annotationTarget;
    }
    
    public AnnotationContent getAnnotationContent() {
    	return annotationContent;
    }
    
    public RenderingInfo getRenderingInfo() {
    	return renderingInfo;
    }
    
    public String getUPBId() {
    	return upb_id;
    }
    
    public String getUpdatedAt() {
    	return updated_at;
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		id = in.readString();
		created = in.readLong();
		modified = in.readLong();
		category = in.readString();
		if (tags == null) {
			tags = new ArrayList();
		}
		in.readStringList(tags);
		author = in.readParcelable(Author.class.getClassLoader());
		annotationTarget = in.readParcelable(AnnotationTarget.class.getClassLoader());
		renderingInfo = in.readParcelable(RenderingInfo.class.getClassLoader());
		annotationContent = in.readParcelable(AnnotationContent.class.getClassLoader());
		upb_id = in.readString();
		updated_at = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeLong(created);
		dest.writeLong(modified);
		dest.writeString(category);
		dest.writeStringList(tags);
		dest.writeParcelable(author, flags);
		dest.writeParcelable(annotationTarget, flags);
		dest.writeParcelable(renderingInfo, flags);
		dest.writeParcelable(annotationContent, flags);
		dest.writeString(upb_id);
		dest.writeString(updated_at);
	}
	
	public static final Parcelable.Creator<Annotation> CREATOR = new Parcelable.Creator<Annotation>() {
		public Annotation createFromParcel(Parcel in) {
			return new Annotation(in);
		}

		public Annotation[] newArray(int size) {
			return new Annotation[size];
		}
	};
}