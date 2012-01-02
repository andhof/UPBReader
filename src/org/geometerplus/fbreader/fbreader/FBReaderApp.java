/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.annotation.model.Annotations;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.library.*;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

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

	public final FBView BookTextView;
	public final FBView FootnoteView;
	public Annotations Annotations;

	public ArrayList<String> HTMLFileNames;

	public BookModel Model;

	private final String myArg0;

	public FBReaderApp(String arg) {
		myArg0 = arg;

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));
		addAction(ActionCode.ROTATE, new RotateAction(this));

		addAction(ActionCode.FIND_NEXT, new FindNextAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new FindPreviousAction(this));
		addAction(ActionCode.CLEAR_FIND_RESULTS, new ClearFindResultsAction(this));

		addAction(ActionCode.SELECTION_CLEAR, new SelectionClearAction(this));

		addAction(ActionCode.TURN_PAGE_FORWARD, new TurnPageAction(this, true));
		addAction(ActionCode.TURN_PAGE_BACK, new TurnPageAction(this, false));

		addAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD, new VolumeKeyTurnPageAction(this, true));
		addAction(ActionCode.VOLUME_KEY_SCROLL_BACK, new VolumeKeyTurnPageAction(this, false));

		addAction(ActionCode.SWITCH_TO_DAY_PROFILE, new SwitchProfileAction(this, ColorProfile.DAY));
		addAction(ActionCode.SWITCH_TO_NIGHT_PROFILE, new SwitchProfileAction(this, ColorProfile.NIGHT));

		addAction(ActionCode.EXIT, new ExitAction(this));

		BookTextView = new FBView(this);
		FootnoteView = new FBView(this);
		Annotations = new Annotations();

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
				openBookInternal(book, bookmark);
			}
		});
	}
	
	public void setHTMLFileNames(final ArrayList<String> htmlFileNames) {
		this.HTMLFileNames = htmlFileNames;
	}
	
	/**
	 * Return the path to the file with paragraph with index paragraphindex
	 * 
	 * @param paragraphIndex
	 * @return
	 */
	public String getPathToChapterFile(int paragraphIndex) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		if (fbreader.Model.TOCTree == null) {
			return "";
		}
		
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
				name = tree.getText();
				path = tree.getPath();
			}
		}
		
		return path;
	}
	
	/**
	 * computes the charOffset from the beginning of the paragraph to the selected element
	 * 
	 * @param cursor
	 * @param elementIndex
	 * @param isSelectionStartElement
	 * @return
	 */
	public int computeCharOffset(ZLTextParagraphCursor cursor, int elementIndex, boolean isSelectionStartElement) {
		final ZLTextElement startelement = cursor.getElement(1);
		final ZLTextElement element = cursor.getElement(elementIndex);
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
				
				loadFromXMLFile();
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
	
//	/**
//	 * load an XML String of annotations into the annotations object structure
//	 * @param xml
//	 */
//	public void loadSemAppFromXMLString(String xml) {
//		try {
//    		Serializer serializer = new Persister();
//    		SemApp = serializer.read(Annotations.class, xml);
//    	} catch (Exception e) {
//    		Log.e("loadFromXMLString", e.toString());
//    	}
//	}
	
	/**
	 * reads the xpath and the charoffset of each annotation and convert it to fbreader positions
	 */
	public void loadAnnotationHighlighting() {
		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		ArrayList<Annotation> annotations = fbReader.Annotations.getAnnotations();
		String startPart;
		String startXPath;
		String endPart;
		String endXPath;
		int startCharOffset;
		int endCharOffset;
		int startParagraphIndexOfChapter;
		int endParagraphIndexOfChapter;
		List<TOCTree> trees;
		int charCount;
		boolean startPartReady;
		boolean endPartReady;
		int startParagraphIndex;
		int startElementIndex;
		int endParagraphIndex;
		int endElementIndex;
		
		for (Annotation a : annotations) {
			startPart = a.getAnnotationTarget().getRange().getStart().getPart();
			startXPath = a.getAnnotationTarget().getRange().getStart().getPath().getXPath();
			startCharOffset = a.getAnnotationTarget().getRange().getStart().getPath().getCharOffset();
			
			endPart = a.getAnnotationTarget().getRange().getEnd().getPart();
			endXPath = a.getAnnotationTarget().getRange().getEnd().getPath().getXPath();
			endCharOffset = a.getAnnotationTarget().getRange().getEnd().getPath().getCharOffset();
			
			trees = fbReader.Model.TOCTree.subTrees();
			
			startParagraphIndex = 0;
			startElementIndex = 0;
			endParagraphIndex = 0;
			endElementIndex = 0;
			startPartReady = false;
			endPartReady = false;
			
			Matcher matcher = Pattern.compile( "\\d+" ).matcher(startXPath);
			startParagraphIndexOfChapter = 0;
			while ( matcher.find() ) {
				startParagraphIndexOfChapter = new Integer(matcher.group());
			}
			matcher = Pattern.compile( "\\d+" ).matcher(endXPath);
			endParagraphIndexOfChapter = 0;
			while ( matcher.find() ) {
				endParagraphIndexOfChapter = new Integer(matcher.group());
			}
			
			
			
			int absoluteParagraphIndex = 0;
			for (TOCTree tree : trees) {
				String path = tree.getPath();
				
//				int maxParagraphNumber = fbReader.Model.BookTextModel.getParagraphsNumber();
				absoluteParagraphIndex = tree.getReference().ParagraphIndex;
				
				ZLTextParagraphCursor cursor = ZLTextParagraphCursor.cursor(fbReader.Model.BookTextModel, absoluteParagraphIndex);
				
				charCount = 0;
				// calculate paragraphIndex and elementIndex of the first element
				if (path.equals(startPart)) {
					startParagraphIndex = absoluteParagraphIndex + startParagraphIndexOfChapter;
					cursor = ZLTextParagraphCursor.cursor(fbReader.Model.BookTextModel, startParagraphIndex);
					int startParagraphCharIndex = 0;
					if (cursor.getElement(1) instanceof ZLTextWord) {
						startParagraphCharIndex = ((ZLTextWord)cursor.getElement(1)).Offset;
					}
					for (int i = 0; i < cursor.getParagraphLength(); i++) {
						ZLTextElement element = cursor.getElement(i);
						if (element instanceof ZLTextWord) {
							charCount = ((ZLTextWord) element).Offset - startParagraphCharIndex;
							if (charCount == startCharOffset) {
								startElementIndex = i;
								startPartReady = true;
								break;
							}
						}
					}
				}
				
				charCount = 0;
				// calculate paragraphIndex and elementIndex of the last element
				if (path.equals(endPart)) {
					endParagraphIndex = absoluteParagraphIndex + endParagraphIndexOfChapter;
					cursor = ZLTextParagraphCursor.cursor(fbReader.Model.BookTextModel, endParagraphIndex);
					int endParagraphCharIndex = 0;
					if (cursor.getElement(1) instanceof ZLTextWord) {
						endParagraphCharIndex = ((ZLTextWord)cursor.getElement(1)).Offset;
					}
					for (int i = 0; i < cursor.getParagraphLength(); i++) {
						ZLTextElement element = cursor.getElement(i);
						if (element instanceof ZLTextWord) {
							charCount = ((ZLTextWord) element).Offset - endParagraphCharIndex + ((ZLTextWord) element).Length;
							if (charCount == endCharOffset) {
								endElementIndex = i;
								endPartReady = true;
								break;
							}
						}
					}
				}
				// add the highlight of this annotation
				if (startPartReady && endPartReady) {
					ZLTextPosition start = new ZLTextFixedPosition(startParagraphIndex, startElementIndex, 0);
					ZLTextPosition end = new ZLTextFixedPosition(endParagraphIndex, endElementIndex, 0);
					ZLColor color = new ZLColor(a.getRenderingInfo().getHighlightColor());
					fbReader.BookTextView.addAnnotationHighlight(start, end, color, true, a);
					break;
				}
			}
		}
		fbReader.BookTextView.repaintAll();
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
}
