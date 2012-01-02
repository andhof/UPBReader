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
	@ElementList(inline=true, entry="semapp")
	protected ArrayList<SemAppDummy> semApps;
	
	/**
	 * Standard empty constructor
	 */
	public SemApps() {
    }
	
	public SemApps(Parcel in) {
		readFromParcel(in);
	}

	public ArrayList<SemAppDummy> getSemApps() {
		if (semApps == null) {
			semApps = new ArrayList<SemAppDummy>();
        }
		return this.semApps;
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
		in.readTypedList(semApps, SemAppDummy.CREATOR);
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