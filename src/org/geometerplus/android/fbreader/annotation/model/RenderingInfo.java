package org.geometerplus.android.fbreader.annotation.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;

import android.os.Parcel;
import android.os.Parcelable;

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "renderinginfo", propOrder = {
//	    "highlightcolor",
//	    "underlined"
//	})
@Element
@Order(elements={"highlightcolor", "underlined", "crossout"})
public class RenderingInfo implements Parcelable {
	
	@Element(required=false)
	protected int highlightcolor;
	@Element(required=false)
	protected boolean underlined;
	@Element(required=false)
	protected boolean crossout;
	
	public RenderingInfo(int highlightColor, boolean underlined, boolean crossout) {
		this.highlightcolor = highlightColor;
		this.underlined = underlined;
		this.crossout = crossout;
	}
	
	public RenderingInfo() {
		this.highlightcolor = 0;
		this.underlined = false;
		this.crossout = false;
	}
	
	private RenderingInfo(Parcel in) {
		readFromParcel(in);
	}
	
	public void setHighlightColor(int newColor) {
		highlightcolor = newColor;
	}
	
	public void setUnderlined(boolean underlined) {
		this.underlined = underlined;
	}
	
	public void setCrossOut(boolean crossout) {
		this.crossout = crossout;
	}
	
	public int getHighlightColor() {
		return highlightcolor;
	}
	
	public boolean isUnderlined() {
		return underlined;
	}
	
	public boolean isCrossedOut() {
		return crossout;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		highlightcolor = in.readInt();
		underlined = in.readByte() == 1;
		crossout = in.readByte() == 1;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(highlightcolor);
		dest.writeByte((byte) (underlined ? 1 : 0));
		dest.writeByte((byte) (crossout ? 1 : 0));
	}
	
	public static final Parcelable.Creator<RenderingInfo> CREATOR = new Parcelable.Creator<RenderingInfo>() {
		public RenderingInfo createFromParcel(Parcel in) {
			return new RenderingInfo(in);
		}

		public RenderingInfo[] newArray(int size) {
			return new RenderingInfo[size];
		}
	};
}
