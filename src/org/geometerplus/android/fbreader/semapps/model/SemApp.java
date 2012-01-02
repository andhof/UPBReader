package org.geometerplus.android.fbreader.semapps.model;

import java.util.ArrayList;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Root
@Order(elements={"id", "name", "updated_at", "epubs"})
public class SemApp implements Parcelable{
	
	@Element(required=false)
	protected String id;
	@Element(required=false)
	protected String name;
	@Element(required=false)
	protected String updated_at;
	@Element(required=false)
	protected EPubs epubs;
	
	/**
	 * Standard empty constructor
	 */
	public SemApp(){
    }
	
	public SemApp(Parcel in) {
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
	
	public EPubs getEPubs() {
		return epubs;
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
		epubs = in.readParcelable(EPubs.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
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