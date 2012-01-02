package org.geometerplus.android.fbreader.annotation;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.annotation.model.*;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.TOCTree;
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
import de.upb.android.reader.R;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SelectionNoteActivity extends Activity {

	private boolean newAnnotation;
	private ZLResource myResource;
	private Button myOkButton;
	private Button cancelButton;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		Intent intent = getIntent();
		final Annotation annotation;
		if (intent.getParcelableExtra("annotation") == null) {
			annotation = fbreader.Annotations.addAnnotation();
			newAnnotation = true;
		} else {
			annotation = (Annotation) intent.getParcelableExtra("annotation");
			newAnnotation = false;
		}
		
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		
		setContentView(R.layout.annotation_note_dialog);
		
		
		
		myResource = ZLResource.resource("dialog").getResource("SelectionNoteDialog");
		
		setTitle(myResource.getResource("title").getValue());
		
		findTextView(R.id.note_author).setText(
				myResource.getResource("author").getValue()
		);
		
		findTextView(R.id.note_input_label).setText(
				myResource.getResource("input").getValue()
		);
		
		((EditText) findViewById(R.id.note_text_input)).setText(
				annotation.getAnnotationContent().getAnnotationText()
		);
		
		findTextView(R.id.note_tags_label).setText(
				myResource.getResource("tags").getValue()
		);
		
		String tagsString = "";
		StringBuffer result = new StringBuffer();
	    if (annotation.getTags().size() > 0) {
	        result.append(annotation.getTags().get(0));
	        for (int i = 1; i < annotation.getTags().size(); i++) {
	            result.append(", ");
	            result.append(annotation.getTags().get(i));
	        }
	    }
	    tagsString = result.toString();
		((EditText) findViewById(R.id.note_tags_input)).setText(tagsString);
		
		findTextView(R.id.note_categories_label).setText(
				myResource.getResource("categories").getValue()
		);
		
		final String[] Categories = new String[] { 
				myResource.getResource("category1").getValue(), 
				myResource.getResource("category2").getValue(),
				myResource.getResource("category3").getValue()
			};
		
		final Spinner spinner = (Spinner) findViewById(R.id.spinner);
		
		spinner.setPrompt(myResource.getResource("categories").getValue());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, Categories);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    for (int i = 0; i < Categories.length; i++) {
	    	Log.v("SelectionNoteActivity", Categories[i]+" - "+annotation.getCategory());
	    	if ( Categories[i].equals(annotation.getCategory())) {
	    		spinner.setSelection(i);
	    	}
	    }
	    
	    final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		
		final View buttonsView = findViewById(R.id.authentication_buttons);
	    
	    myOkButton = (Button)buttonsView.findViewById(R.id.ok_button);
		myOkButton.setText(buttonResource.getResource("ok").getValue());
		myOkButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Book book = fbreader.Model.Book;
				ZLTextModel textModel = fbreader.BookTextView.getModel();
				
				TOCTree tree;
				ZLTextParagraphCursor cursor;
				
				String content = ((EditText) findViewById(R.id.note_text_input)).getText().toString();
				String tagString = ((EditText)findViewById(R.id.note_tags_input)).getText().toString();
				String[] tags = tagString.split(", ");
				
				
				if (newAnnotation) {
					ZLTextPosition selectionStartPos = fbreader.BookTextView.getSelectionStartPosition();
					ZLTextPosition selectionEndPos = fbreader.BookTextView.getSelectionEndPosition();
					int selectionStartParagraphIndex = selectionStartPos.getParagraphIndex();
					int selectionEndParagraphIndex = selectionEndPos.getParagraphIndex();
					
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
					int startCharOffset = fbreader.computeCharOffset(cursor, selectionStartPos.getElementIndex(), true);
					cursor = ZLTextParagraphCursor.cursor(textModel, selectionEndPos.getParagraphIndex());
					int endCharOffset = fbreader.computeCharOffset(cursor, selectionEndPos.getElementIndex(), false);
					
					ZLColor highlightColor = new ZLColor(150, 150, 255);
					
					annotation.getAuthor().setName("localuser");
					annotation.setCategory(spinner.getSelectedItem().toString());
					annotation.setCreated(new Date().getTime());
					annotation.setModified(new Date().getTime());
					annotation.setTags(new ArrayList(Arrays.asList(tags)));
					DocumentIdentifier documentIdentifier = annotation.getAnnotationTarget().getDocumentIdentifier();
					documentIdentifier.setTitle(book.getTitle());
					documentIdentifier.setAuthor(book.authorNames());
					Range range = annotation.getAnnotationTarget().getRange();
					range.getStart().setPart(startPart);
					range.getStart().getPath().setXPath(startXPath);
					range.getStart().getPath().setCharOffset(startCharOffset);
					range.getEnd().setPart(endPart);
					range.getEnd().getPath().setXPath(endXPath);
					range.getEnd().getPath().setCharOffset(endCharOffset);
					RenderingInfo renderingInfo = annotation.getRenderingInfo();
					renderingInfo.setHighlightColor(highlightColor.getIntValue());
					
					annotation.getAnnotationContent().setAnnotationText(content);
					
					
					ZLTextFixedPosition newEndPos = new ZLTextFixedPosition(selectionEndPos.getParagraphIndex(), selectionEndPos.getElementIndex()+1, selectionEndPos.getCharIndex());
					fbreader.BookTextView.addAnnotationHighlight(selectionStartPos, newEndPos, highlightColor, true, annotation);
					
					saveToXML(fbreader.Annotations);
				} else {
					Bundle bundle = new Bundle();
					bundle.putString("content", content);
					bundle.putStringArrayList("tags", new ArrayList(Arrays.asList(tags)));
					bundle.putString("category", spinner.getSelectedItem().toString());
					bundle.putLong("modified", new Date().getTime());
					Intent in = new Intent(SelectionNoteActivity.this, FBReader.class);
					in.putExtras(bundle);
			        setResult(3,in);
					
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
	
}