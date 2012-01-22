package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Element
@Order(elements={"bookId", "documentIdentifier", "range"})
public class AnnotationTarget implements Parcelable {
	
	@Element(required=false)
	protected String bookId;
	@Element(required=false)
	protected DocumentIdentifier documentIdentifier;
	@Element(required=false)
	protected Range range;
	protected String targetAnnotationId;
	
	public AnnotationTarget(String bookId, DocumentIdentifier documentidentifier, Range range) {
		this.bookId = bookId;
		this.documentIdentifier = documentidentifier;
		this.range = range;
	}
	
	public AnnotationTarget() {
		this.documentIdentifier = new DocumentIdentifier();
		this.range = new Range();
		this.targetAnnotationId = "";
	}
	
	private AnnotationTarget(Parcel in) {
		readFromParcel(in);
	}
	
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	
	public void setDocumentIdentifier(DocumentIdentifier documentIdentifier) {
		this.documentIdentifier = documentIdentifier;
	}
	
	public void setRange(Range range) {
		this.range = range;
	}
	
	public void setTargetAnnotationId(String annotation_id) {
		this.targetAnnotationId = annotation_id;
	}

	public String getBookId() {
		return bookId;
	}
	
	public DocumentIdentifier getDocumentIdentifier() {
		return documentIdentifier;
	}
	
	public Range getRange() {
		return range;
	}
	
	public String getTargetAnnotationId() {
		return targetAnnotationId;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		bookId = in.readString();
		documentIdentifier = in.readParcelable(DocumentIdentifier.class.getClassLoader());
		range = in.readParcelable(Range.class.getClassLoader());
		targetAnnotationId = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(bookId);
		dest.writeParcelable(documentIdentifier, flags);
		dest.writeParcelable(range, flags);
		dest.writeString(targetAnnotationId);
	}
	
	public static final Parcelable.Creator<AnnotationTarget> CREATOR = new Parcelable.Creator<AnnotationTarget>() {
		public AnnotationTarget createFromParcel(Parcel in) {
			return new AnnotationTarget(in);
		}

		public AnnotationTarget[] newArray(int size) {
			return new AnnotationTarget[size];
		}
	};
}
