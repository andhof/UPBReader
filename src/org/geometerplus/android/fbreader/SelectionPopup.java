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

package org.geometerplus.android.fbreader;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import de.upb.android.reader.R;

class SelectionPopup extends ButtonsPopupPanel {
	final static String ID = "SelectionPopup";
	
	SelectionPopup(FBReaderApp fbReader) {
		super(fbReader);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void createControlPanel(FBReader activity, RelativeLayout root, PopupWindow.Location location) {
		if (myWindow != null && activity == myWindow.getActivity()) {
			return;
		}

		myWindow = new PopupWindow(activity, root, location, false);
		
        addButton(ActionCode.SELECTION_COPY_TO_CLIPBOARD, true, R.drawable.selection_copy);
        addButton(ActionCode.SELECTION_SHARE, true, R.drawable.selection_share);
        addButton(ActionCode.SELECTION_TRANSLATE, true, R.drawable.selection_translate);
        addButton(ActionCode.SELECTION_BOOKMARK, true, R.drawable.selection_bookmark);
        addButton(ActionCode.SELECTION_CLEAR, true, R.drawable.selection_close);
        
      //################################################
//        ActionItem nextItem	= new ActionItem(ID_DOWN, "kopieren");
//		ActionItem prevItem	= new ActionItem(ID_UP, "senden");
//        ActionItem searchItem	= new ActionItem(ID_SEARCH, "übersetzen");
//        ActionItem infoItem = new ActionItem(ID_INFO, "Lesezeichen setzen");
//        ActionItem eraseItem = new ActionItem(ID_ERASE, "Notiz");
//        ActionItem okItem = new ActionItem(ID_OK, "abbrechen");
//        
//        prevItem.setSticky(true);
//        nextItem.setSticky(true);
//        
//        final QuickAction quickAction = new QuickAction(activity);
//        
//        quickAction.addActionItem(nextItem);
//		quickAction.addActionItem(prevItem);
//        quickAction.addActionItem(searchItem);
//        quickAction.addActionItem(infoItem);
//        quickAction.addActionItem(eraseItem);
//        quickAction.addActionItem(okItem);
      //################################################
    }
    
	/**
	 * Look, if selected text is on top part of the screen. Then draw the icon bar on the bottom.
	 * 
	 * @param selectionStartY
	 * @param selectionEndY
	 */
    public void move(int selectionStartY, int selectionEndY) {
		if (myWindow == null) {
			return;
		}

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
			LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
		);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        final int verticalPosition; 
        final int screenHeight = ((View)myWindow.getParent()).getHeight();
		final int diffTop = screenHeight - selectionEndY;
		final int diffBottom = selectionStartY;
		if (diffTop > diffBottom) {
			verticalPosition = diffTop > myWindow.getHeight() + 20
				? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.CENTER_VERTICAL;
		} else {
			verticalPosition = diffBottom > myWindow.getHeight() + 20
				? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.CENTER_VERTICAL;
		}

        layoutParams.addRule(verticalPosition);
        myWindow.setLayoutParams(layoutParams);
    }
}
