package org.geometerplus.zlibrary.text.view;

import java.util.ArrayList;

import org.geometerplus.android.fbreader.annotation.model.Annotation;

public class ZLTextAnnotationHighlighting implements ZLTextAbstractHighlighting {
	private ZLTextPosition myStartPosition;
	private ZLTextPosition myEndPosition;
	private boolean isNote;
	private ArrayList<int[]> positions;
	private Annotation annotation;
	
	public ZLTextAnnotationHighlighting(boolean isNote, Annotation annotation) {
		this.isNote = isNote;
		this.annotation = annotation;
	}

	void setup(ZLTextPosition start, ZLTextPosition end) {
		myStartPosition = new ZLTextFixedPosition(start);
		myEndPosition = new ZLTextFixedPosition(end);
		positions = new ArrayList<int[]>();
	}

	public boolean clear() {
		if (isEmpty()) {
			return false;
		}
		myStartPosition = null;
		myEndPosition = null;
		return true;
	}
	
	public void clearPositionsArray() {
		positions.clear();
	}
	
	public void addPositionArray(int[] position) {
		positions.add(position);
	}
	
	public Annotation getAnnotation() {
		return annotation;
	}
	
	public ArrayList<int[]> getPositions() {
		return positions;
	}

	public boolean isEmpty() {
		return myStartPosition == null;
	}
	
	public boolean isNote() {
		return isNote;
	}

	public ZLTextPosition getStartPosition() {
		return myStartPosition;
	}

	public ZLTextPosition getEndPosition() {
		return myEndPosition;
	}
	
	public ZLTextElementArea getStartArea(ZLTextPage page) {
		return page.TextElementMap.getFirstAfter(myStartPosition);
	}

	public ZLTextElementArea getEndArea(ZLTextPage page) {
		return page.TextElementMap.getLastBefore(myEndPosition);
	}
}