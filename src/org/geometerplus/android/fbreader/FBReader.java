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

package org.geometerplus.android.fbreader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.geometerplus.android.fbreader.annotation.database.AnnotationsDbAdapter;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.library.KillerCallback;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.quickactionbar.AnnotationListItem;
import org.geometerplus.android.fbreader.quickactionbar.AnnotationListPopup;
import org.geometerplus.android.fbreader.quickactionbar.QuickActionBar;
import org.geometerplus.android.fbreader.quickactionbar.QuickActionItem;
import org.geometerplus.android.fbreader.tips.TipsActivity;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.fbreader.tips.TipsManager;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;

import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;

import org.geometerplus.zlibrary.ui.android.library.*;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.tips.TipsManager;

import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.library.KillerCallback;
import org.geometerplus.android.fbreader.api.*;
import org.geometerplus.android.fbreader.tips.TipsActivity;

import org.geometerplus.android.util.UIUtil;

import de.upb.android.reader.R;

public final class FBReader extends ZLAndroidActivity {
	static final int ACTION_BAR_COLOR = Color.BLACK;
	
	public static final String BOOK_PATH_KEY = "BookPath";

	final static int REPAINT_CODE = 1;
	final static int CANCEL_CODE = 2;
	final static int ANNOTATION_CODE = 3;
	final static int CLOSE_CODE = 4;

	private AnnotationsDbAdapter dbHelper;
	private Cursor cursor;
	
	private QuickActionBar quickSelectionActionBar;
	private QuickActionBar quickAnnotationActionBar;
	private AnnotationListPopup annotationListPopup;
	private ZLResource myResource;
	private Annotation myAnnotation;
	
	QuickActionItem copy;
	QuickActionItem send;
	QuickActionItem dict;
	QuickActionItem bookmark;
	QuickActionItem highlight;
	QuickActionItem note;
	QuickActionItem cancel;
	QuickActionItem show;
	QuickActionItem edit;
	QuickActionItem comment;
	QuickActionItem remove;

	private boolean myFullScreenFlag;

	private static final String PLUGIN_ACTION_PREFIX = "___";
	private final List<PluginApi.ActionInfo> myPluginActions =
		new LinkedList<PluginApi.ActionInfo>();
	private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(true).<PluginApi.ActionInfo>getParcelableArrayList(PluginApi.PluginInfo.KEY);
			if (actions != null) {
				synchronized (myPluginActions) {
					final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
					int index = 0;
					while (index < myPluginActions.size()) {
						fbReader.removeAction(PLUGIN_ACTION_PREFIX + index++);
					}
					myPluginActions.addAll(actions);
					index = 0;
					for (PluginApi.ActionInfo info : myPluginActions) {
						fbReader.addAction(
							PLUGIN_ACTION_PREFIX + index++,
							new RunPluginAction(FBReader.this, fbReader, info.getId())
						);
					}
				}
			}
		}
	};
	
	@Override
	protected ZLFile fileFromIntent(Intent intent) {
		String filePath = intent.getStringExtra(BOOK_PATH_KEY);
		if (filePath == null) {
			final Uri data = intent.getData();
			if (data != null) {
				filePath = data.getPath();
			}
		}
		return filePath != null ? ZLFile.createFileByPath(filePath) : null;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
		myFullScreenFlag = !zlibrary.ShowStatusBarOption.getValue();

		final ActionBar bar = getActionBar();
		bar.setDisplayOptions(
			ActionBar.DISPLAY_SHOW_CUSTOM,
			ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE
		);
		final TextView titleView = (TextView)getLayoutInflater().inflate(R.layout.title_view, null);
		titleView.setText(getTitle());
		titleView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				fbReader.runAction(ActionCode.SHOW_BOOK_INFO);
			}
		});
		bar.setCustomView(titleView);
		bar.setBackgroundDrawable(new ColorDrawable(ACTION_BAR_COLOR));

		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		);

		if (fbReader.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(fbReader);
		}
