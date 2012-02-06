/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.database.DBAuthor.DBAuthors;
import org.geometerplus.android.fbreader.annotation.database.DBEPub.DBEPubs;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.annotation.model.TargetAuthor;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public abstract class SQLiteUtil {
	
	public static void bindString(SQLiteStatement statement, int index, String value) {
		if (value != null) {
			statement.bindString(index, value);
		} else {
			statement.bindNull(index);
		}
	}

	public static void bindDate(SQLiteStatement statement, int index, Date value) {
		if (value != null) {
			statement.bindLong(index, value.getTime());
		} else {
			statement.bindNull(index);
		}
	}

	public static Date getDate(Cursor cursor, int index) {
		if (cursor.isNull(index)) {
			return null;
		}
		return new Date(cursor.getLong(index));
	}
	
	/**
	 * load all epubs information
	 */
	public static void loadEPubsFromDatabase(Context context) {
		final Cursor cursor;
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		String[] projection = DBEPubs.Projection;
		Uri uri = DBEPubs.CONTENT_URI;
		cursor = context.getContentResolver().query(uri, projection, null, null, null);
		try {
			if (cursor.getCount() == 0) {
				return;
			}
			cursor.moveToFirst();
			do {
				String id = cursor.getString(cursor.getColumnIndex(DBEPubs.EPUB_ID));
				String name = cursor.getString(cursor.getColumnIndex(DBEPubs.NAME));
				String updated_at = cursor.getString(cursor.getColumnIndex(DBEPubs.UPDATED_AT));
				String file_name = cursor.getString(cursor.getColumnIndex(DBEPubs.FILENAME));
				String file_path = cursor.getString(cursor.getColumnIndex(DBEPubs.FILEPATH));
				String local_path = cursor.getString(cursor.getColumnIndex(DBEPubs.LOCALPATH));
				String semapp_id = cursor.getString(cursor.getColumnIndex(DBEPubs.SEMAPP_ID));
				
				if (fbreader.EPubs.getEPubs() != null) {
					fbreader.EPubs.removeAllEPubs();						
				}
				fbreader.EPubs.addEPub(id, name, updated_at, file_name, file_path, local_path, semapp_id);
			} while (cursor.moveToNext());
		} catch (Exception e) {
			e.printStackTrace();
		    Log.e("FBReaderApp", e.toString());
		} finally {
			cursor.close();
		}
	}
	
	/**
	 * load the annotations of one book
	 * @param path
	 */
	public static void loadAnnotationsFromDatabase(Context context, final String local_path) {
		final Cursor cursor_epub;
		final Cursor cursor_annotation;
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		if (fbreader.Annotations.getAnnotations().size() != 0) {
			fbreader.Annotations.removeAllAnnotations();
		}
		
		String ePubId = null;
		Uri uri = DBEPubs.CONTENT_URI;
		String[] projection = DBEPubs.Projection;
		String selection = DBEPubs.LOCALPATH + "=\"" + local_path + "\"";
		
		cursor_epub = context.getContentResolver().query(uri, projection, selection, null, null);
		try {
			if (cursor_epub.getCount() == 0) {
				return;
			}
			cursor_epub.moveToFirst();
			ePubId = cursor_epub.getString(cursor_epub.getColumnIndex(DBEPubs.EPUB_ID));
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			cursor_epub.close();
		}
		
		uri = DBAnnotations.CONTENT_URI;
		projection = DBAnnotations.Projection;
		selection = DBAnnotations.EPUB_ID + "=\"" + ePubId + "\"";
		cursor_annotation = context.getContentResolver().query(uri, projection, selection, null, null);
		try {
			if (cursor_annotation.getCount() == 0) {
				return;
			}
			
			cursor_annotation.moveToFirst();
			do {
				String annotation_id = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.ANNOTATION_ID));
				long created = cursor_annotation.getLong(cursor_annotation.getColumnIndex(DBAnnotations.CREATED));
				long modified = cursor_annotation.getLong(cursor_annotation.getColumnIndex(DBAnnotations.MODIFIED));
				String category = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.CATEGORY));
				String[] tagsarray = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.TAGS)).split(", ");
				ArrayList<String> tags = new ArrayList(Arrays.asList(tagsarray));
				String author_name = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.AUTHOR_NAME));
				String bookid = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.BOOKID));
				String targetannotationid = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.TARGET_ANNOTATION_ID));
				String isbn = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.ISBN));
				String title = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.TITLE));
				String publicationdate = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.PUBLICATIONDATE));
				String start_part = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.START_PART));
				String start_xpath = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.START_PATH_XPATH));
				int start_charoffset = cursor_annotation.getInt(cursor_annotation.getColumnIndex(DBAnnotations.START_PATH_CHAROFFSET));
				String end_part = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.END_PART));
				String end_xpath = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.END_PATH_XPATH));
				int end_charoffset = cursor_annotation.getInt(cursor_annotation.getColumnIndex(DBAnnotations.END_PATH_CHAROFFSET));
				int highlightcolor = cursor_annotation.getInt(cursor_annotation.getColumnIndex(DBAnnotations.HIGHLIGHTCOLOR));
				boolean underlined = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.UNDERLINED)).equals("true") ?	 true : false;
				boolean crossout = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.CROSSOUT)).equals("true") ? true : false;
				String content = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.CONTENT));
				String upb_id = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.UPB_ID));
				String updated_at = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.UPDATED_AT));
				String epub_id = cursor_annotation.getString(cursor_annotation.getColumnIndex(DBAnnotations.EPUB_ID));
				
				uri = DBAuthors.CONTENT_URI;
				projection = DBAuthors.Projection;
				selection = DBAuthors.EPUB_ID + "=\"" + epub_id + "\"";
				Cursor aCursor = context.getContentResolver().query(uri, projection, selection, null, null);
				ArrayList<String> authors = new ArrayList<String>();
				try {
					aCursor.moveToFirst();
					do {
						if (aCursor.getCount() != 0) {
							authors.add(aCursor.getString(aCursor.getColumnIndex(DBAuthors.NAME)));
						}
					} while (aCursor.moveToNext());
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					aCursor.close();
				}
				
				fbreader.Annotations.addAnnotation(annotation_id, created, modified, category, tags, 
						author_name, bookid, targetannotationid, isbn, title, authors, publicationdate, start_part, 
						start_xpath, start_charoffset, end_part, end_xpath, end_charoffset, 
						highlightcolor, underlined, crossout, content, epub_id, upb_id, updated_at);
		    } while (cursor_annotation.moveToNext());
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			cursor_annotation.close();
		}
	}
	
	/**
	 * Insert the ePub information into the database or update it 
	 * @param semApp
	 */
	public static void writeEPubToDatabase(final Context context, EPub ePub, final String local_path, String sid) {
		final String epub_id = ePub.getId();
		final String name = ePub.getName();
		final String updated_at = ePub.getUpdated_at();
		final String file_name = ePub.getFile().getName();
		final String file_path = ePub.getFile().getPath();
		final String semapp_id = sid;
		
		// working on database in a different thread
		new Thread(new Runnable() {
			Cursor cursor;
			
			@Override
			public void run() {
				Uri uri = DBEPubs.CONTENT_URI;
				String[] projection = DBEPubs.Projection;
				String selection = DBEPubs.EPUB_ID + "=\"" + epub_id + "\"";
				try {
					cursor = context.getContentResolver().query(uri, projection, selection, null, null);

					ContentValues values = new ContentValues();
					values.put(DBEPubs.EPUB_ID, epub_id);
					values.put(DBEPubs.NAME, name);
					values.put(DBEPubs.UPDATED_AT, updated_at);
					values.put(DBEPubs.FILENAME, file_name);
					values.put(DBEPubs.FILEPATH, file_path);
					values.put(DBEPubs.SEMAPP_ID, semapp_id);
					values.put(DBEPubs.LOCALPATH, local_path);
					if (cursor.getCount() == 0) {
						context.getContentResolver().insert(uri, values);
					} else {
						context.getContentResolver().update(uri, values, selection, null);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					cursor.close();
				}
			}
		}).start();
	}
	
	/**
	 * Insert the ePub and annotation information into the database or update it 
	 * @param semApp
	 */
	public static void writeEPubAndAnnotationToDatabase(final Context context, EPub ePub, 
			final String local_path, String sid, Annotation annotation, final String eid) {
		// epub data
		final String epub_id = ePub.getId();
		final String name = ePub.getName();
		final String updated_at = ePub.getUpdated_at();
		final String file_name = ePub.getFile().getName();
		final String file_path = ePub.getFile().getPath();
		final String semapp_id = sid;
		
		// annotation data
		final String annotation_id = annotation.getId();
		final long created = annotation.getCreated();
		final long modified = annotation.getModified();
		final String category = annotation.getCategory();
		String tags_tmp = "";
		for (String tag : annotation.getTags()) {
			tags_tmp += tag;
			tags_tmp += ", ";
		}
		if (tags_tmp.length() > 0) {
			tags_tmp = tags_tmp.substring(0, tags_tmp.length()-2);
		}
		final String tags = tags_tmp;
		final String author_name = annotation.getAuthor().getName();
		final String bookid = annotation.getAnnotationTarget().getBookId();
		final String targetannotationid = annotation.getAnnotationTarget().getTargetAnnotationId();
		final String isbn = annotation.getAnnotationTarget().getDocumentIdentifier().getISBN();
		final String title = annotation.getAnnotationTarget().getDocumentIdentifier().getTitle();
		final ArrayList<TargetAuthor> authors = annotation.getAnnotationTarget().getDocumentIdentifier().getAuthors();
		final String publicationdate = annotation.getAnnotationTarget().getDocumentIdentifier().getPublicationDate();
		final String start_part = annotation.getAnnotationTarget().getRange().getStart().getPart();
		final String start_xpath = annotation.getAnnotationTarget().getRange().getStart().getPath().getXPath();
		final long start_charoffset = annotation.getAnnotationTarget().getRange().getStart().getPath().getCharOffset();
		final String end_part = annotation.getAnnotationTarget().getRange().getEnd().getPart();
		final String end_xpath = annotation.getAnnotationTarget().getRange().getEnd().getPath().getXPath();
		final long end_charoffset = annotation.getAnnotationTarget().getRange().getEnd().getPath().getCharOffset();
		final long highlightcolor = annotation.getRenderingInfo().getHighlightColor();
		final String underlined = annotation.getRenderingInfo().isUnderlined() ? "true" : "false";
		final String crossout = annotation.getRenderingInfo().isCrossedOut() ? "true" : "false";
		final String content = annotation.getAnnotationContent().getAnnotationText();
		final String upb_id = annotation.getUPBId();
		final String updated_at2 = annotation.getUpdatedAt();
		
		// working on database in a different thread
		new Thread(new Runnable() {
			Cursor cursor;
			
			@Override
			public void run() {
				Uri uri = DBEPubs.CONTENT_URI;
				String[] projection = DBEPubs.Projection;
				String selection = DBEPubs.EPUB_ID + "=\"" + epub_id + "\"";
				cursor = context.getContentResolver().query(uri, projection, selection, null, null);

				ContentValues values = new ContentValues();
				values.put(DBEPubs.EPUB_ID, epub_id);
				values.put(DBEPubs.NAME, name);
				values.put(DBEPubs.UPDATED_AT, updated_at);
				values.put(DBEPubs.FILENAME, file_name);
				values.put(DBEPubs.FILEPATH, file_path);
				values.put(DBEPubs.SEMAPP_ID, semapp_id);
				values.put(DBEPubs.LOCALPATH, local_path);
				if (cursor.getCount() == 0) {
					context.getContentResolver().insert(uri, values);
				} else {
					context.getContentResolver().update(uri, values, selection, null);
				}
				cursor.close();
				
				uri = DBAnnotations.CONTENT_URI;
				projection = DBAnnotations.Projection;
				selection = DBAnnotations.ANNOTATION_ID + "=\"" + annotation_id + "\"";
				cursor = context.getContentResolver().query(uri, projection, selection, null, null);
				values.clear();
				values.put(DBAnnotations.ANNOTATION_ID, annotation_id);
				values.put(DBAnnotations.CREATED, created);
				values.put(DBAnnotations.MODIFIED, modified);
				values.put(DBAnnotations.CATEGORY, category);
				values.put(DBAnnotations.TAGS, tags);
				values.put(DBAnnotations.AUTHOR_NAME, author_name);
				values.put(DBAnnotations.BOOKID, bookid);
				values.put(DBAnnotations.TARGET_ANNOTATION_ID, targetannotationid);
				values.put(DBAnnotations.ISBN, isbn);
				values.put(DBAnnotations.TITLE, title);
				values.put(DBAnnotations.PUBLICATIONDATE, publicationdate);
				values.put(DBAnnotations.START_PART, start_part);
				values.put(DBAnnotations.START_PATH_XPATH, start_xpath);
				values.put(DBAnnotations.START_PATH_CHAROFFSET, start_charoffset);
				values.put(DBAnnotations.END_PART, end_part);
				values.put(DBAnnotations.END_PATH_XPATH, end_xpath);
				values.put(DBAnnotations.END_PATH_CHAROFFSET, end_charoffset);
				values.put(DBAnnotations.HIGHLIGHTCOLOR, highlightcolor);
				values.put(DBAnnotations.UNDERLINED, underlined);
				values.put(DBAnnotations.CROSSOUT, crossout);
				values.put(DBAnnotations.CONTENT, content);
				values.put(DBAnnotations.UPB_ID, upb_id);
				values.put(DBAnnotations.UPDATED_AT, updated_at2);
				values.put(DBAnnotations.EPUB_ID, epub_id);
				if (cursor.getCount() == 0) {
					context.getContentResolver().insert(uri, values);
					uri = DBAuthors.CONTENT_URI;
					projection = DBAuthors.Projection;
					selection = DBAuthors.EPUB_ID + "=\"" + epub_id + "\"";
					Cursor aCursor = context.getContentResolver().query(uri, projection, selection, null, null);
					ArrayList<String> dbAuthors = new ArrayList<String>();
					try {
						aCursor.moveToFirst();
						if (aCursor.getCount() == 0) { 
							for (TargetAuthor author : authors) {
								values.clear();
								values.put(DBAuthors.NAME, author.getName());
								values.put(DBAuthors.EPUB_ID, epub_id);
								context.getContentResolver().insert(uri, values);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					} finally {
						aCursor.close();
					}
				} else if (cursor.getCount() > 0) {
					context.getContentResolver().update(uri, values, selection, null);
					uri = DBAuthors.CONTENT_URI;
					projection = DBAuthors.Projection;
					selection = DBAuthors.EPUB_ID + "=\"" + epub_id + "\"";
					Cursor aCursor = context.getContentResolver().query(uri, projection, selection, null, null);
					ArrayList<String> dbAuthors = new ArrayList<String>();
					try {
						aCursor.moveToFirst();
						if (aCursor.getCount() == 0) { 
							for (TargetAuthor author : authors) {
								values.clear();
								values.put(DBAuthors.NAME, author.getName());
								values.put(DBAuthors.EPUB_ID, epub_id);
								context.getContentResolver().update(uri, values, selection, null);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					} finally {
						aCursor.close();
					}
				}
				
				cursor.close();
			}
		}).start();
	}
	
	/**
	 * Insert one Annotation of the book into the database or update it 
	 */
	public static void writeAnnotationToDatabase(final Context context, Annotation annotation, final String eid) {
		final String annotation_id = annotation.getId();
		final long created = annotation.getCreated();
		final long modified = annotation.getModified();
		final String category = annotation.getCategory();
		String tags_tmp = "";
		for (String tag : annotation.getTags()) {
			tags_tmp += tag;
			tags_tmp += ", ";
		}
		if (tags_tmp.length() > 0) {
			tags_tmp = tags_tmp.substring(0, tags_tmp.length()-2);
		}
		final String tags = tags_tmp;
		final String author_name = annotation.getAuthor().getName();
		final String bookid = annotation.getAnnotationTarget().getBookId();
		final String targetannotationid = annotation.getAnnotationTarget().getTargetAnnotationId();
		final String isbn = annotation.getAnnotationTarget().getDocumentIdentifier().getISBN();
		final String title = annotation.getAnnotationTarget().getDocumentIdentifier().getTitle();
		final ArrayList<TargetAuthor> authors = annotation.getAnnotationTarget().getDocumentIdentifier().getAuthors();
		final String publicationdate = annotation.getAnnotationTarget().getDocumentIdentifier().getPublicationDate();
		final String start_part = annotation.getAnnotationTarget().getRange().getStart().getPart();
		final String start_xpath = annotation.getAnnotationTarget().getRange().getStart().getPath().getXPath();
		final long start_charoffset = annotation.getAnnotationTarget().getRange().getStart().getPath().getCharOffset();
		final String end_part = annotation.getAnnotationTarget().getRange().getEnd().getPart();
		final String end_xpath = annotation.getAnnotationTarget().getRange().getEnd().getPath().getXPath();
		final long end_charoffset = annotation.getAnnotationTarget().getRange().getEnd().getPath().getCharOffset();
		final long highlightcolor = annotation.getRenderingInfo().getHighlightColor();
		final String underlined = annotation.getRenderingInfo().isUnderlined() ? "true" : "false";
		final String crossout = annotation.getRenderingInfo().isCrossedOut() ? "true" : "false";
		final String content = annotation.getAnnotationContent().getAnnotationText();
		final String upb_id = annotation.getUPBId();
		final String updated_at2 = annotation.getUpdatedAt();
		
		// working on database in a different thread
		new Thread(new Runnable() {
			Cursor cursor;
			
			@Override
			public void run() {
				String epub_id;
				
				Uri uri = DBEPubs.CONTENT_URI;
				String[] projection = DBEPubs.Projection;
				String selection = DBEPubs.EPUB_ID + "=\"" + eid + "\"";
				try {
					cursor = context.getContentResolver().query(uri, projection, selection, null, null);
					cursor.moveToFirst();
					if (cursor.getCount() == 0) {
						epub_id = eid;
					} else {
						epub_id = cursor.getString(cursor.getColumnIndex(DBEPubs.EPUB_ID));
					}
					
					uri = DBAnnotations.CONTENT_URI;
					projection = DBAnnotations.Projection;
					selection = DBAnnotations.ANNOTATION_ID + "=\"" + annotation_id + "\"";
					cursor = context.getContentResolver().query(uri, projection, selection, null, null);
					ContentValues values = new ContentValues();
					values.put(DBAnnotations.ANNOTATION_ID, annotation_id);
					values.put(DBAnnotations.CREATED, created);
					values.put(DBAnnotations.MODIFIED, modified);
					values.put(DBAnnotations.CATEGORY, category);
					values.put(DBAnnotations.TAGS, tags);
					values.put(DBAnnotations.AUTHOR_NAME, author_name);
					values.put(DBAnnotations.BOOKID, bookid);
					values.put(DBAnnotations.TARGET_ANNOTATION_ID, targetannotationid);
					values.put(DBAnnotations.ISBN, isbn);
					values.put(DBAnnotations.TITLE, title);
					values.put(DBAnnotations.PUBLICATIONDATE, publicationdate);
					values.put(DBAnnotations.START_PART, start_part);
					values.put(DBAnnotations.START_PATH_XPATH, start_xpath);
					values.put(DBAnnotations.START_PATH_CHAROFFSET, start_charoffset);
					values.put(DBAnnotations.END_PART, end_part);
					values.put(DBAnnotations.END_PATH_XPATH, end_xpath);
					values.put(DBAnnotations.END_PATH_CHAROFFSET, end_charoffset);
					values.put(DBAnnotations.HIGHLIGHTCOLOR, highlightcolor);
					values.put(DBAnnotations.UNDERLINED, underlined);
					values.put(DBAnnotations.CROSSOUT, crossout);
					values.put(DBAnnotations.CONTENT, content);
					values.put(DBAnnotations.UPB_ID, upb_id);
					values.put(DBAnnotations.UPDATED_AT, updated_at2);
					values.put(DBAnnotations.EPUB_ID, epub_id);
					
					if (cursor.getCount() == 0) {
						context.getContentResolver().insert(uri, values);
						uri = DBAuthors.CONTENT_URI;
						projection = DBAuthors.Projection;
						selection = DBAuthors.EPUB_ID + "=\"" + epub_id + "\"";
						Cursor aCursor = context.getContentResolver().query(uri, projection, selection, null, null);
						ArrayList<String> dbAuthors = new ArrayList<String>();
						try {
							aCursor.moveToFirst();
							if (aCursor.getCount() == 0) { 
								for (TargetAuthor author : authors) {
									values.clear();
									values.put(DBAuthors.NAME, author.getName());
									values.put(DBAuthors.EPUB_ID, epub_id);
									context.getContentResolver().insert(uri, values);
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						} finally {
							aCursor.close();
						}
					}  else if (cursor.getCount() > 0) {
						context.getContentResolver().update(uri, values, selection, null);
						uri = DBAuthors.CONTENT_URI;
						projection = DBAuthors.Projection;
						selection = DBAuthors.EPUB_ID + "=\"" + epub_id + "\"";
						Cursor aCursor = context.getContentResolver().query(uri, projection, selection, null, null);
						ArrayList<String> dbAuthors = new ArrayList<String>();
						try {
							aCursor.moveToFirst();
							if (aCursor.getCount() == 0) { 
								for (TargetAuthor author : authors) {
									values.clear();
									values.put(DBAuthors.NAME, author.getName());
									values.put(DBAuthors.EPUB_ID, epub_id);
									context.getContentResolver().update(uri, values, selection, null);
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						} finally {
							aCursor.close();
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					cursor.close();
				}
			}
		}).start();
	}
	
}