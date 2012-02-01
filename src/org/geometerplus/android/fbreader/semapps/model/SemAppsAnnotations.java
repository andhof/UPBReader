package org.geometerplus.android.fbreader.semapps.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Root
public class SemAppsAnnotations implements Parcelable{
	
	@ElementList(required=false, inline=true, name="annotations", entry="annotation")
	protected ArrayList<SemAppsAnnotation> annotations;
	@Attribute(required=false)
    protected String type;
	
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
	
	public SemAppsAnnotation getAnnotationByData(String data) {
		ArrayList<SemAppsAnnotation> saAnnotations = new ArrayList<SemAppsAnnotation>();
		for (SemAppsAnnotation a : annotations) {
			if (a.getData().equals(data)) {
				return a;
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