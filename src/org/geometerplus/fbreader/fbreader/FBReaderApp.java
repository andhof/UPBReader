/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.fbreader;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.application.*;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.annotation.database.AnnotationsDbAdapter;
import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.database.DBAuthor.DBAuthors;
import org.geometerplus.android.fbreader.annotation.database.DBEPub.DBEPubs;
import org.geometerplus.android.fbreader.annotation.database.DBSemApp.DBSemApps;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.annotation.model.Annotations;
import org.geometerplus.android.fbreader.annotation.model.TargetAuthor;
import org.geometerplus.android.fbreader.provider.AnnotationsContentProvider;
import org.geometerplus.android.fbreader.provider.SemAppsContentProvider;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.EPubs;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.library.*;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public final class FBReaderApp extends ZLApplication {
	public final ZLBooleanOption AllowScreenBrightnessAdjustmentOption =
		new ZLBooleanOption("LookNFeel", "AllowScreenBrightnessAdjustment", true);
	public final ZLStringOption TextSearchPatternOption =
		new ZLStringOption("TextSearch", "Pattern", "");

	public final ZLBooleanOption UseSeparateBindingsOption =
		new ZLBooleanOption("KeysOptions", "UseSeparateBindings", false);

	public final ZLBooleanOption EnableDoubleTapOption =
		new ZLBooleanOption("Options", "EnableDoubleTap", false);
	public final ZLBooleanOption NavigateAllWordsOption =
		new ZLBooleanOption("Options", "NavigateAllWords", false);

	public static enum WordTappingAction {
		doNothing, selectSingleWord, startSelecting, openDictionary
	}
	public final ZLEnumOption<WordTappingAction> WordTappingActionOption =
		new ZLEnumOption<WordTappingAction>("Options", "WordTappingAction", WordTappingAction.startSelecting);

	public final ZLColorOption ImageViewBackgroundOption =
		new ZLColorOption("Colors", "ImageViewBackground", new ZLColor(127, 127, 127));
	public static enum ImageTappingAction {
		doNothing, selectImage, openImageView
	}
	public final ZLEnumOption<ImageTappingAction> ImageTappingActionOption =
		new ZLEnumOption<ImageTappingAction>("Options", "ImageTappingAction", ImageTappingAction.openImageView);

	private final int myDpi = ZLibrary.Instance().getDisplayDPI();
	public final ZLIntegerRangeOption LeftMarginOption =
		new ZLIntegerRangeOption("Options", "LeftMargin", 0, 30, myDpi / 5);
	public final ZLIntegerRangeOption RightMarginOption =
		new ZLIntegerRangeOption("Options", "RightMargin", 0, 30, myDpi / 5);
	public final ZLIntegerRangeOption TopMarginOption =
		new ZLIntegerRangeOption("Options", "TopMargin", 0, 60, 15);
	public final ZLIntegerRangeOption BottomMarginOption =
		new ZLIntegerRangeOption("Options", "BottomMargin", 0, 60, 20);

	public final ZLIntegerRangeOption ScrollbarTypeOption =
		new ZLIntegerRangeOption("Options", "ScrollbarType", 0, 3, FBView.SCROLLBAR_SHOW_AS_FOOTER);
	public final ZLIntegerRangeOption FooterHeightOption =
		new ZLIntegerRangeOption("Options", "FooterHeight", 8, 20, 9);
	public final ZLBooleanOption FooterShowTOCMarksOption =
		new ZLBooleanOption("Options", "FooterShowTOCMarks", true);
	public final ZLBooleanOption FooterShowClockOption =
		new ZLBooleanOption("Options", "ShowClockInFooter", true);
	public final ZLBooleanOption FooterShowBatteryOption =
		new ZLBooleanOption("Options", "ShowBatteryInFooter", true);
	public final ZLBooleanOption FooterShowProgressOption =
		new ZLBooleanOption("Options", "ShowProgressInFooter", true);
	public final ZLStringOption FooterFontOption =
		new ZLStringOption("Options", "FooterFont", "Droid Sans");

	final ZLStringOption ColorProfileOption =
		new ZLStringOption("Options", "ColorProfile", ColorProfile.DAY);

	public final ZLBooleanOption ShowPreviousBookInCancelMenuOption =
		new ZLBooleanOption("CancelMenu", "previousBook", false);
	public final ZLBooleanOption ShowPositionsInCancelMenuOption =
		new ZLBooleanOption("CancelMenu", "positions", true);

	private final ZLKeyBindings myBindings = new ZLKeyBindings("Keys");
	
	private Context context;
	private AnnotationsDbAdapter dbHelper;
	private Cursor cursor;

	public final FBView BookTextView;
	public final FBView FootnoteView;
	public Annotations Annotations;
	public EPubs EPubs;
	
	public ArrayList<String> CategoriesEN;
	public ArrayList<String> CategoriesDE;

	public BookModel Model;

	private final String myArg0;
	
	private ArrayList<Integer> paragraphIndexList;
	private ArrayList<String> paragraphFilePathList;
	
	private TreeMap<String, Integer> myTOCLabels;
	private ArrayList<String> myHtmlFileNames;
	private ArrayList<Integer> myParagraphIndexList;

	public FBReaderApp(String arg, Context context) {
		myArg0 = arg;
		this.context = context;
		
		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));

		addAction(ActionCode.FIND_NEXT, new FindNextAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new FindPreviousAction(this));
		addAction(ActionCode.CLEAR_FIND_RESULTS, new ClearFindResultsAction(this));

		addAction(ActionCode.SELECTION_CLEAR, new SelectionClearAction(this));

		addAction(ActionCode.TURN_PAGE_FORWARD, new TurnPageAction(this, true));
		addAction(ActionCode.TURN_PAGE_BACK, new TurnPageAction(this, false));

		addAction(ActionCode.MOVE_CURSOR_UP, new MoveCursorAction(this, FBView.Direction.up));
		addAction(ActionCode.MOVE_CURSOR_DOWN, new MoveCursorAction(this, FBView.Direction.down));
		addAction(ActionCode.MOVE_CURSOR_LEFT, new MoveCursorAction(this, FBView.Direction.rightToLeft));
		addAction(ActionCode.MOVE_CURSOR_RIGHT, new MoveCursorAction(this, FBView.Direction.leftToRight));

		addAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD, new VolumeKeyTurnPageAction(this, true));
		addAction(ActionCode.VOLUME_KEY_SCROLL_BACK, new VolumeKeyTurnPageAction(this, false));

		addAction(ActionCode.SWITCH_TO_DAY_PROFILE, new SwitchProfileAction(this, ColorProfile.DAY));
		addAction(ActionCode.SWITCH_TO_NIGHT_PROFILE, new SwitchProfileAction(this, ColorProfile.NIGHT));

		addAction(ActionCode.EXIT, new ExitAction(this));

		BookTextView = new FBView(this);
		FootnoteView = new FBView(this);
		Annotations = new Annotations();
		EPubs = new EPubs();
		
		CategoriesEN = new ArrayList<String> ();
		CategoriesEN.add("Note");
		CategoriesEN.add("Question");
		CategoriesEN.add("Issue");
		CategoriesEN.add("Comment");
		CategoriesDE = new ArrayList<String> ();
		CategoriesDE.add("Notiz");
		CategoriesDE.add("Frage");
		CategoriesDE.add("Problem");
		CategoriesDE.add("Kommentar");
		
		paragraphIndexList = new ArrayList<Integer>();
		paragraphFilePathList = new ArrayList<String>();
		
		myTOCLabels = new TreeMap<String, Integer>();
		myHtmlFileNames = new ArrayList<String>();
		myParagraphIndexList = new ArrayList<Integer>();
		
		dbHelper = new AnnotationsDbAdapter(context);

		setView(BookTextView);
	}

	@Override
	public void initWindow() {
		super.initWindow();
		wait("loadingBook", new Runnable() {
			public void run() {
				Book book = createBookForFile(ZLFile.createFileByPath(myArg0));
				if (book == null) {
					book = Library.getRecentBook();
				}
				if ((book == null) || !book.File.exists()) {
					book = Book.getByFile(Library.getHelpFile());
				}
				
				// load the annotations of the book
				String path = book.File.getPath();
				loadEPubsFromDatabase();
				for (EPub epub : EPubs.getEPubs()) {
					loadAnnotationsFromDatabase(path);
				}
				
				openBookInternal(book, null);
			}
		});
	}

	public void openBook(final Book book, final Bookmark bookmark) {
		if (book == null) {
			return;
		}
		if (Model != null) {
			if (bookmark == null & book.File.getPath().equals(Model.Book.File.getPath())) {
				return;
			}
		}
		wait("loadingBook", new Runnable() {
			public void run() {
				// load the annotations of the book
				// TODO mal schauen ob das hier überhaupt gebraucht wird
				String path = book.File.getPath();
				loadEPubsFromDatabase();
				for (EPub epub : EPubs.getEPubs()) {
					loadAnnotationsFromDatabase(path);
				}
				
				openBookInternal(book, bookmark);
			}
		});
	}
	
	public void setParagraphIndexList(ArrayList<Integer> myParagraphIndexList) {
		this.myParagraphIndexList = myParagraphIndexList;
	}
	
	public void setHtmlFileNames(ArrayList<String> myHtmlFileNames, String myFilePrefix,
			String myCoverFileName) {
		ArrayList<String> tmpHtmlFileNames =  new ArrayList<String>();
		
		String cutString = ".epub:";
		String myShortFilePrefix = myFilePrefix.substring(myFilePrefix.indexOf(cutString) + cutString.length());
		for (String entry : myHtmlFileNames) {
			if ((myFilePrefix + entry).equals(myCoverFileName)) {
				continue;
			}
			tmpHtmlFileNames.add(myShortFilePrefix + entry);
			Log.v("FBReaderApp", "Datei: " + myShortFilePrefix + entry);
        }
		this.myHtmlFileNames = tmpHtmlFileNames;
	}
	
	/**
	 * load all epubs information
	 */
	private void loadEPubsFromDatabase() {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		String[] projection = DBEPubs.Projection;
		Uri uri = DBEPubs.CONTENT_URI;
		cursor = context.getContentResolver().query(uri, projection, null, null, null);
		
//		dbHelper.open();
//		cursor = dbHelper.fetchAllEPubs();
		if (cursor.getCount() == 0) {
			return;
		}
		cursor.moveToFirst();
		do {
			String id = cursor.getString(cursor.getColumnIndex("_id"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String updated_at = cursor.getString(cursor.getColumnIndex("updated_at"));
			String file_name = cursor.getString(cursor.getColumnIndex("file_name"));
			String file_path = cursor.getString(cursor.getColumnIndex("file_path"));
			
			if (fbreader.EPubs.getEPubs() != null) {
				fbreader.EPubs.removeAllEPubs();						
			}
			fbreader.EPubs.addEPub(id, name, updated_at, file_name, file_path);
		} while (cursor.moveToNext());
		cursor.close();
//		dbHelper.close();
	}
	
	/**
	 * load the annotations of one book
	 * @param path
	 */
	private void loadAnnotationsFromDatabase(final String local_path) {
		if (Annotations.getAnnotations().size() != 0) {
			Annotations.removeAllAnnotations();
		}
		
		Uri uri = DBEPubs.CONTENT_URI;
		String[] projection = DBEPubs.Projection;
		String selection = DBEPubs.LOCALPATH + "=\"" + local_path + "\"";
		cursor = context.getContentResolver().query(uri, projection, selection, null, null);
		
//		dbHelper.open();
//		cursor = dbHelper.fetchEPubByPath(local_path);
		if (cursor.getCount() == 0) {
			return;
		}
		cursor.moveToFirst();
		String ePubId = cursor.getString(cursor.getColumnIndex(DBEPubs.EPUB_ID));
		cursor.close();
		
		uri = DBAnnotations.CONTENT_URI;
		projection = DBAnnotations.Projection;
		selection = DBAnnotations.EPUB_ID + "=\"" + ePubId + "\"";
		cursor = context.getContentResolver().query(uri, projection, selection, null, null);
//		cursor = dbHelper.fetchAnnotationsByEPubId(ePubId);
		if (cursor.getCount() == 0) {
			return;
		}
		
		cursor.moveToFirst();
		do {
			String annotation_id = cursor.getString(cursor.getColumnIndex(DBAnnotations.ANNOTATION_ID));
			long created = cursor.getLong(cursor.getColumnIndex(DBAnnotations.CREATED));
			long modified = cursor.getLong(cursor.getColumnIndex(DBAnnotations.MODIFIED));
			String category = cursor.getString(cursor.getColumnIndex(DBAnnotations.CATEGORY));
			String[] tagsarray = cursor.getString(cursor.getColumnIndex(DBAnnotations.TAGS)).split(", ");
			ArrayList<String> tags = new ArrayList(Arrays.asList(tagsarray));
			String author_name = cursor.getString(cursor.getColumnIndex(DBAnnotations.AUTHOR_NAME));
			String bookid = cursor.getString(cursor.getColumnIndex(DBAnnotations.BOOKID));
			String targetannotationid = cursor.getString(cursor.getColumnIndex(DBAnnotations.TARGET_ANNOTATION_ID));
			String isbn = cursor.getString(cursor.getColumnIndex(DBAnnotations.ISBN));
			String title = cursor.getString(cursor.getColumnIndex(DBAnnotations.TITLE));
			String publicationdate = cursor.getString(cursor.getColumnIndex(DBAnnotations.PUBLICATIONDATE));
			String start_part = cursor.getString(cursor.getColumnIndex(DBAnnotations.START_PART));
			String start_xpath = cursor.getString(cursor.getColumnIndex(DBAnnotations.START_PATH_XPATH));
			int start_charoffset = cursor.getInt(cursor.getColumnIndex(DBAnnotations.START_PATH_CHAROFFSET));
			String end_part = cursor.getString(cursor.getColumnIndex(DBAnnotations.END_PART));
			String end_xpath = cursor.getString(cursor.getColumnIndex(DBAnnotations.END_PATH_XPATH));
			int end_charoffset = cursor.getInt(cursor.getColumnIndex(DBAnnotations.END_PATH_CHAROFFSET));
			int highlightcolor = cursor.getInt(cursor.getColumnIndex(DBAnnotations.HIGHLIGHTCOLOR));
			boolean underlined = cursor.getString(cursor.getColumnIndex(DBAnnotations.UNDERLINED)).equals("true") ?	 true : false;
			boolean crossout = cursor.getString(cursor.getColumnIndex(DBAnnotations.CROSSOUT)).equals("true") ? true : false;
			String content = cursor.getString(cursor.getColumnIndex(DBAnnotations.CONTENT));
			String upb_id = cursor.getString(cursor.getColumnIndex(DBAnnotations.UPB_ID));
			String updated_at = cursor.getString(cursor.getColumnIndex(DBAnnotations.UPDATED_AT));
			String epub_id = cursor.getString(cursor.getColumnIndex(DBAnnotations.EPUB_ID));
			
			uri = DBAuthors.CONTENT_URI;
			projection = DBAuthors.Projection;
			selection = DBAuthors.ANNOTATION_ID + "=\"" + annotation_id + "\"";
			Cursor aCursor = context.getContentResolver().query(uri, projection, selection, null, null);
//			Cursor aCursor = dbHelper.fetchAuthor(id);
			ArrayList<String> authors = new ArrayList<String>();
			aCursor.moveToFirst();
			do {
				if (aCursor.getCount() != 0) {
					authors.add(aCursor.getString(aCursor.getColumnIndex(DBAuthors.NAME)));
				}
			} while (aCursor.moveToNext());
			aCursor.close();
			
			Annotations.addAnnotation(annotation_id, created, modified, category, tags, 
					author_name, bookid, targetannotationid, isbn, title, authors, publicationdate, start_part, 
					start_xpath, start_charoffset, end_part, end_xpath, end_charoffset, 
					highlightcolor, underlined, crossout, content, upb_id, updated_at);
	    } while (cursor.moveToNext());
		cursor.close();
//		dbHelper.close();
	}
	
	/**
	 * Insert the ePub and annotation information into the database or update it 
	 * @param semApp
	 */
	public void writeEPubAndAnnotationToDatabase(Context context, EPub ePub, 
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
			@Override
			public void run() {
				Uri uri = DBEPubs.CONTENT_URI;
				String[] projection = DBEPubs.Projection;
				String selection = DBEPubs.EPUB_ID + "=\"" + epub_id + "\"";
				cursor = FBReaderApp.this.context.getContentResolver().query(uri, projection, selection, null, null);

				ContentValues values = new ContentValues();
				values.put(DBEPubs.EPUB_ID, epub_id);
				values.put(DBEPubs.NAME, name);
				values.put(DBEPubs.UPDATED_AT, updated_at);
				values.put(DBEPubs.FILENAME, file_name);
				values.put(DBEPubs.FILEPATH, file_path);
				values.put(DBEPubs.SEMAPP_ID, semapp_id);
				values.put(DBEPubs.LOCALPATH, local_path);
				if (cursor.getCount() == 0) {
					FBReaderApp.this.context.getContentResolver().insert(uri, values);
				} else {
					FBReaderApp.this.context.getContentResolver().update(uri, values, selection, null);
				}
				cursor.close();
				
				uri = DBAnnotations.CONTENT_URI;
				projection = DBAnnotations.Projection;
				selection = DBAnnotations.ANNOTATION_ID + "=\"" + annotation_id + "\"";
				cursor = FBReaderApp.this.context.getContentResolver().query(uri, projection, selection, null, null);
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
					FBReaderApp.this.context.getContentResolver().insert(uri, values);
					uri = DBAuthors.CONTENT_URI;
					projection = DBAuthors.Projection;
					for (TargetAuthor author : authors) {
						values.clear();
						values.put(DBAuthors.NAME, author.getName());
						values.put(DBAuthors.ANNOTATION_ID, annotation_id);
						FBReaderApp.this.context.getContentResolver().insert(uri, values);
					}
				} else if (cursor.getCount() > 0) {
					FBReaderApp.this.context.getContentResolver().update(uri, values, selection, null);
					uri = DBAuthors.CONTENT_URI;
					projection = DBAuthors.Projection;
					selection = DBAuthors.ANNOTATION_ID + "=\"" + annotation_id + "\"";
					for (TargetAuthor author : authors) {
						values.clear();
						values.put(DBAuthors.NAME, author.getName());
						values.put(DBAuthors.ANNOTATION_ID, annotation_id);
						FBReaderApp.this.context.getContentResolver().update(uri, values, selection, null);
					}
				}
				
				cursor.close();
			}
		}).start();
	}
	
	/**
	 * Insert the ePub information into the database or update it 
	 * @param semApp
	 */
	public void writeEPubToDatabase(Context context, EPub ePub, final String local_path, String sid) {
		final String epub_id = ePub.getId();
		final String name = ePub.getName();
		final String updated_at = ePub.getUpdated_at();
		final String file_name = ePub.getFile().getName();
		final String file_path = ePub.getFile().getPath();
		final String semapp_id = sid;
		
		// working on database in a different thread
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Uri uri = DBEPubs.CONTENT_URI;
				String[] projection = DBEPubs.Projection;
				String selection = DBEPubs.EPUB_ID + "=\"" + epub_id + "\"";
				cursor = FBReaderApp.this.context.getContentResolver().query(uri, projection, selection, null, null);

				ContentValues values = new ContentValues();
				values.put(DBEPubs.EPUB_ID, epub_id);
				values.put(DBEPubs.NAME, name);
				values.put(DBEPubs.UPDATED_AT, updated_at);
				values.put(DBEPubs.FILENAME, file_name);
				values.put(DBEPubs.FILEPATH, file_path);
				values.put(DBEPubs.SEMAPP_ID, semapp_id);
				values.put(DBEPubs.LOCALPATH, local_path);
//				dbHelper.open();
//				cursor = dbHelper.fetchEPub(id);
				if (cursor.getCount() == 0) {
					FBReaderApp.this.context.getContentResolver().insert(uri, values);
				} else {
					FBReaderApp.this.context.getContentResolver().update(uri, values, selection, null);
				}
//				if (cursor.getCount() == 0) {
//					dbHelper.createEPub(id, name, updated_at, file_name, file_path, semapp_id, local_path);
//				} else {
//					dbHelper.updateEPub(id, name, updated_at, file_name, file_path, semapp_id, local_path);
//				}
				cursor.close();
//				dbHelper.close();
			}
		}).start();
	}
	
	/**
	 * Insert one Annotation of the book into the database or update it 
	 */
	public void writeAnnotationToDatabase(Context context, Annotation annotation, final String eid) {
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
			
			@Override
			public void run() {
				String epub_id;
				
				Uri uri = DBEPubs.CONTENT_URI;
				String[] projection = DBEPubs.Projection;
				String selection = DBEPubs.EPUB_ID + "=\"" + eid + "\"";
				cursor = FBReaderApp.this.context.getContentResolver().query(uri, projection, selection, null, null);
				cursor.moveToFirst();
				if (cursor.getCount() == 0) {
					epub_id = eid;
				} else {
					epub_id = cursor.getString(cursor.getColumnIndex(DBEPubs.EPUB_ID));
				}
				
				uri = DBAnnotations.CONTENT_URI;
				projection = DBAnnotations.Projection;
				selection = DBAnnotations.ANNOTATION_ID + "=\"" + annotation_id + "\"";
				cursor = FBReaderApp.this.context.getContentResolver().query(uri, projection, selection, null, null);
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
					FBReaderApp.this.context.getContentResolver().insert(uri, values);
					uri = DBAuthors.CONTENT_URI;
					projection = DBAuthors.Projection;
					for (TargetAuthor author : authors) {
						values.clear();
						values.put(DBAuthors.NAME, author.getName());
						values.put(DBAuthors.ANNOTATION_ID, annotation_id);
						FBReaderApp.this.context.getContentResolver().insert(uri, values);
					}
				}  if (cursor.getCount() > 0) {
					FBReaderApp.this.context.getContentResolver().update(uri, values, selection, null);
					uri = DBAuthors.CONTENT_URI;
					projection = DBAuthors.Projection;
					selection = DBAuthors.ANNOTATION_ID + "=\"" + annotation_id + "\"";
					for (TargetAuthor author : authors) {
						values.clear();
						values.put(DBAuthors.NAME, author.getName());
						values.put(DBAuthors.ANNOTATION_ID, annotation_id);
						FBReaderApp.this.context.getContentResolver().update(uri, values, selection, null);
					}
				}
				cursor.close();
			}
		}).start();
	}
	
	public String md5(String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	/**
	 * Return all annotations by the given category
	 */
	public ArrayList<Annotation> getAnnotationsByCategory(String category) {
		ArrayList<Annotation> annotationList = new ArrayList<Annotation>();
		
		String de = CategoriesDE.indexOf(category) != -1 ? 
				CategoriesDE.get(CategoriesDE.indexOf(category)) : null;
		String en = CategoriesEN.indexOf(category) != -1 ? 
				CategoriesEN.get(CategoriesEN.indexOf(category)) : null;
		
		for (Annotation annotation : Annotations.getAnnotations()) {
			if (annotation.getCategory().equals(de) || annotation.getCategory().equals(en)) {
				annotationList.add(annotation);
			}
		}
		
		return annotationList;
	}
	
	/**
	 * Return the path to the file with paragraph with index paragraphindex
	 * 
	 * @param paragraphIndex
	 * @return
	 */
	public String getPathToChapterFile(int paragraphIndex) {
		ZLTextModel textModel = BookTextView.getModel();
		
		int thisParagraphIndex;
		int nextParagraphIndex;
		
		for (int i = 0; i < myHtmlFileNames.size(); i++) {
			thisParagraphIndex = myParagraphIndexList.get(i);
			nextParagraphIndex = (i+1 < myHtmlFileNames.size() ? 
					myParagraphIndexList.get(i+1) : textModel.getParagraphsNumber());
			
			if (thisParagraphIndex <= paragraphIndex && paragraphIndex < nextParagraphIndex) {
				return myHtmlFileNames.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * computes the charOffset from the beginning of the paragraph to the selected element
	 * 
	 * @param cursor
	 * @param elementIndex
	 * @param isSelectionStartElement
	 * @return
	 */
	public int computeCharOffset(ZLTextParagraphCursor startCursor, ZLTextParagraphCursor elementCursor, int elementIndex, boolean isSelectionStartElement) {
		ZLTextElement element = elementCursor.getElement(elementIndex);
		ZLTextElement startelement = null;
		for (int i = 0; i < startCursor.getParagraphLength(); i++) {
			if (startCursor.getElement(i) instanceof ZLTextWord) {
				startelement = startCursor.getElement(i);
				break;
			}
		}
		int charOffset = 0;
		
		if (startelement instanceof ZLTextWord && element instanceof ZLTextWord) {
			if (isSelectionStartElement) {
				charOffset = ((ZLTextWord)element).Offset - ((ZLTextWord)startelement).Offset;
			} else {
				charOffset = ((ZLTextWord)element).Offset - ((ZLTextWord)startelement).Offset + ((ZLTextWord)element).Length;
			}
		}
		
		return charOffset;
	}
	
	/**
	 * Return tree with paragraph index paragraphindex
	 * 
	 * @param paragraphIndex
	 * @return
	 */
	public TOCTree getTreeByParagraphIndex(int paragraphIndex) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		ZLTextModel textModel = fbreader.BookTextView.getModel();
		
		String name = "";
		String path = "";
		List<TOCTree> trees = fbreader.Model.TOCTree.subTrees();
		
		int thisParagraphIndex;
		int nextParagraphIndex;
		
		for (int i = 0; i < trees.size(); i++) {
			TOCTree tree = trees.get(i);
			thisParagraphIndex = tree.getReference().ParagraphIndex;
			nextParagraphIndex = (i+1 < trees.size()) ? trees.get(i+1).getReference().ParagraphIndex : textModel.getParagraphsNumber();
			
			if (thisParagraphIndex <= paragraphIndex && paragraphIndex < nextParagraphIndex) {
				return tree;
			}
		}
		
		return null;
	}

	private ColorProfile myColorProfile;

	public ColorProfile getColorProfile() {
		if (myColorProfile == null) {
			myColorProfile = ColorProfile.get(getColorProfileName());
		}
		return myColorProfile;
	}

	public String getColorProfileName() {
		return ColorProfileOption.getValue();
	}

	public void setColorProfileName(String name) {
		ColorProfileOption.setValue(name);
		myColorProfile = null;
	}

	@Override
	public ZLKeyBindings keyBindings() {
		return myBindings;
	}

	public FBView getTextView() {
		return (FBView)getCurrentView();
	}

	public void tryOpenFootnote(String id) {
		if (Model != null) {
			BookModel.Label label = Model.getLabel(id);
			if (label != null) {
				addInvisibleBookmark();
				if (label.ModelId == null) {
					BookTextView.gotoPosition(label.ParagraphIndex, 0, 0);
				} else {
					FootnoteView.setModel(Model.getFootnoteModel(label.ModelId));
					setView(FootnoteView);
					FootnoteView.gotoPosition(label.ParagraphIndex, 0, 0);
				}
				getViewWidget().repaint();
			}
		}
	}

	public void clearTextCaches() {
		BookTextView.clearCaches();
		FootnoteView.clearCaches();
	}

	void openBookInternal(Book book, Bookmark bookmark) {
		if (book != null) {
			onViewChanged();

			if (Model != null) {
				Model.Book.storePosition(BookTextView.getStartCursor());
			}
			BookTextView.setModel(null);
			FootnoteView.setModel(null);
			clearTextCaches();

			Model = null;
			System.gc();
			System.gc();
			Model = BookModel.createModel(book);
			if (Model != null) {
				ZLTextHyphenator.Instance().load(book.getLanguage());
				BookTextView.setModel(Model.BookTextModel);
				BookTextView.gotoPosition(book.getStoredPosition());
				if (bookmark == null) {
					setView(BookTextView);
				} else {
					gotoBookmark(bookmark);
				}
				Library.addBookToRecentList(book);
				final StringBuilder title = new StringBuilder(book.getTitle());
				if (!book.authors().isEmpty()) {
					boolean first = true;
					for (Author a : book.authors()) {
						title.append(first ? " (" : ", ");
						title.append(a.DisplayName);
						first = false;
					}
					title.append(")");
				}
				setTitle(title.toString());
				
				loadAnnotationHighlighting();
			}
		}
		getViewWidget().repaint();
	}
	
	/**
	 * load the XML structure into the annotations object structure
	 */
	public void loadFromXMLFile() {
		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		
		try {
    		Serializer serializer = new Persister();
    		String path = Paths.BooksDirectoryOption().getValue()+"/annotations.xml";
    		File annotationXML = new File(path);
    		fbReader.Annotations = serializer.read(Annotations.class, annotationXML);
    	} catch (Exception e) {
    		Log.e("loadFromXMLFile", e.toString());
    	}
	}
	
	/**
	 * load an XML String of annotations into the annotations object structure
	 * @param xml
	 */
	public void loadAnnotationsFromXMLString(String xml) {
		try {
    		Serializer serializer = new Persister();
    		Annotations = serializer.read(Annotations.class, xml);
    	} catch (Exception e) {
    		Log.e("loadFromXMLString", e.toString());
    	}
	}
	
	/**
	 * reads the xpath and the charoffset of each annotation and convert it to fbreader positions
	 */
	public void loadAnnotationHighlighting() {
		ZLTextModel textModel = BookTextView.getModel();
		ArrayList<Annotation> annotations = Annotations.getAnnotations();
		
		String startPart;
		String startXPath;
		String endPart;
		String endXPath;
		int startCharOffset;
		int endCharOffset;
		
		for (Annotation a : annotations) {
			startPart = a.getAnnotationTarget().getRange().getStart().getPart();
			startXPath = a.getAnnotationTarget().getRange().getStart().getPath().getXPath();
			startCharOffset = a.getAnnotationTarget().getRange().getStart().getPath().getCharOffset();
			
			endPart = a.getAnnotationTarget().getRange().getEnd().getPart();
			endXPath = a.getAnnotationTarget().getRange().getEnd().getPath().getXPath();
			endCharOffset = a.getAnnotationTarget().getRange().getEnd().getPath().getCharOffset();
			
			if (!myHtmlFileNames.contains(startPart)) {
				continue;
			}
			
			int[] startData = computeParagraphData(startPart, startXPath, startCharOffset);
			int[] endData = computeParagraphData(endPart, endXPath, endCharOffset);
			
			ZLTextPosition start = new ZLTextFixedPosition(startData[0], startData[1], 0);
			ZLTextPosition end = new ZLTextFixedPosition(endData[0], endData[1]+1, 0);
			ZLColor color = new ZLColor(a.getRenderingInfo().getHighlightColor());
			BookTextView.addAnnotationHighlight(start, end, color, true, a);
		}
		BookTextView.repaintAll();
	}
	
	/**
	 * computes the paragraph index and the element index of the given text 
	 */
	private int[] computeParagraphData(String part, String xPath, int charOffset) {
		ZLTextModel textModel = BookTextView.getModel();
		
		Matcher matcher = Pattern.compile( "\\d+" ).matcher(xPath);
		int tagIndex = 0;
		while ( matcher.find() ) {
			tagIndex = new Integer(matcher.group());
		}
		String xPathShort = xPath.substring(0, xPath.indexOf("[" + tagIndex + "]"));
		
		int startIndex = myHtmlFileNames.indexOf(part);
		int thisFileParagraphStartIndex = myParagraphIndexList.get(startIndex);
		int thisFileParagraphEndIndex = myParagraphIndexList.size() > startIndex + 1 ? 
			myParagraphIndexList.get(startIndex + 1) - 1 :
			Model.BookTextModel.getParagraphsNumber();
		
		ArrayList<Integer> possibleIndexList = textModel.getIndexByXPathInRange(xPathShort, thisFileParagraphStartIndex, thisFileParagraphEndIndex);
		ZLTextParagraphCursor tmpCursor = null;
		for (int i =0; i < possibleIndexList.size(); i++) {
			if (textModel.getParagraphTagCount(possibleIndexList.get(i)) == tagIndex) {
				tmpCursor = ZLTextParagraphCursor.cursor(textModel, possibleIndexList.get(i));
				break;
			}
		}
		ZLTextElement startElement = null;
		for (int i = 0; i < tmpCursor.getParagraphLength(); i++) {
			if (tmpCursor.getElement(i) instanceof ZLTextWord) {
				startElement = tmpCursor.getElement(i);
				break;
			}
		}
		int absoluteElementCharOffset = charOffset + ((ZLTextWord)startElement).Offset;
		
		return tmpCursor.getIndexesByCharOffset(absoluteElementCharOffset);
	}


	public void gotoBookmark(Bookmark bookmark) {
		addInvisibleBookmark();
		final String modelId = bookmark.ModelId;
		if (modelId == null) {
			BookTextView.gotoPosition(bookmark);
			setView(BookTextView);
		} else {
			FootnoteView.setModel(Model.getFootnoteModel(modelId));
			FootnoteView.gotoPosition(bookmark);
			setView(FootnoteView);
		}
		getViewWidget().repaint();
	}

	public void showBookTextView() {
		setView(BookTextView);
	}

	private Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = Book.getByFile(file);
		if (book != null) {
			book.insertIntoBookList();
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = Book.getByFile(child);
				if (book != null) {
					book.insertIntoBookList();
					return book;
				}
			}
		}
		return null;
	}

	@Override
	public void openFile(ZLFile file) {
		final Book book = createBookForFile(file);
		if (book != null) {
			openBook(book, null);
		}
	}

	@Override
	public void onWindowClosing() {
		if (Model != null && BookTextView != null) {
			Model.Book.storePosition(BookTextView.getStartCursor());
		}
	}

	static enum CancelActionType {
		previousBook,
		returnTo,
		close
	}

	public static class CancelActionDescription {
		final CancelActionType Type;
		public final String Title;
		public final String Summary;

		CancelActionDescription(CancelActionType type, String summary) {
			final ZLResource resource = ZLResource.resource("cancelMenu");
			Type = type;
			Title = resource.getResource(type.toString()).getValue();
			Summary = summary;
		}
	}

	private static class BookmarkDescription extends CancelActionDescription {
		final Bookmark Bookmark;
		
		BookmarkDescription(Bookmark b) {
			super(CancelActionType.returnTo, b.getText());
			Bookmark = b;
		}
	}

	private final ArrayList<CancelActionDescription> myCancelActionsList =
		new ArrayList<CancelActionDescription>();

	public List<CancelActionDescription> getCancelActionsList() {
		myCancelActionsList.clear();
		if (ShowPreviousBookInCancelMenuOption.getValue()) {
			final Book previousBook = Library.getPreviousBook();
			if (previousBook != null) {
				myCancelActionsList.add(new CancelActionDescription(
					CancelActionType.previousBook, previousBook.getTitle()
				));
			}
		}
		if (ShowPositionsInCancelMenuOption.getValue()) {
			if (Model != null && Model.Book != null) {
				for (Bookmark bookmark : Bookmark.invisibleBookmarks(Model.Book)) {
					myCancelActionsList.add(new BookmarkDescription(bookmark));
				}
			}
		}
		myCancelActionsList.add(new CancelActionDescription(
			CancelActionType.close, null
		));
		return myCancelActionsList;
	}

	public void runCancelAction(int index) {
		if (index < 0 || index >= myCancelActionsList.size()) {
			return;
		}

		final CancelActionDescription description = myCancelActionsList.get(index);
		switch (description.Type) {
			case previousBook:
				openBook(Library.getPreviousBook(), null);
				break;
			case returnTo:
			{
				final Bookmark b = ((BookmarkDescription)description).Bookmark;
				b.delete();
				gotoBookmark(b);
				break;
			}
			case close:
				closeWindow();
				break;
		}
	}

	private void updateInvisibleBookmarksList(Bookmark b) {
		if (Model.Book != null && b != null) {
			for (Bookmark bm : Bookmark.invisibleBookmarks(Model.Book)) {
				if (b.equals(bm)) {
					bm.delete();
				}
			}
			b.save();
			final List<Bookmark> bookmarks = Bookmark.invisibleBookmarks(Model.Book);
			for (int i = 3; i < bookmarks.size(); ++i) {
				bookmarks.get(i).delete();
			}
		}
	}

	public void addInvisibleBookmark(ZLTextWordCursor cursor) {
		if (cursor != null && Model != null && Model.Book != null && getTextView() == BookTextView) {
			updateInvisibleBookmarksList(new Bookmark(
				Model.Book,
				getTextView().getModel().getId(),
				cursor,
				6,
				false
			));
		}
	}

	public void addInvisibleBookmark() {
		if (Model.Book != null && getTextView() == BookTextView) {
			updateInvisibleBookmarksList(addBookmark(6, false));
		}
	}

	public Bookmark addBookmark(int maxLength, boolean visible) {
		final FBView view = getTextView();
		final ZLTextWordCursor cursor = view.getStartCursor();

		if (cursor.isNull()) {
			return null;
		}

		return new Bookmark(
			Model.Book,
			view.getModel().getId(),
			cursor,
			maxLength,
			visible
		);
	}

	public TOCTree getCurrentTOCElement() {
		final ZLTextWordCursor cursor = BookTextView.getStartCursor();
		if (Model == null || cursor == null) {
			return null;
		}

		int index = cursor.getParagraphIndex();	
		if (cursor.isEndOfParagraph()) {
			++index;
		}
		TOCTree treeToSelect = null;
		for (TOCTree tree : Model.TOCTree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference == null) {
				continue;
			}
			if (reference.ParagraphIndex > index) {
				break;
			}
			treeToSelect = tree;
		}
		return treeToSelect;
	}
}