//		if (fbReader.getPopupById(SelectionPopup.ID) == null) {
//			new SelectionPopup(fbReader);
//		}

		fbReader.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_UPB_LOGIN_SCREEN, new ShowUPBLibraryLoginAction(this, fbReader));
		
		fbReader.addAction(ActionCode.REFRESH_ANNOTATIONS, new RefreshAnnotationsAction(this, fbReader));
		
//		fbReader.addAction(ActionCode.SHOW_SEMAPPS_LIST, new ShowSemAppsListAction(this, fbReader));
//		fbReader.addAction(ActionCode.SHOW_EPUB_LIST, new ShowEPubListAction(this, fbReader));
		
		fbReader.addAction(ActionCode.TOGGLE_BARS, new ToggleBarsAction(this, fbReader));
		fbReader.addAction(ActionCode.SEARCH, new SearchAction(this, fbReader));

		fbReader.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, fbReader));
//		fbReader.addAction(ActionCode.SELECTION_HIGHLIGHT, new SelectionHighlightAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_NOTE, new SelectionNoteAction(this, fbReader));
		
//		fbReader.addAction(ActionCode.SELECTION_EDIT_ANNOTATION, new SelectionNoteAction(this, fbReader));
//		fbReader.addAction(ActionCode.SELECTION_EDIT_ANNOTATION, new SelectionNoteAction(this, fbReader));
//		fbReader.addAction(ActionCode.SELECTION_EDIT_ANNOTATION, new SelectionNoteAction(this, fbReader));
		
		fbReader.addAction(ActionCode.SELECTION_SHOW_ANNOTATION, new SelectionShowAnnotationAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_SHOW_ANNOTATION_PANEL, new SelectionShowAnnotationPanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_SHOW_ANNOTATION_LIST, new ShowAnnotationListAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_LIST_ELEMENT_HIGHLIGHT, new SelectionHighlightAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_LIST_ELEMENT_NOTE, new SelectionNoteAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_REMOVE_ANNOTATION, new SelectionRemoveAnnotationAction(this, fbReader));

		fbReader.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, fbReader));

		fbReader.addAction(ActionCode.SHOW_CANCEL_MENU, new ShowCancelMenuAction(this, fbReader));

		fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}
	}

//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
//		if (!application.ShowStatusBarOption.getValue() &&
//			application.ShowStatusBarWhenMenuIsActiveOption.getValue()) {
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//		}
//		return super.onPrepareOptionsMenu(menu);
//	}
//
//	@Override
//	public void onOptionsMenuClosed(Menu menu) {
//		super.onOptionsMenuClosed(menu);
//		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
//		if (!application.ShowStatusBarOption.getValue() &&
//			application.ShowStatusBarWhenMenuIsActiveOption.getValue()) {
//			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//		}
//	}

	@Override
	protected void onNewIntent(Intent intent) {
		final Uri data = intent.getData();
		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())
					&& data != null && "fbreader-action".equals(data.getScheme())) {
			fbReader.doAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Runnable runnable = new Runnable() {
				public void run() {
					final TextSearchPopup popup = (TextSearchPopup)fbReader.getPopupById(TextSearchPopup.ID);
					popup.initPosition();
					fbReader.TextSearchPatternOption.setValue(pattern);
					if (fbReader.getTextView().search(pattern, true, false, false, false) != 0) {
						runOnUiThread(new Runnable() {
							public void run() {
								fbReader.showPopup(popup.getId());
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								UIUtil.showErrorMessage(FBReader.this, "textNotFound");
								popup.StartPosition = null;
							}
						});
					}
				}
			};
			UIUtil.wait("search", runnable, this);
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();

		final boolean fullScreenFlag = !zlibrary.ShowStatusBarOption.getValue();
		if (fullScreenFlag != myFullScreenFlag) {
			finish();
			startActivity(new Intent(this, getClass()));
		}

		SetScreenOrientationAction.setOrientation(this, zlibrary.OrientationOption.getValue());

		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);
		((PopupPanel)fbReader.getPopupById(TextSearchPopup.ID)).createControlPanel(this, root, PopupWindow.Type.Bottom);
