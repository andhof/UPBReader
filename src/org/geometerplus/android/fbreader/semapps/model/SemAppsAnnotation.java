package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Element
@Order(elements={"id", "updated_at", "data"})
public class SemAppsAnnotation implements Parcelable{
	
	@Element(required=false)
	protected String id;
	@Element(required=false)
	protected String updated_at;
	@Element(required=false)
	protected String data;
	
	/**
	 * Standard empty constructor
	 */
	public SemAppsAnnotation() {
		id = "";
		updated_at = "";
		data = "";
    }
	
	public SemAppsAnnotation(Parcel in) {
		readFromParcel(in);
	}

	public String getId() {
		return id;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public String getData() {
		return data;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		id = in.readString();
		updated_at = in.readString();
		data = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(updated_at);
		dest.writeString(data);
	}
	
	public static final Parcelable.Creator<SemAppsAnnotation> CREATOR = new Parcelable.Creator<SemAppsAnnotation>() {
		public SemAppsAnnotation createFromParcel(Parcel in) {
			return new SemAppsAnnotation(in);
		}

		public SemAppsAnnotation[] newArray(int size) {
			return new SemAppsAnnotation[size];
		}
	};
}