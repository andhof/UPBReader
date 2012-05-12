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

package org.geometerplus.zlibrary.text.model;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.html.HtmlTag;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.image.ZLImageMap;

import android.util.Log;

public final class ZLTextWritablePlainModel extends ZLTextPlainModel implements ZLTextWritableModel {
	private char[] myCurrentDataBlock;
	private int myBlockOffset;
	
	private int[] tagcount = new int[35];
	
	public ZLTextWritablePlainModel(String id, String language, int arraySize, int dataBlockSize, String directoryName, String extension, ZLImageMap imageMap) {
		super(id, language, arraySize, dataBlockSize, directoryName, extension, imageMap);
	}

	private void extend() {
		final int size = myStartEntryIndices.length;
		myStartEntryIndices = ZLArrayUtils.createCopy(myStartEntryIndices, size, size << 1);
		myStartEntryOffsets = ZLArrayUtils.createCopy(myStartEntryOffsets, size, size << 1);
		myParagraphLengths = ZLArrayUtils.createCopy(myParagraphLengths, size, size << 1);
		myTextSizes = ZLArrayUtils.createCopy(myTextSizes, size, size << 1);
		myParagraphKinds = ZLArrayUtils.createCopy(myParagraphKinds, size, size << 1);
		myParagraphHtmlTags = ZLArrayUtils.createCopy(myParagraphHtmlTags, size, size << 1);
		myParagraphTagNumbers = ZLArrayUtils.createCopy(myParagraphTagNumbers, size, size << 1);
		myParagraphTagCounts = ZLArrayUtils.createCopy(myParagraphTagCounts, size, size << 1);
		myParagraphXPaths = ZLArrayUtils.createCopy(myParagraphXPaths, size, size << 1);
	}
	
	public void createParagraph(byte kind) {
		final int index = myParagraphsNumber++;
		Log.v("ZLXMLParser", "Anzahl paragraphs "+myParagraphsNumber);
		int[] startEntryIndices = myStartEntryIndices;
		if (index == startEntryIndices.length) {
			extend();
			startEntryIndices = myStartEntryIndices;
		}
		if (index > 0) {
			myTextSizes[index] = myTextSizes[index - 1];
		}
		final int dataSize = myStorage.size();
		startEntryIndices[index] = (dataSize == 0) ? 0 : (dataSize - 1);
		myStartEntryOffsets[index] = myBlockOffset;
		myParagraphLengths[index] = 0;
		myParagraphKinds[index] = kind;
		
	}

	/**
	 * Create a new text paragraph 
	 */
	public void createParagraph(byte kind, byte tag, String[] tagStack) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		final int index = myParagraphsNumber++;
		String myChapterPath = null;
		int[] startEntryIndices = myStartEntryIndices;
		if (index == startEntryIndices.length) {
			extend();
			startEntryIndices = myStartEntryIndices;
		}
		if (index > 0) {
			myTextSizes[index] = myTextSizes[index - 1];
		}
		final int dataSize = myStorage.size();
		startEntryIndices[index] = (dataSize == 0) ? 0 : (dataSize - 1);
		myStartEntryOffsets[index] = myBlockOffset;
		myParagraphLengths[index] = 0;
		if (myParagraphHtmlTags[index] != HtmlTag.BR)  {
			myParagraphHtmlTags[index] = tag;
		}
		
		if (tagStack != null) {
			myParagraphXPaths[index] = getXPathFromTagStack(tagStack);
			if (tag == HtmlTag.BR) {
				myParagraphXPaths[index+1] = myParagraphXPaths[index];
				myParagraphHtmlTags[index+1] = tag;
			}
		}
		
		if (index == 70) {
			System.out.println();
		}
		// count the number of tags from one type with the same depth and the same xpath string
		int tagCounter = 1;
		for (int i = index-1; i >= 0; i--) {
			if (myParagraphXPaths[index] == null || myParagraphHtmlTags[index] == HtmlTag.BR) {
				tagCounter = 0;
				break;
			}
			if (myParagraphKinds[i] == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH) {
				break;
			}
			if (myParagraphXPaths[i] == null) {
				continue;
			}
			if (myParagraphXPaths[i].equals(myParagraphXPaths[index]) && 
					myParagraphHtmlTags[i] == myParagraphHtmlTags[index]) {
				tagCounter = myParagraphTagCounts[i]+1;
				break;
			} 
			if (countSlashInString(myParagraphXPaths[i]) < countSlashInString(myParagraphXPaths[index])) {
				break;
			} 
		}
		myParagraphTagCounts[index] = tagCounter;
		
