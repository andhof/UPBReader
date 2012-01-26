package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Element
@Order(elements={"id", "name", "updated_at"})
public class SemAppDummy implements Parcelable {
	
	@Element(required=false)
	protected String id;
	@Element(required=false)
	protected String name;
	@Element(required=false)
	protected String updated_at;
	
	/**
	 * Standard empty constructor
	 */
	public SemAppDummy() {
		id = "";
		name = "";
		updated_at = "";
    }
	
	public SemAppDummy(Parcel in) {
		readFromParcel(in);
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUpdatedAt(String updated_at) {
		this.updated_at = updated_at;
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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		id = in.readString();
		name = in.readString();
		updated_at = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(updated_at);
	}
	
	public static final Parcelable.Creator<SemAppDummy> CREATOR = new Parcelable.Creator<SemAppDummy>() {
		public SemAppDummy createFromParcel(Parcel in) {
			return new SemAppDummy(in);
		}

		public SemAppDummy[] newArray(int size) {
			return new SemAppDummy[size];
		}
	};
}