//        ((PopupPanel)fbReader.getPopupById(SelectionPopup.ID)).createControlPanel(this, root, PopupWindow.Type.Floating);
		
		
		copy = new QuickActionItem(ActionCode.SELECTION_COPY_TO_CLIPBOARD, true, this.getString(R.string.quickaction_copy));
		send = new QuickActionItem(ActionCode.SELECTION_SHARE, true, this.getString(R.string.quickaction_share));
		dict	= new QuickActionItem(ActionCode.SELECTION_TRANSLATE, true, this.getString(R.string.quickaction_translate));
		bookmark = new QuickActionItem(ActionCode.SELECTION_BOOKMARK, true, this.getString(R.string.quickaction_bookmark));
//		highlight = new QuickActionItem(ActionCode.SELECTION_HIGHLIGHT, true, myResource.getResource("highlight").getValue());
		note = new QuickActionItem(ActionCode.SELECTION_NOTE, true, this.getString(R.string.quickaction_note));
		cancel = new QuickActionItem(ActionCode.SELECTION_CLEAR, true, this.getString(R.string.quickaction_clear));
		
        quickSelectionActionBar = new QuickActionBar(this, fbReader);
        
        quickSelectionActionBar.addQuickActionItem(copy);
        quickSelectionActionBar.addQuickActionItem(send);
        quickSelectionActionBar.addQuickActionItem(dict);
        quickSelectionActionBar.addQuickActionItem(bookmark);
//        quickSelectionActionBar.addQuickActionItem(highlight);
        quickSelectionActionBar.addQuickActionItem(note);
        quickSelectionActionBar.addQuickActionItem(cancel);
        
		quickAnnotationActionBar = new QuickActionBar(this, fbReader);
		
		annotationListPopup = new AnnotationListPopup(this, fbReader);
		
		synchronized (myPluginActions) {
			int index = 0;
			while (index < myPluginActions.size()) {
				fbReader.removeAction(PLUGIN_ACTION_PREFIX + index++);
			}
			myPluginActions.clear();
		}

		sendOrderedBroadcast(
			new Intent(PluginApi.ACTION_REGISTER).addFlags(0x20/*FLAG_INCLUDE_STOPPED_PACKAGES*/),
			null,
			myPluginInfoReceiver,
			null,
			RESULT_OK,
			null,
			null
		);

		final TipsManager manager = TipsManager.Instance();
		switch (manager.requiredAction()) {
			case Initialize:
				startActivity(new Intent(TipsActivity.INITIALIZE_ACTION, null, this, TipsActivity.class));
				break;
			case Show:
				startActivity(new Intent(TipsActivity.SHOW_TIP_ACTION, null, this, TipsActivity.class));
				break;
			case Download:
				manager.startDownloading();
				break;
			case None:
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			sendBroadcast(new Intent(getApplicationContext(), KillerCallback.class));
		} catch (Throwable t) {
		}
		PopupPanel.restoreVisibilities(FBReaderApp.Instance());

		hideBars();

		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
	}

	@Override
	public void onStop() {
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_CLOSED);
		PopupPanel.removeAllWindows(FBReaderApp.Instance(), this);
		super.onStop();
	}

	@Override
	protected FBReaderApp createApplication(ZLFile file) {
		if (BooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase(this, "READER");
		}
		
		return new FBReaderApp(file != null ? file.getPath() : null, this);
	}

	@Override
	public boolean onSearchRequested() {
		hideBars();
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		final FBReaderApp.PopupPanel popup = fbreader.getActivePopup();
		fbreader.hideActivePopup();
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				if (popup != null) {
					fbreader.showPopup(popup.getId());
				}
				manager.setOnCancelListener(null);
			}
		});
		startSearch(fbreader.TextSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	/**
	 * Show the QuickActionBar with the copy, highlight...
	 */
	public void showSelectionPanel() {
		annotationListPopup.dismiss();
		quickAnnotationActionBar.dismiss();
		quickSelectionActionBar.show(this.findViewById(R.id.root_view));
	}
	
	/**
	 * Hide the QuickActionBar
	 */
	public void hideSelectionPanel() {
		quickSelectionActionBar.dismiss();
	}
	
	/**
	 * Show the QuickActionBar for one selected Annotation with the edit, comment...
	 */
	public void showAnnotationSelectionPanel(int x, int y, Annotation annotation) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		myAnnotation = annotation;
		
		ArrayList<String> annotation_ids = new ArrayList<String>();
		annotation_ids.add(annotation.getId());
		
		fbreader.BookTextView.clearSelectionHighlight();
		fbreader.BookTextView.setAnnotationHighlightColors(annotation_ids, true);
		fbreader.BookTextView.repaintAll();
		
		annotationListPopup.dismiss();
		quickAnnotationActionBar.dismiss();
//		quickSelectionActionBar.dismiss();
		
		quickAnnotationActionBar = new QuickActionBar(this, fbreader);
		
		show = new QuickActionItem(ActionCode.SELECTION_SHOW_ANNOTATION, true, this.getString(R.string.quickaction_show));
		edit	= new QuickActionItem(ActionCode.SELECTION_NOTE, true, this.getString(R.string.quickaction_edit));
		
//		comment = new QuickActionItem(ActionCode.SELECTION_COMMENT_ANNOTATION, true, this.getString(R.string.quickaction_comment));
		remove = new QuickActionItem(ActionCode.SELECTION_REMOVE_ANNOTATION, true, this.getString(R.string.quickaction_remove));
		
		quickAnnotationActionBar.addQuickActionItemWithAnnotation(show, annotation);
		quickAnnotationActionBar.addQuickActionItemWithAnnotation(edit, annotation);
//		quickAnnotationActionBar.addQuickActionItemWithAnnotation(comment, annotation);
		quickAnnotationActionBar.addQuickActionItemWithAnnotation(remove, annotation);
		quickAnnotationActionBar.addQuickActionItemWithAnnotation(cancel, annotation);
		
        quickAnnotationActionBar.show(this.findViewById(R.id.root_view), x, y);
	}
	
	/**
	 * Hide the QuickActionBar for one selected Annotation
	 */
	public void hideAnnotationSelectionPanel() {
		quickAnnotationActionBar.dismiss();
	}
	
	/**
	 * Show the List for more than one selectable Annotations
	 */
	public void showAnnotationListPanel(int x, int y, ArrayList<Annotation> annotationsOnPosition) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		ArrayList<String> annotation_ids = new ArrayList<String>();
		annotationListPopup.dismiss();
