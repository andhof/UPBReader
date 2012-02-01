package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Element
public class SemAppsAnnotation implements Parcelable{
	
	@Element(required=false, name="id")
	protected String id;
	@Element(required=false, name="updated_at")
	protected String updated_at;
	@Element(required=false, name="created_at")
	protected String created_at;
	@Element(required=false, name="user_id")
	protected String user_id;
	@Element(required=false)
	protected String data;
	@Attribute(required=false)
    protected String type;
	
	/**
	 * Standard empty constructor
	 */
	public SemAppsAnnotation() {
		id = "";
		updated_at = "";
		created_at = "";
		user_id = "";
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
	
	public String getCreated_at() {
		return created_at;
	}
	
	public String getUserID() {
		return user_id;
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
		created_at = in.readString();
		user_id = in.readString();
		data = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(updated_at);
		dest.writeString(created_at);
		dest.writeString(user_id);
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