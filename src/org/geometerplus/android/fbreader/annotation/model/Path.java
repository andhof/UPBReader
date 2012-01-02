package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

@Element
@Order(elements={"xpath", "charOffset"})
public class Path implements Parcelable {
	
	@Element(required=false)
	protected String xpath;
	@Element(required=false)
	protected int charOffset;
	
	public Path() {
		xpath = "";
		charOffset = 0;
	}
	
	public Path(String xpath, int charOffset) {
		this.xpath = xpath;
		this.charOffset = charOffset;
	}
	
	private Path(Parcel in) {
		readFromParcel(in);
	}
	
	public void setXPath(String newXPath) {
		xpath = newXPath;
	}
	
	public void setCharOffset(int newCharOffset) {
		charOffset = newCharOffset;
	}
	
	public String getXPath() {
		return xpath;
	}
	
	public int getCharOffset() {
		return charOffset;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void readFromParcel(Parcel in) {
		xpath = in.readString();
		charOffset = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(xpath);
		dest.writeInt(charOffset);
	}
	
	public static final Parcelable.Creator<Path> CREATOR = new Parcelable.Creator<Path>() {
		public Path createFromParcel(Parcel in) {
			return new Path(in);
		}

		public Path[] newArray(int size) {
			return new Path[size];
		}
	};
}