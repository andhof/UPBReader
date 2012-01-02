/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.android.fbreader.annotation.AnnotationListItem;
import org.geometerplus.android.fbreader.annotation.AnnotationListPopup;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.library.KillerCallback;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
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

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import de.upb.android.reader.R;

public final class FBReader extends ZLAndroidActivity {
	public static final String BOOK_PATH_KEY = "BookPath";

	final static int REPAINT_CODE = 1;
	final static int CANCEL_CODE = 2;
	final static int ANNOTATION_CODE = 3;
	final static int CLOSE_CODE = 4;
	
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

	private int myFullScreenFlag;

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
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		myFullScreenFlag =
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN, myFullScreenFlag
		);

		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		if (fbReader.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(fbReader);
		}
		if (fbReader.getPopupById(NavigationPopup.ID) == null) {
			new NavigationPopup(fbReader);
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
		
//		fbReader.addAction(ActionCode.SHOW_SEMAPPS_LIST, new ShowSemAppsListAction(this, fbReader));
//		fbReader.addAction(ActionCode.SHOW_EPUB_LIST, new ShowEPubListAction(this, fbReader));
		
		fbReader.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, fbReader));
		fbReader.addAction(ActionCode.SEARCH, new SearchAction(this, fbReader));

		fbReader.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_HIGHLIGHT, new SelectionHighlightAction(this, fbReader));
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

		final TipsManager manager = TipsManager.Instance();
		if (manager.tipShouldBeShown()) {
			startActivity(new Intent(this, TipsActivity.class));
		} else if (manager.tipsShouldBeDownloaded()) {
			manager.startDownloading();
		}
	}
	
 	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		if (!application.ShowStatusBarOption.getValue() &&
			application.ShowStatusBarWhenMenuIsActiveOption.getValue()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		if (!application.ShowStatusBarOption.getValue() &&
			application.ShowStatusBarWhenMenuIsActiveOption.getValue()) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
	}

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
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		
		final int fullScreenFlag =
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (fullScreenFlag != myFullScreenFlag) {
			finish();
			startActivity(new Intent(this, getClass()));
		}

		final RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);
		((PopupPanel)fbReader.getPopupById(TextSearchPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Bottom);
		((PopupPanel)fbReader.getPopupById(NavigationPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Bottom);
//		((PopupPanel)fbReader.getPopupById(SelectionPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Floating);
		
		
		myResource = ZLResource.resource("dialog").getResource("QuickActionBar");
		copy = new QuickActionItem(ActionCode.SELECTION_COPY_TO_CLIPBOARD, true, myResource.getResource("copy").getValue());
		send = new QuickActionItem(ActionCode.SELECTION_SHARE, true, myResource.getResource("share").getValue());
		dict	= new QuickActionItem(ActionCode.SELECTION_TRANSLATE, true, myResource.getResource("translate").getValue());
		bookmark = new QuickActionItem(ActionCode.SELECTION_BOOKMARK, true, myResource.getResource("bookmark").getValue());
//		highlight = new QuickActionItem(ActionCode.SELECTION_HIGHLIGHT, true, myResource.getResource("highlight").getValue());
		note = new QuickActionItem(ActionCode.SELECTION_NOTE, true, myResource.getResource("note").getValue());
		cancel = new QuickActionItem(ActionCode.SELECTION_CLEAR, true, myResource.getResource("clear").getValue());
		
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
			new Intent(PluginApi.ACTION_REGISTER),
			null,
			myPluginInfoReceiver,
			null,
			RESULT_OK,
			null,
			null
		);
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			sendBroadcast(new Intent(getApplicationContext(), KillerCallback.class));
		} catch (Throwable t) {
		}
		PopupPanel.restoreVisibilities(ZLApplication.Instance());
	}

	@Override
	public void onStop() {
		PopupPanel.removeAllWindows(ZLApplication.Instance(), this);
		super.onStop();
	}

	@Override
	protected FBReaderApp createApplication(ZLFile file) {
		if (BooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase(this, "READER");
		}
		return new FBReaderApp(file != null ? file.getPath() : null);
	}

	@Override
	public boolean onSearchRequested() {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
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
		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		myAnnotation = annotation;
		
		annotationListPopup.dismiss();
		quickAnnotationActionBar.dismiss();
//		quickSelectionActionBar.dismiss();
		
		quickAnnotationActionBar = new QuickActionBar(this, fbReader);
		
		myResource = ZLResource.resource("dialog").getResource("QuickActionBar");
		
		show = new QuickActionItem(ActionCode.SELECTION_SHOW_ANNOTATION, true, myResource.getResource("edit").getValue());
		edit	= new QuickActionItem(ActionCode.SELECTION_NOTE, true, myResource.getResource("edit").getValue());
		
		comment = new QuickActionItem(ActionCode.SELECTION_COMMENT_ANNOTATION, true, myResource.getResource("comment").getValue());
		remove = new QuickActionItem(ActionCode.SELECTION_REMOVE_ANNOTATION, true, myResource.getResource("remove").getValue());
		
		quickAnnotationActionBar.addQuickActionItemWithAnnotation(show, annotation);
		quickAnnotationActionBar.addQuickActionItemWithAnnotation(edit, annotation);
		quickAnnotationActionBar.addQuickActionItemWithAnnotation(comment, annotation);
		quickAnnotationActionBar.addQuickActionItemWithAnnotation(remove, annotation);
		quickAnnotationActionBar.addQuickActionItem(cancel);
		
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
		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		annotationListPopup.dismiss();
//		quickSelectionActionBar.dismiss();
		quickAnnotationActionBar.dismiss();
		annotationListPopup = new AnnotationListPopup(this, fbReader);
		AnnotationListItem aItem;
		
		for (Annotation annotation : annotationsOnPosition) {
			Log.v("FBReader", "Ein Item hinzugefügt");
			aItem	= new AnnotationListItem(ActionCode.SELECTION_SHOW_ANNOTATION_PANEL, true, myResource.getResource("copy").getValue());
			
			annotationListPopup.addQuickActionItem(aItem, x, y, annotation);
		}
		
		
		
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

	public void navigate() {
		((NavigationPopup)ZLApplication.Instance().getPopupById(NavigationPopup.ID)).runNavigation();
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
		addMenuItem(menu, ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night);
		addMenuItem(menu, ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day);
		addMenuItem(menu, ActionCode.SEARCH, R.drawable.ic_menu_search);
		addMenuItem(menu, ActionCode.SHOW_PREFERENCES);
		addMenuItem(menu, ActionCode.SHOW_BOOK_INFO);
		addMenuItem(menu, ActionCode.ROTATE);
		addMenuItem(menu, ActionCode.INCREASE_FONT);
		addMenuItem(menu, ActionCode.DECREASE_FONT);
		addMenuItem(menu, ActionCode.SHOW_NAVIGATION);
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
}
