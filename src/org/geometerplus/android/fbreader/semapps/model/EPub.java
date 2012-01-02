package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Default
@Order(elements={"id", "name", "updated_at", "file", "annotations"})
public class EPub implements Parcelable {
	
	@Element(required=false)
	protected String id;
	@Element(required=false)
	protected String name;
	@Element(required=false)
	protected String updated_at;
	@Element(required=false)
	protected File file;
	@Element(required=false)
	protected Annotations annotations;
	
	/**
	 * Standard empty constructor
	 */
	public EPub() {
    }
	
	public EPub(Parcel in) {
		readFromParcel(in);
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public File getFile() {
		return file;
	}
	
	public Annotations getAnnotations() {
		return annotations;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void readFromParcel(Parcel in) {
		id = in.readString();
		name = in.readString();
		updated_at = in.readString();
		file = in.readParcelable(File.class.getClassLoader());
		annotations = in.readParcelable(Annotations.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(updated_at);
		dest.writeParcelable(file, flags);
		dest.writeParcelable(annotations, flags);
	}
	
	public static final Parcelable.Creator<EPub> CREATOR = new Parcelable.Creator<EPub>() {
		public EPub createFromParcel(Parcel in) {
			return new EPub(in);
		}

		public EPub[] newArray(int size) {
			return new EPub[size];
		}
	};
}