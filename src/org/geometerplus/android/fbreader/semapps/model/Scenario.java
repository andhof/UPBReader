package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Root
@Order(elements={"id", "semapp-id", "epub-id", "name", "version", "active", "created-at", "updated-at"})
public class Scenario implements Parcelable {

	@Element(required=false)
	protected int id;
	@Element(required=false, name="semapp-id")
	protected int semapp_id;
	@Element(required=false, name="epub-id")
	protected int epub_id;
	@Element(required=false)
	protected String name;
	@Element(required=false)
	protected int version;
	@Element(required=false)
	protected boolean active;
	@Element(required=false, name="created-at")
	protected String created_at;
	@Element(required=false, name="updated-at")
	protected String updated_at;

	protected SemAppsAnnotations annotations;
	
	/**
	 * Standard empty constructor
	 */
	public Scenario() {
		id = -1;
		semapp_id = -1;
		epub_id = -1;
		name = "";
		version = -1;
		active = false;
		created_at = "";
		updated_at = "";
		annotations = new SemAppsAnnotations();
    }
	
	public Scenario(Parcel in) {
		readFromParcel(in);
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setSemAppId(int semapp_id) {
		this.semapp_id = semapp_id;
	}
	
	public void setEPubId(int epub_id) {
		this.epub_id = epub_id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	
	public int getId() {
		return id;
	}
	
	public int getSemAppId() {
		return semapp_id;
	}
	
	public int getEPubId() {
		return epub_id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getVersion() {
		return version;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public String getCreated_at() {
		return created_at;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void readFromParcel(Parcel in) {
		id = in.readInt();
		semapp_id = in.readInt();
		epub_id = in.readInt();
		name = in.readString();
		version = in.readInt();
		active = in.readByte() == 1;
		created_at = in.readString();
		updated_at = in.readString();
		annotations = in.readParcelable(SemAppsAnnotations.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(semapp_id);
		dest.writeInt(epub_id);
		dest.writeString(name);
		dest.writeInt(version);
		dest.writeByte((byte) (active ? 1 : 0));
		dest.writeString(created_at);
		dest.writeString(updated_at);
		dest.writeParcelable(annotations, flags);
	}
	
	public static final Parcelable.Creator<Scenario> CREATOR = new Parcelable.Creator<Scenario>() {
		public Scenario createFromParcel(Parcel in) {
			return new Scenario(in);
		}

		public Scenario[] newArray(int size) {
			return new Scenario[size];
		}
	};
}