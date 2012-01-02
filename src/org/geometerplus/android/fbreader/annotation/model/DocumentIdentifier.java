package org.geometerplus.android.fbreader.annotation.model;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Text;

import android.os.Parcel;
import android.os.Parcelable;

@Element
@Order(elements={"isbn", "title", "authors", "publicationDate"})
public class DocumentIdentifier implements Parcelable {
	
	@Element(required=false)
	protected String isbn;
	@Element(required=false)
	protected String title;
	@ElementList(required=false)
	protected ArrayList<String> authors;
	@Element(required=false)
	protected String publicationDate;
	
	public DocumentIdentifier(String isbn, String title, ArrayList<String> authors, String publicationdate) {
		this.isbn = isbn;
		this.title = title;
		this.authors = authors;
		this.publicationDate = publicationdate;
	}
	
	public DocumentIdentifier() {
		this.isbn = "";
		this.title = "";
		this.authors = new ArrayList<String>();
		this.publicationDate = "";
	}
	
	private DocumentIdentifier(Parcel in) {
		readFromParcel(in);
	}
	
	public void setISBN(String newISBN) {
		isbn = newISBN;
	}
	
	public void setTitle(String newTitle) {
		title = newTitle;
	}
	
	public void setAuthor(ArrayList<String> newAuthors) {
		authors = newAuthors;
	}
	
	public void setPublicationDate(String newPublicationDate) {
		publicationDate = newPublicationDate;
	}
	
	public String getISBN() {
		return isbn;
	}
	
	public String getTitle() {
		return title;
	}
	
	public ArrayList<String> getAuthor() {
		return authors;
	}
	
	public String getPublicationDate() {
		return publicationDate;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		isbn = in.readString();
		title = in.readString();
		if (authors == null) {
			authors = new ArrayList();
		}
		in.readStringList(authors);
		publicationDate = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(isbn);
		dest.writeString(title);
		dest.writeStringList(authors);
		dest.writeString(publicationDate);
	}
	
	public static final Parcelable.Creator<DocumentIdentifier> CREATOR = new Parcelable.Creator<DocumentIdentifier>() {
		public DocumentIdentifier createFromParcel(Parcel in) {
			return new DocumentIdentifier(in);
		}

		public DocumentIdentifier[] newArray(int size) {
			return new DocumentIdentifier[size];
		}
	};
}
