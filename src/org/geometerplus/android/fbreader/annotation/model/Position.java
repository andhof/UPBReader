package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Element
@Order(elements={"part", "path"})
public class Position implements Parcelable {
	
	@Element(required=false)
	protected String part;
	@Element(required=false)
	protected Path path;
	
	public Position() {
		part = "";
		path = new Path();
	}
	
	public Position(String part, Path path) {
		this.part = part;
		this.path = path;
	}
	
	private Position(Parcel in) {
		readFromParcel(in);
	}
	
	public void setPart(String newPart) {
		part = newPart;
	}
	
	public void setPath(Path newPath) {
		path = newPath;
	}
	
	public String getPart() {
		return part;
	}
	
	public Path getPath() {
		return path;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		part = in.readString();
		path = in.readParcelable(Path.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(part);
		dest.writeParcelable(path, flags);
	}
	
	public static final Parcelable.Creator<Position> CREATOR = new Parcelable.Creator<Position>() {
		public Position createFromParcel(Parcel in) {
			return new Position(in);
		}

		public Position[] newArray(int size) {
			return new Position[size];
		}
	};
}