//		quickSelectionActionBar.dismiss();
		quickAnnotationActionBar.dismiss();
		annotationListPopup = new AnnotationListPopup(this, fbreader);
		AnnotationListItem aItem;
		
		int i = 0;
		for (Annotation annotation : annotationsOnPosition) {
			annotation_ids.add(annotation.getId());
			i++;
			Log.v("FBReader", "Ein Item hinzugefügt");
			aItem	= new AnnotationListItem(ActionCode.SELECTION_SHOW_ANNOTATION_PANEL, true, "#"+i);
			
			annotationListPopup.addQuickActionItem(aItem, x, y, annotation);
		}
		fbreader.BookTextView.clearSelectionHighlight();
		fbreader.BookTextView.setAnnotationHighlightColors(annotation_ids, true);
		fbreader.BookTextView.repaintAll();
		
		annotationListPopup.show(this.findViewById(R.id.root_view), x, y, annotationsOnPosition);
		
		annotationListPopup.removeAllActionItems();
	}
	
	
	
	/**
	 * Original with small box, no quickactionbar
	 */
//	public void showSelectionPanel() {
//		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
//		final ZLTextView view = fbReader.getTextView();
//		((SelectionPopup)fbReader.getPopupById(SelectionPopup.ID))
//			.move(view.getSelectionStartY(), view.getSelectionEndY());
//		fbReader.showPopup(SelectionPopup.ID);
//	}

