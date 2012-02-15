package org.geometerplus.android.fbreader.semapps.model;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Root
@Order(elements={"id", "name", "created-at", "updated-at", "file-name", "file-path"})
public class EPub implements Parcelable {
	
	@Element(required=false)
	protected int id;
	@Element(required=false)
	protected String name;
	@Element(required=false, name="created-at")
	protected String created_at;
	@Element(required=false, name="updated-at")
	protected String updated_at;
	@Element(required=false, name="file-name")
	protected String file_name;
	@Element(required=false, name="file-path")
	protected String file_path;

	protected Scenarios scenarios;
	protected String local_path;
	protected int semapp_id;
	
	/**
	 * Standard empty constructor
	 */
	public EPub() {
		id = -1;
		name = "";
		created_at = "";
		updated_at = "";
		file_name = "";
		file_path = "";
		scenarios = new Scenarios();
		local_path = "";
		semapp_id = -1;
    }
	
	public EPub(Parcel in) {
		readFromParcel(in);
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	
	public void setFileName(String file_name) {
		this.file_name = file_name;
	}
	
	public void setFilePath(String file_path) {
		this.file_path = file_path;
	}
	
	public void setLocalPath(String local_path) {
		this.local_path = local_path;
	}
	
	public void setSemAppId(int semapp_id) {
		this.semapp_id = semapp_id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCreated_at() {
		return created_at;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public String getFileName() {
		return file_name;
	}
	
	public String getFilePath() {
		return file_path;
	}
	
	public Scenarios getScenarios() {
		return scenarios;
	}
	
	public String getLocalPath() {
		return local_path;
	}
	
	public int getSemAppId() {
		return semapp_id;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void readFromParcel(Parcel in) {
		id = in.readInt();
		name = in.readString();
		created_at = in.readString();
		updated_at = in.readString();
		file_name = in.readString();
		file_path = in.readString();
		scenarios = in.readParcelable(Scenarios.class.getClassLoader());
		local_path = in.readString();
		semapp_id = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(created_at);
		dest.writeString(updated_at);
		dest.writeString(file_name);
		dest.writeString(file_path);
		dest.writeParcelable(scenarios, flags);
		dest.writeString(local_path);
		dest.writeInt(semapp_id);
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