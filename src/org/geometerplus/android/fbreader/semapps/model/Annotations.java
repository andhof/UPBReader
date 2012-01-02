package org.geometerplus.android.fbreader.semapps.model;

import java.util.ArrayList;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Default
public class Annotations implements Parcelable{
	
	@ElementList(inline=true, name="annotations")
	protected ArrayList<Annotation> annotations;
	
	/**
	 * Standard empty constructor
	 */
	public Annotations() {
    }
	
	public Annotations(Parcel in) {
		readFromParcel(in);
	}

	public ArrayList<Annotation> getAnnotations() {
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
		in.readTypedList(annotations, Annotation.CREATOR);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(annotations);
	}
	
	public static final Parcelable.Creator<Annotations> CREATOR = new Parcelable.Creator<Annotations>() {
		public Annotations createFromParcel(Parcel in) {
			return new Annotations(in);
		}

		public Annotations[] newArray(int size) {
			return new Annotations[size];
		}
	};
}