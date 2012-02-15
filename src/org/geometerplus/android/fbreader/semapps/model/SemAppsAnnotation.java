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
	protected int id;
	@Element(required=false, name="user-id")
	protected int user_id;
	@Element(required=false, name="scenario-id")
	protected int scenario_id;
	@Element(required=false)
	protected String data;
	@Element(required=false, name="created-at")
	protected String created_at;
	@Element(required=false, name="updated-at")
	protected String updated_at;
	
	/**
	 * Standard empty constructor
	 */
	public SemAppsAnnotation() {
		id = -1;
		scenario_id = -1;
		data = "";
		created_at = "";
		updated_at = "";
    }
	
	public SemAppsAnnotation(Parcel in) {
		readFromParcel(in);
	}

	public int getId() {
		return id;
	}
	
	public int getUserId() {
		return user_id;
	}
	
	public int getScenarioId() {
		return scenario_id;
	}
	
	public String getData() {
		return data;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public String getCreated_at() {
		return created_at;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		id = in.readInt();
		user_id = in.readInt();
		scenario_id = in.readInt();
		data = in.readString();
		created_at = in.readString();
		updated_at = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(user_id);
		dest.writeInt(scenario_id);
		dest.writeString(data);
		dest.writeString(created_at);
		dest.writeString(updated_at);
		
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