//	public void hideSelectionPanel() {
//		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
//		final FBReaderApp.PopupPanel popup = fbReader.getActivePopup();
//		if (popup != null && popup.getId() == SelectionPopup.ID) {
//			ZLApplication.Instance().hideActivePopup();
//		}
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		switch (requestCode) {
			case REPAINT_CODE:
			{
				final BookModel model = fbreader.Model;
				if (model != null) {
					final Book book = model.Book;
					if (book != null) {
						book.reloadInfoFromDatabase();
						ZLTextHyphenator.Instance().load(book.getLanguage());
					}
				}
				fbreader.clearTextCaches();
				fbreader.getViewWidget().repaint();
				break;
			}
			case CANCEL_CODE:
				fbreader.runCancelAction(resultCode - 1);
				break;
		}
		
		switch (resultCode) {
			case ANNOTATION_CODE:
				Bundle bundle = data.getExtras();
	        	myAnnotation.getAnnotationContent().setAnnotationText(bundle.getString("content"));
	        	myAnnotation.setTags(bundle.getStringArrayList("tags"));
	        	myAnnotation.setCategory(bundle.getString("category"));
	            myAnnotation.setModified(bundle.getLong("modified"));
	            break;
		}
	}

	private Menu addSubMenu(Menu menu, String id) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		return application.myMainWindow.addSubMenu(menu, id);
	}

	private void addMenuItem(Menu menu, String actionId, String name) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, null, name);
	}

	private void addMenuItem(Menu menu, String actionId, int iconId) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, iconId, null);
	}

	private void addMenuItem(Menu menu, String actionId) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library);
		addMenuItem(menu, ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary);
		addMenuItem(menu, ActionCode.SHOW_UPB_LOGIN_SCREEN, R.drawable.ic_menu_upblibrary);
		addMenuItem(menu, ActionCode.SHOW_TOC, R.drawable.ic_menu_toc);
		addMenuItem(menu, ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks);
		addMenuItem(menu, ActionCode.REFRESH_ANNOTATIONS, getString(R.string.upbrefresh_label));
		addMenuItem(menu, ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night);
		addMenuItem(menu, ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day);
		addMenuItem(menu, ActionCode.SEARCH, R.drawable.ic_menu_search);
		addMenuItem(menu, ActionCode.SHOW_PREFERENCES);
		addMenuItem(menu, ActionCode.SHOW_BOOK_INFO);
		final Menu subMenu = addSubMenu(menu, "screenOrientation");
		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_SYSTEM);
		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT);
		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE);
		if (ZLibrary.Instance().supportsAllOrientations()) {
			addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		}
		addMenuItem(menu, ActionCode.INCREASE_FONT);
		addMenuItem(menu, ActionCode.DECREASE_FONT);
		synchronized (myPluginActions) {
			int index = 0;
			for (PluginApi.ActionInfo info : myPluginActions) {
				if (info instanceof PluginApi.MenuActionInfo) {
					addMenuItem(
						menu,
						PLUGIN_ACTION_PREFIX + index++,
						((PluginApi.MenuActionInfo)info).MenuItemName
					);
				}
			}
		}

		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.refreshMenu();

		return true;
	}

	private NavigationPopup myNavigationPopup;

	boolean barsAreShown() {
		return myNavigationPopup != null;
	}

	void hideBars() {
		if (myNavigationPopup != null) {
			myNavigationPopup.stopNavigation();
			myNavigationPopup = null;
		}

		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
		if (!zlibrary.ShowStatusBarOption.getValue()) {
			getActionBar().hide();
		}

		if (zlibrary.DisableButtonLightsOption.getValue()) {
			findViewById(R.id.root_view).setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		}
	}

	void showBars() {
		final ActionBar bar = getActionBar();
		if (!bar.isShowing()) {
			bar.show();
		}

		final RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);
		root.setSystemUiVisibility(View.STATUS_BAR_VISIBLE);

		if (myNavigationPopup == null) {
			final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
			fbreader.hideActivePopup();
			myNavigationPopup = new NavigationPopup(fbreader);
			myNavigationPopup.runNavigation(this, root);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		final TextView view = (TextView)getActionBar().getCustomView();
		if (view != null) {
			view.setText(title);
			view.postInvalidate();
		}
	}
}
