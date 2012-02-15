package org.geometerplus.android.fbreader.semapps.model;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Root
public class SemApps implements Parcelable {
	
	@Attribute
	protected String type;
	@ElementList(required=false, inline=true, name="semapps", entry="semapp")
	protected ArrayList<SemApp> semApps;
	
	/**
	 * Standard empty constructor
	 */
	public SemApps() {
		type = "";
		semApps = new ArrayList<SemApp>();
    }
	
	public SemApps(Parcel in) {
		readFromParcel(in);
	}

	public ArrayList<SemApp> getSemApps() {
		if (semApps == null) {
			semApps = new ArrayList<SemApp>();
        }
		return this.semApps;
	}
	
	public SemApp getSemAppById(int semapp_id) {
		for (SemApp semApp : semApps) {
			if (semApp.getId() == semapp_id) {
				return semApp;
			}
		}
		return null;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		type = in.readString();
		if (semApps == null) {
			semApps = new ArrayList();
		}
		in.readTypedList(semApps, SemApp.CREATOR);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeTypedList(semApps);
	}
	
	public static final Parcelable.Creator<SemApps> CREATOR = new Parcelable.Creator<SemApps>() {
		public SemApps createFromParcel(Parcel in) {
			return new SemApps(in);
		}

		public SemApps[] newArray(int size) {
			return new SemApps[size];
		}
	};
}