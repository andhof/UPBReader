package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;

import android.os.Parcel;
import android.os.Parcelable;

@Element
public class TargetAuthor implements Parcelable {
	
	@Element(required=false)
	protected String name;
	
	public TargetAuthor(String name) {
		this.name = name;
	}
	
	public TargetAuthor() {
		this.name = "";
	}
	
	private TargetAuthor(Parcel in) {
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
	
	public static final Parcelable.Creator<TargetAuthor> CREATOR = new Parcelable.Creator<TargetAuthor>() {
		public TargetAuthor createFromParcel(Parcel in) {
			return new TargetAuthor(in);
		}

		public TargetAuthor[] newArray(int size) {
			return new TargetAuthor[size];
		}
	};
}