		myParagraphKinds[index] = kind;
		switch (tag) {
			case HtmlTag.A:
			case HtmlTag.B:
			case HtmlTag.BODY:
			case HtmlTag.BR:
			case HtmlTag.CITE:
			case HtmlTag.CODE:
			case HtmlTag.DFN:
			case HtmlTag.DIV:
			case HtmlTag.EM:
			case HtmlTag.H1:
			case HtmlTag.H2:
			case HtmlTag.H3:
			case HtmlTag.H4:
			case HtmlTag.H5:
			case HtmlTag.H6:
			case HtmlTag.HEAD:
			case HtmlTag.HR:
			case HtmlTag.HTML:
			case HtmlTag.I:
			case HtmlTag.IMG:
			case HtmlTag.LI:
			case HtmlTag.OL:
			case HtmlTag.P:
			case HtmlTag.PRE:
			case HtmlTag.S:
			case HtmlTag.SCRIPT:
			case HtmlTag.SELECT:
			case HtmlTag.STRONG:
			case HtmlTag.STYLE:
			case HtmlTag.SUB:
			case HtmlTag.SUP:
			case HtmlTag.TITLE:
			case HtmlTag.TR:
			case HtmlTag.UL:
			case HtmlTag.UNKNOWN:
				tagcount[tag]++;
//				if (myChapterPath != null && myChapterPath != fbreader.getPathToChapterFile(index)) {
//					tagcount[tag] = 1;
//				} 
//				myChapterPath = fbreader.getPathToChapterFile(index);
				myParagraphTagNumbers[index] = tagcount[tag];
				break;
			default:
				break;
		}
		System.out.println();
	}
	
	/**
	 * how many slashes contains the string 
	 */
	private int countSlashInString(String s) {
		int counter = 0;
		for(int i = 0; i < s.length(); i++){
		    if (s.charAt(i) == '/') {
		    	counter++;
		    }
		}
		return counter;
	}
	
	/**
	 * generate the xpath string for the current paragraph
	 * @param tagStack
	 * @return
	 */
	private String getXPathFromTagStack(String[] tagStack) {
		String xPath = "/xhtml:";
		for (int i = 0; i < tagStack.length; i++) {
			if (tagStack[i] == null) {
				break;
			}
			if (tagStack[i].contains("html")) {
				tagStack[i] = "html";
				xPath += tagStack[i];
			} else {
				xPath += "/" + tagStack[i];
			}
			
		}
		return xPath;
//		String xPath = "";
//		for (int i = 0; i < tagStack.length; i++) {
//			if (tagStack[i] == null) {
//				break;
//			}
//			if (tagStack[i].contains("html")) {
//				tagStack[i] = "html";
//			}
//			xPath += "/*[local-name()='" + tagStack[i] + "']";
//		}
//		return xPath;
	}

	private char[] getDataBlock(int minimumLength) {
		char[] block = myCurrentDataBlock;
		if ((block == null) || (minimumLength > block.length - myBlockOffset)) {
			if (block != null) {
				myStorage.freezeLastBlock();
			}
			block = myStorage.createNewBlock(minimumLength);
			myCurrentDataBlock = block;
			myBlockOffset = 0;
		}
		return block;
	}

	public void addControl(byte textKind, boolean isStart) {
		final char[] block = getDataBlock(2);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		short kind = textKind;
		if (isStart) {
			kind += 0x0100;
		}
		block[myBlockOffset++] = (char)kind;
	}

	public void addText(char[] text) {
		addText(text, 0, text.length);
	}

	public void addText(char[] text, int offset, int length) {
		char[] block = getDataBlock(3 + length);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.TEXT;
		block[blockOffset++] = (char)(length >> 16);
		block[blockOffset++] = (char)length;
		System.arraycopy(text, offset, block, blockOffset, length);
		myBlockOffset = blockOffset + length;
		myTextSizes[myParagraphsNumber - 1] += length;
	}
	
	public void addControl(ZLTextForcedControlEntry entry) {
		int len = 2;
		for (int mask = entry.getMask(); mask != 0; mask >>= 1) {
			len += mask & 1;
		}
		final char[] block = getDataBlock(len);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FORCED_CONTROL;
		block[myBlockOffset++] = (char)entry.getMask();
		if (entry.isLeftIndentSupported()) {
			block[myBlockOffset++] = (char)entry.getLeftIndent();
		}
		if (entry.isRightIndentSupported()) {
			block[myBlockOffset++] = (char)entry.getRightIndent();
		}
		if (entry.isAlignmentTypeSupported()) {
			block[myBlockOffset++] = (char)entry.getAlignmentType();
		}
	}
	
	public void addHyperlinkControl(byte textKind, byte hyperlinkType, String label) {
		final short labelLength = (short)label.length();
		final char[] block = getDataBlock(3 + labelLength);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		block[blockOffset++] = (char)((hyperlinkType << 9) + 0x0100 + textKind);
		block[blockOffset++] = (char)labelLength;
		label.getChars(0, labelLength, block, blockOffset);
		myBlockOffset = blockOffset + labelLength;
	}
	
	public void addImage(String id, short vOffset, boolean isCover) {
		final int len = id.length();
		final char[] block = getDataBlock(4 + len);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.IMAGE;
		block[blockOffset++] = (char)vOffset;
		block[blockOffset++] = (char)len;
		id.getChars(0, len, block, blockOffset);
		blockOffset += len;
		block[blockOffset++] = (char)(isCover ? 1 : 0);
		myBlockOffset = blockOffset;
	}
	
	public void addFixedHSpace(short length) {
		final char[] block = getDataBlock(2);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FIXED_HSPACE;
		block[myBlockOffset++] = (char)length;
	}	

	public void stopReading() {
		/*
		if (myCurrentDataBlock != null) {
			myStorage.freezeLastBlock();
			myCurrentDataBlock = null;
		}
		final int size = myParagraphsNumber;
		myStartEntryIndices = ZLArrayUtils.createCopy(myStartEntryIndices, size, size);
		myStartEntryOffsets = ZLArrayUtils.createCopy(myStartEntryOffsets, size, size);
		myParagraphLengths = ZLArrayUtils.createCopy(myParagraphLengths, size, size);
		myParagraphKinds = ZLArrayUtils.createCopy(myParagraphKinds, size, size);
		*/
	}
}
