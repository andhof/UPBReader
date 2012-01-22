package org.geometerplus.android.fbreader.semapps.model;

import java.util.ArrayList;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.ElementList;
import android.os.Parcel;
import android.os.Parcelable;

@Default
public class SemAppsAnnotations implements Parcelable{
	
	@ElementList(inline=true, name="annotations", entry="annotation")
	protected ArrayList<SemAppsAnnotation> annotations;
	
	/**
	 * Standard empty constructor
	 */
	public SemAppsAnnotations() {
		annotations = new ArrayList<SemAppsAnnotation>();
    }
	
	public SemAppsAnnotations(Parcel in) {
		readFromParcel(in);
	}

	public ArrayList<SemAppsAnnotation> getAnnotations() {
		return annotations;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void readFromParcel(Parcel in) {
		if (annotations == null) {
			annotations = new ArrayList();
		}
		in.readTypedList(annotations, SemAppsAnnotation.CREATOR);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(annotations);
	}
	
	public static final Parcelable.Creator<SemAppsAnnotations> CREATOR = new Parcelable.Creator<SemAppsAnnotations>() {
		public SemAppsAnnotations createFromParcel(Parcel in) {
			return new SemAppsAnnotations(in);
		}

		public SemAppsAnnotations[] newArray(int size) {
			return new SemAppsAnnotations[size];
		}
	};
}