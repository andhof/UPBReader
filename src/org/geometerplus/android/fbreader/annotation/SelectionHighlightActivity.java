package org.geometerplus.android.fbreader.annotation;

import java.io.File;
import java.io.Reader;
import java.util.Date;
import org.geometerplus.android.fbreader.annotation.model.*;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.bookmodel.TOCTree.Reference;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.html.HtmlTag;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import de.upb.android.reader.R;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class SelectionHighlightActivity extends Activity {

	private ZLResource myResource;
	private View myColorButton;
	private View myColorView;
	private Button myOkButton;
	private Button cancelButton;
	private static final String TAG = "SelectionHighlightActivity";
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		Intent i = getIntent();
		final Annotation annotation;
		if (i.getParcelableExtra("annotation") == null) {
			annotation = fbreader.Annotations.addAnnotation();
		} else {
			annotation = (Annotation) i.getParcelableExtra("annotation");
		}
			
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		
		setContentView(R.layout.annotation_highlight_dialog);
		
		myResource = ZLResource.resource("dialog").getResource("SelectionHighlightDialog");
		
		setTitle(myResource.getResource("title").getValue());
		
		findTextView(R.id.highlight_color_label).setText(
				myResource.getResource("color").getValue()
		);
		findTextView(R.id.highlight_underline_label).setText(
				myResource.getResource("underline").getValue()
		);
		findTextView(R.id.highlight_crossout_label).setText(
				myResource.getResource("crossout").getValue()
		);
		
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		
		final View buttonsView = findViewById(R.id.authentication_buttons);
		
		myColorView = findViewById(R.id.highlight_color); 
		
		OnClickListener buttonOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myColorView.setBackgroundDrawable(v.getBackground());
			}
		};
		
		myColorButton = findViewById(R.id.highlight_color_button1);
		myColorButton.setOnClickListener(buttonOnClickListener);
		
		myColorButton = findViewById(R.id.highlight_color_button2);
		myColorButton.setOnClickListener(buttonOnClickListener);
		
		myColorButton = findViewById(R.id.highlight_color_button3);
		myColorButton.setOnClickListener(buttonOnClickListener);
		
		myColorButton = findViewById(R.id.highlight_color_button4);
		myColorButton.setOnClickListener(buttonOnClickListener);
		
		myColorButton = findViewById(R.id.highlight_color_button5);
		myColorButton.setOnClickListener(buttonOnClickListener);
		
		myOkButton = (Button)buttonsView.findViewById(R.id.ok_button);
		myOkButton.setText(buttonResource.getResource("ok").getValue());
		myOkButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Book book = fbreader.Model.Book;
				ZLTextModel textModel = fbreader.BookTextView.getModel();
				ZLTextPosition selectionStartPos = fbreader.BookTextView.getSelectionStartPosition();
				ZLTextPosition selectionEndPos = fbreader.BookTextView.getSelectionEndPosition();
				int selectionStartParagraphIndex = selectionStartPos.getParagraphIndex();
				int selectionEndParagraphIndex = selectionEndPos.getParagraphIndex();
				
				TOCTree tree;
				ZLTextParagraphCursor cursor;

				// define the  Annotationtarget 
				// define the path to the chapter file
				String startPart = fbreader.getPathToChapterFile(selectionStartPos.getParagraphIndex());
				String endPart = fbreader.getPathToChapterFile(selectionEndPos.getParagraphIndex());
				// define the xpath String to the paragraph in the document and the charOffset
				byte tag = textModel.getParagraphHtmlTag(selectionStartParagraphIndex);
				int absoluteStartTagCount = textModel.getParagraphTagNumbers(selectionStartParagraphIndex);
				int absoluteEndTagCount = textModel.getParagraphTagNumbers(selectionEndParagraphIndex);
				tree = fbreader.getTreeByParagraphIndex(selectionStartParagraphIndex);
				// compute the number of the selected p tag for one chapter
				int chapterStartTagCount = absoluteStartTagCount - tree.getTagCount() + 1;
				tree = fbreader.getTreeByParagraphIndex(selectionEndParagraphIndex);
				// compute the number of the selected p tag for one chapter
				int chapterEndTagCount = absoluteEndTagCount - tree.getTagCount() + 1;
				String startXPath = "//"+ HtmlTag.getNameByTag(tag) + "[" + chapterStartTagCount + "]";
				String endXPath = "//"+ HtmlTag.getNameByTag(tag) + "[" + chapterEndTagCount + "]";
						
				cursor = ZLTextParagraphCursor.cursor(textModel, selectionStartPos.getParagraphIndex());
//				int startCharOffset = fbreader.computeCharOffset(cursor, selectionStartPos.getElementIndex(), true);
				cursor = ZLTextParagraphCursor.cursor(textModel, selectionEndPos.getParagraphIndex());
