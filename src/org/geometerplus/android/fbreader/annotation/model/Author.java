package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;

import android.os.Parcel;
import android.os.Parcelable;

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "author", propOrder = {
//	    "name",
//	    "identity"
//	})
@Element
public class Author implements Parcelable {
	
	@Element(required=false)
	protected String name;
	
	public Author(String name) {
		this.name = name;
	}
	
	public Author() {
		this.name = "";
	}
	
	private Author(Parcel in) {
		readFromParcel(in);
	}
	
	public void setName(String newName) {
		name = newName;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		name = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
	}
	
	public static final Parcelable.Creator<Author> CREATOR = new Parcelable.Creator<Author>() {
		public Author createFromParcel(Parcel in) {
			return new Author(in);
		}

		public Author[] newArray(int size) {
			return new Author[size];
		}
	};
}