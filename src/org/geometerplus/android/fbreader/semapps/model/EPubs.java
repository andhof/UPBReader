package org.geometerplus.android.fbreader.semapps.model;

import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
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
	
	public EPub addEPub(
			String id,
			String name,
			String updated_at,
			String file_name, 
			String file_path) {

		EPub epub = new EPub();
		
		epub.setId(id);
		epub.setName(name);
		epub.setUpdated_at(updated_at);
		epub.getFile().setName(file_name);
		epub.getFile().setPath(file_path);
		
		epubs.add(epub);
		
		return epub;
	}

	public ArrayList<EPub> getEPubs() {
		return epubs;
	}
	
	public EPub getEPubById(String id) {
		for (EPub epub : epubs) {
			if (epub.getId().equals(id)) {
				return epub;
			}
		}
		return null;
	}
	
	public EPub getEPubByPath(String path) {
		for (EPub epub : epubs) {
			if (epub.getFile().getPath().equals(path)) {
				return epub;
			}
		}
		return null;
	}
	
	public void removeAllEPubs() {
		epubs.clear();
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