//				int endCharOffset = fbreader.computeCharOffset(cursor, selectionEndPos.getElementIndex(), false);
				
				int highlightColor = ((ColorDrawable) myColorView.getBackground()).getColor();
				boolean underlined = ((CheckBox) findViewById(R.id.highlight_underline_checkbox)).isChecked();
				boolean crossedout = ((CheckBox) findViewById(R.id.highlight_crossout_checkbox)).isChecked();
				
				if (true/*annotation == null*/) {
					annotation.getAuthor().setName("localuser");
					annotation.setCreated(new Date().getTime());
					annotation.setModified(new Date().getTime());
					DocumentIdentifier documentIdentifier = annotation.getAnnotationTarget().getDocumentIdentifier();
					documentIdentifier.setTitle(book.getTitle());
//					documentIdentifier.setAuthor(book.authorNames());
					Range range = annotation.getAnnotationTarget().getRange();
					range.getStart().setPart(startPart);
					range.getStart().getPath().setXPath(startXPath);
//					range.getStart().getPath().setCharOffset(startCharOffset);
					range.getEnd().setPart(endPart);
					range.getEnd().getPath().setXPath(endXPath);
//					range.getEnd().getPath().setCharOffset(endCharOffset);
					
					RenderingInfo renderingInfo = annotation.getRenderingInfo();
					renderingInfo.setHighlightColor(highlightColor);
					renderingInfo.setUnderlined(underlined);
					renderingInfo.setCrossOut(crossedout);
					
					ZLTextFixedPosition newEndPos = new ZLTextFixedPosition(selectionEndPos.getParagraphIndex(), selectionEndPos.getElementIndex()+1, selectionEndPos.getCharIndex());
					fbreader.BookTextView.addAnnotationHighlight(selectionStartPos, newEndPos, new ZLColor(highlightColor), false, annotation);
										
					saveToXML(fbreader.Annotations);
				} 
				
				runOnUiThread(new Runnable() {
					public void run() {
						fbreader.doAction(ActionCode.SELECTION_CLEAR);
					}
				});
				finish();
			}
		});
		
		cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				runOnUiThread(new Runnable() {
					public void run() {
						final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
						fbreader.doAction(ActionCode.SELECTION_CLEAR);
					}
				});
				finish();
			}
		});
	}
	
	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
	}
	
	public void saveToXML(Annotations annotations) {
		Reader xml = null;
		
		try {
//    		xml = new FileReader("xmlDocument.xml");
    		
    		Serializer serializer = new Persister();
//    		Reader reader = new StringReader(xml.toString());
    		String path = Paths.BooksDirectoryOption().getValue()+"/annotations.xml";
    		File annotationXML = new File(path);
    		serializer.write(annotations, annotationXML);
    		
    		System.out.println(Annotations.class.getSimpleName() + " - " + annotations.toString());
    	} catch (Exception e) {
    		Log.e("saveToXML", e.toString());
    	}
	}
	

	void testwiese() {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		Book book = fbreader.Model.Book;
		ZLTextModel textModel = fbreader.BookTextView.getModel();
		/*
		 * #########################################
		 *  Testwiese
		 * #########################################
		 */
		
		String selectedtext = fbreader.BookTextView.getSelectedText();
		ZLTextWordCursor textcursor = fbreader.BookTextView.getStartCursor();
		ZLTextView textview = fbreader.BookTextView;
	//		int test = textview.getSelectedRegion().getSoul().StartElementIndex;
		textview.gotoPosition(2, 4, 0);
		ZLColor color = textview.getHighlightingColor();
	//		fbreader.getColorProfile().HighlightingOption.setValue(new ZLColor(highlightColor.getColor()));
		
	//		textview.highlight(fbreader.BookTextView.getSelectionStartPosition(), newEndPos);
		
	//		ZLTextModel textModel = fbreader.BookTextView.getModel();
		int blub = 0;
		
	//		ZLTextPlainModel test = (ZLTextPlainModel) fbreader.Model.BookTextModel;
	//		String id = test.getId();
		
		String fiesfepath = fbreader.Model.TOCTree.subTrees().get(blub).getPath();
	//		for ( Iterator i = trees.iterator(); i.hasNext(); )	{
	//			TOCTree tree = (TOCTree) i.next();
	//			int thisParagraphIndex = tree.getReference().ParagraphIndex;
	////			int nextParagraphIndex = prevtree != null ? prevtree.getReference().ParagraphIndex : 0;
	//			
	////			if (lastParagraphIndex < paragraphindex && newParagraphIndex >= paragraphindex) {
	////				String name = tree.getText();
	////				String path = tree.getPath();
	////				System.out.println();
	////			}
	//		}
		
		Reference refnumber = fbreader.Model.TOCTree.subTrees().get(blub).getReference(); 
		int ref = refnumber.ParagraphIndex;
		String id = textModel.getId();
		
		
		
	//		TOCTree tree = fbreader.Model.TOCTree.getTreeByParagraphNumber(paragraphindex);
		
		
	//		CharStorage charStorage= ptext.getCharStorage();
	//		char[] data = charStorage.block(0);
	//		fbreader.BookTextView.getStartCursor().getParagraphCursor().
	//		ZLTextParagraphCursor.Processor;
		
	//		ZLTextModel
		int length2 = selectedtext.length();
		int number = fbreader.Model.BookTextModel.getParagraphsNumber();
	//		byte test = fbreader.Model.BookTextModel.getParagraphHtmlTag(bla.getParagraphIndex());
	//		int pcount = fbreader.Model.BookTextModel.getParagraphTagNumbers(bla.getParagraphIndex());
		System.out.println();
	}
}