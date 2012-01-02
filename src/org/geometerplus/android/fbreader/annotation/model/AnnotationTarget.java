package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Element
@Order(elements={"bookId", "documentIdentifier", "range"})
public class AnnotationTarget implements Parcelable {
	
	@Element(required=false)
	protected long bookId;
	@Element(required=false)
	protected DocumentIdentifier documentIdentifier;
	@Element(required=false)
	protected Range range;
	
	public AnnotationTarget(long bookId, DocumentIdentifier documentidentifier, Range range) {
		this.bookId = bookId;
		this.documentIdentifier = documentidentifier;
		this.range = range;
	}
	
	public AnnotationTarget() {
		this.documentIdentifier = new DocumentIdentifier();
		this.range = new Range();
	}
	
	private AnnotationTarget(Parcel in) {
		readFromParcel(in);
	}
	
	public DocumentIdentifier getDocumentIdentifier() {
		return documentIdentifier;
	}
	
	public Range getRange() {
		return range;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		bookId = in.readLong();
		documentIdentifier = in.readParcelable(DocumentIdentifier.class.getClassLoader());
		range = in.readParcelable(Range.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(bookId);
		dest.writeParcelable(documentIdentifier, flags);
		dest.writeParcelable(range, flags);
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
