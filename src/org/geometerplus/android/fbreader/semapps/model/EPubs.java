package org.geometerplus.android.fbreader.semapps.model;

import java.util.ArrayList;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Element
public class EPubs implements Parcelable{
	
	@ElementList(inline=true, entry="epub")
	protected ArrayList<EPub> epubs;
	
	/**
	 * Standard empty constructor
	 */
	public EPubs(){
		epubs = new ArrayList<EPub>();
    }
	
	public EPubs(Parcel in) {
		readFromParcel(in);
	}

	public ArrayList<EPub> getEPubs() {
		return epubs;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		if (epubs == null) {
			epubs = new ArrayList();
		}
		in.readTypedList(epubs, EPub.CREATOR);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(epubs);
	}
	
	public static final Parcelable.Creator<EPubs> CREATOR = new Parcelable.Creator<EPubs>() {
		public EPubs createFromParcel(Parcel in) {
			return new EPubs(in);
		}

		public EPubs[] newArray(int size) {
			return new EPubs[size];
		}
	};
}