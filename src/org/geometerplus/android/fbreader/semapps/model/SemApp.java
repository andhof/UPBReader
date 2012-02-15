package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Element
@Order(elements={"id", "user-id", "name", "created-at", "updated-at"})
public class SemApp implements Parcelable{
	
	@Element(required=false)
	protected int id;
	@Element(required=false, name="user-id")
	protected int user_id;
	@Element(required=false)
	protected String name;
	@Element(required=false, name="created-at")
	protected String created_at;
	@Element(required=false, name="updated-at")
	protected String updated_at;
	
	protected EPubs epubs;
	
	/**
	 * Standard empty constructor
	 */
	public SemApp(){
		id = -1;
		user_id = -1;
		name = "";
		updated_at = "";
		epubs = new EPubs();
    }
	
	public SemApp(Parcel in) {
		readFromParcel(in);
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public EPubs getEPubs() {
		return epubs;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void readFromParcel(Parcel in) {
		id = in.readInt();
		user_id = in.readInt();
		name = in.readString();
		created_at = in.readString();
		updated_at = in.readString();
		epubs = in.readParcelable(EPubs.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(user_id);
		dest.writeString(name);
		dest.writeString(created_at);
		dest.writeString(updated_at);
		dest.writeParcelable(epubs, flags);
	}
	
	public static final Parcelable.Creator<SemApp> CREATOR = new Parcelable.Creator<SemApp>() {
		public SemApp createFromParcel(Parcel in) {
			return new SemApp(in);
		}

		public SemApp[] newArray(int size) {
			return new SemApp[size];
		}
	};
}