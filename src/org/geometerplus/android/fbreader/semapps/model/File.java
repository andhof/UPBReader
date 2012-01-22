package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Default
@Order(elements={"name", "path"})
public class File implements Parcelable{
	
	@Element(required=false)
	protected String name;
	@Element(required=false)
	protected String path;
	
	/**
	 * Standard empty constructor
	 */
	public File() {
		name = "";
		path = "";
    }
	
	public File(Parcel in) {
		readFromParcel(in);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		name = in.readString();
		path = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(path);
	}
	
	public static final Parcelable.Creator<File> CREATOR = new Parcelable.Creator<File>() {
		public File createFromParcel(Parcel in) {
			return new File(in);
		}

		public File[] newArray(int size) {
			return new File[size];
		}
	};
}