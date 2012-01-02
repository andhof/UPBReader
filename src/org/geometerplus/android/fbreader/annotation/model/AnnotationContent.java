package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;

import android.os.Parcel;
import android.os.Parcelable;

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "annotationcontent", propOrder = {
//	    "annotationtext",
//	    "contentrange"
//	})
@Element
public class AnnotationContent implements Parcelable {
	
	@Element(required=false)
	protected String annotationText;
	
	public AnnotationContent(String annotationText) {
		this.annotationText = annotationText;
	}
	
	public AnnotationContent() {
		this.annotationText = "";
	}
	
	private AnnotationContent(Parcel in) {
		readFromParcel(in);
	}
	
	public void setAnnotationText(String newText) {
		annotationText = newText;
	}
	
	public String getAnnotationText() {
		return annotationText;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		annotationText = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(annotationText);
	}
	
	public static final Parcelable.Creator<AnnotationContent> CREATOR = new Parcelable.Creator<AnnotationContent>() {
		public AnnotationContent createFromParcel(Parcel in) {
			return new AnnotationContent(in);
		}

		public AnnotationContent[] newArray(int size) {
			return new AnnotationContent[size];
		}
	};
}