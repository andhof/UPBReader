package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Root
@Order(elements={"id", "name", "updated_at", "file", "annotations"})
public class EPub implements Parcelable {
	
	@Element(required=false)
	protected String id;
	@Element(required=false)
	protected String name;
	@Element(required=false)
	protected String updated_at;
	@Element(required=false)
	protected File file;
	@Element(required=false)
	protected SemAppsAnnotations annotations;
	
	protected String local_path;
	protected String semapp_id;
	
	/**
	 * Standard empty constructor
	 */
	public EPub() {
		id = "";
		name = "";
		updated_at = "";
		file = new File();
		annotations = new SemAppsAnnotations();
		local_path = "";
		semapp_id = "";
    }
	
	public EPub(Parcel in) {
		readFromParcel(in);
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	
	public void setLocalPath(String local_path) {
		this.local_path = local_path;
	}
	
	public void setSemAppId(String semapp_id) {
		this.semapp_id = semapp_id;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public File getFile() {
		return file;
	}
	
	public SemAppsAnnotations getAnnotations() {
		return annotations;
	}
	
	public String getLocalPath() {
		return local_path;
	}
	
	public String getSemAppId() {
		return semapp_id;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void readFromParcel(Parcel in) {
		id = in.readString();
		name = in.readString();
		updated_at = in.readString();
		file = in.readParcelable(File.class.getClassLoader());
		annotations = in.readParcelable(SemAppsAnnotations.class.getClassLoader());
		local_path = in.readString();
		semapp_id = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(updated_at);
		dest.writeParcelable(file, flags);
		dest.writeParcelable(annotations, flags);
		dest.writeString(local_path);
		dest.writeString(semapp_id);
	}
	
	public static final Parcelable.Creator<EPub> CREATOR = new Parcelable.Creator<EPub>() {
		public EPub createFromParcel(Parcel in) {
			return new EPub(in);
		}

		public EPub[] newArray(int size) {
			return new EPub[size];
		}
	};
}