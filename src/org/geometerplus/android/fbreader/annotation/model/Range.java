package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "range", propOrder = {
//	    "contentsignature",
//	    "start",
//	    "end"
//	})
@Element
@Order(elements={"start", "end"})
public class Range implements Parcelable {
	
	@Element(required=false)
	protected Position start;
	@Element(required=false)
	protected Position end;
	
	public Range(Position start, Position end) {
		this.start = start;
		this.end = end;
	}
	
	public Range() {
		this.start = new Position();
		this.end = new Position();
	}
	
	private Range(Parcel in) {
		readFromParcel(in);
	}
	
	public void setStart(Position newStart) {
		start = newStart;
	}
	
	public void setEnd(Position newEnd) {
		end = newEnd;
	}
	
	public Position getStart() {
		return start;
	}
	
	public Position getEnd() {
		return end;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		start = in.readParcelable(Position.class.getClassLoader());
		end = in.readParcelable(Position.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(start, flags);
		dest.writeParcelable(end, flags);
	}
	
	public static final Parcelable.Creator<Range> CREATOR = new Parcelable.Creator<Range>() {
		public Range createFromParcel(Parcel in) {
			return new Range(in);
		}

		public Range[] newArray(int size) {
			return new Range[size];
		}
	};
}
