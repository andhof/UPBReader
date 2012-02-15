package org.geometerplus.android.fbreader.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.annotation.database.DBEPub.DBEPubs;
import org.geometerplus.android.fbreader.annotation.database.DBSemApp.DBSemApps;
import org.geometerplus.android.fbreader.annotation.model.*;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotation;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotations;
import org.geometerplus.android.util.SQLiteUtil;
import org.geometerplus.android.util.StorageUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.XMLUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.html.HtmlTag;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextWord;

import de.upb.android.reader.R;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SelectionNoteActivity extends Activity {

	private HttpHelper asyncTask;
	
	Annotation annotation;
	private boolean newAnnotation;
	private Button myOkButton;
	private Button cancelButton;
	private String username;
	private String password;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		Intent intent = getIntent();
		
		if (intent.getParcelableExtra("annotation") == null) {
			annotation = fbreader.Annotations.addAnnotation();
			newAnnotation = true;
		} else {
			annotation = (Annotation) intent.getParcelableExtra("annotation");
			newAnnotation = false;
		}
		
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		
		SharedPreferences settings = getSharedPreferences("upblogin", 0);
		username = settings.getString("user", "Localuser");
		password = settings.getString("password", null);
		
		setContentView(R.layout.annotation_note_dialog);
		
		setTitle(R.string.selectionnote_title);
		
		findTextView(R.id.note_author_label).setText(R.string.selectionnote_author);
		
		findTextView(R.id.note_author_name).setText(username);
		
		findTextView(R.id.note_input_label).setText(R.string.selectionnote_input);
		
		final EditText contentEditText = (EditText) findViewById(R.id.note_text_input);
		contentEditText.setText(annotation.getAnnotationContent().getAnnotationText());
		
		final EditText tagEditText = (EditText)findViewById(R.id.note_tags_input);
		tagEditText.setText(R.string.selectionnote_tags);
		
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
		
		findTextView(R.id.note_categories_label).setText(R.string.selectionnote_categories);
		
		final String[] Categories = new String[] { 
				getString(R.string.selectionnote_category1), 
				getString(R.string.selectionnote_category2),
				getString(R.string.selectionnote_category3)
			};
		
		final Spinner spinner = (Spinner) findViewById(R.id.spinner);
		
		spinner.setPrompt(getString(R.string.selectionnote_categories));
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
				ZLTextParagraphCursor startCursor;
				ZLTextParagraphCursor elementCursor;
				
				String content = contentEditText.getText().toString();
				String tagString = tagEditText.getText().toString();
				String[] tags = tagString.split(", ");
				
				
				if (newAnnotation) {
					ZLTextPosition selectionStartPos = fbreader.BookTextView.getSelectionStartPosition();
					ZLTextPosition selectionEndPos = fbreader.BookTextView.getSelectionEndPosition();
					int selectionStartParagraphIndex = selectionStartPos.getParagraphIndex();
					int selectionEndParagraphIndex = selectionEndPos.getParagraphIndex();
					
					String selectedText = fbreader.BookTextView.getSelectedText();
					
					// define the  Annotationtarget 
					// define the path to the chapter file
					String startPart = fbreader.getPathToChapterFile(selectionStartPos.getParagraphIndex());
					String endPart = fbreader.getPathToChapterFile(selectionEndPos.getParagraphIndex());
					// define the xpath String to the paragraph in the document and the charOffset
					String startParagraphXPath = textModel.getParagraphXPath(selectionStartParagraphIndex);
					String endParagraphXPath = textModel.getParagraphXPath(selectionEndParagraphIndex);
					int startParagraphTagCount = textModel.getParagraphTagCountWithBR(selectionStartParagraphIndex);
					int endParagraphTagCount = textModel.getParagraphTagCountWithBR(selectionEndParagraphIndex);
					
					String startXPath = startParagraphXPath + "[" + startParagraphTagCount + "]";
					String endXPath = endParagraphXPath + "[" + endParagraphTagCount + "]";
					
					startCursor = getStartCursor(selectionStartParagraphIndex);
					elementCursor = ZLTextParagraphCursor.cursor(textModel, selectionStartParagraphIndex);
					int startCharOffset = fbreader.computeCharOffset(startCursor, elementCursor, selectionStartPos.getElementIndex(), true);
					
					startCursor = getStartCursor(selectionEndParagraphIndex);
					elementCursor = ZLTextParagraphCursor.cursor(textModel, selectionEndParagraphIndex);
					int endCharOffset = fbreader.computeCharOffset(startCursor, elementCursor, selectionEndPos.getElementIndex(), false);
					
					ZLColor highlightColor = new ZLColor(150, 150, 255);
					
					annotation.getAuthor().setName(username);
					
					annotation.setCategory(spinner.getSelectedItem().toString());
					annotation.setCreated(new Date().getTime());
					annotation.setModified(new Date().getTime());
					annotation.setTags(new ArrayList(Arrays.asList(tags)));
					DocumentIdentifier documentIdentifier = annotation.getAnnotationTarget().getDocumentIdentifier();
					documentIdentifier.setTitle(book.getTitle());
					documentIdentifier.setAuthors(book.authorNames());
					annotation.getAnnotationTarget().setMarkedText(selectedText);
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
					
					int annotation_id = StorageUtil.getCurrentCounterAndIncrement(SelectionNoteActivity.this, "annotation_id_counter");
			    	annotation.setId(annotation_id);
					
					ZLTextFixedPosition newEndPos = new ZLTextFixedPosition(selectionEndPos.getParagraphIndex(), selectionEndPos.getElementIndex()+1, selectionEndPos.getCharIndex());
					fbreader.BookTextView.addAnnotationHighlight(selectionStartPos, newEndPos, highlightColor, true, annotation);
					
//					saveToXML(fbreader.Annotations);
				} else {
					annotation.getAnnotationContent().setAnnotationText(content);
					annotation.setTags(new ArrayList(Arrays.asList(tags)));
					annotation.setCategory(spinner.getSelectedItem().toString());
					annotation.setModified(new Date().getTime());
					Bundle bundle = new Bundle();
					bundle.putString("content", content);
					bundle.putStringArrayList("tags", new ArrayList(Arrays.asList(tags)));
					bundle.putString("category", spinner.getSelectedItem().toString());
					bundle.putLong("modified", new Date().getTime());
					Intent in = new Intent(SelectionNoteActivity.this, FBReader.class);
					in.putExtras(bundle);
			        setResult(3,in);
					
//					saveToXML(fbreader.Annotations);
				}
				
				int semapp_id;
				int epub_id;
				String bookPath = book.File.getPath();
				EPub epub = fbreader.EPubs.getEPubByLocalPath(bookPath);
				if (epub == null) {
					epub_id = StorageUtil.getCurrentCounterAndIncrement(SelectionNoteActivity.this, "epub_id_counter");
					String name = book.getTitle();
					String updated_at = new Date().toString();
					String file_name = book.File.getShortName();
					String file_path = bookPath;
					String local_path = bookPath;
					semapp_id = -1;

					epub = fbreader.EPubs.addEPub(
							epub_id, name, updated_at, file_name, file_path, local_path, semapp_id);
				} 
				epub_id = epub.getId();
				semapp_id = epub.getSemAppId();
				
				annotation.setEPubId(epub_id);
				
				SQLiteUtil.writeEPubAndAnnotationToDatabase(SelectionNoteActivity.this, epub, bookPath, annotation);
				
				// Start asynctask for uploading annotation
				if (semapp_id > 0) {
					if (asyncTask != null) asyncTask.cancel(true);
				    asyncTask = new HttpHelper();
				    String xml = XMLUtil.saveAnnotationToString(annotation);
				    
				    if (newAnnotation) {
				    	int scenario_id = fbreader.Scenario.getId();
				    	asyncTask.execute("http://epubdummy.provideal.net/api/scenarios/"+ 
				    			scenario_id + "/annotations", xml);
				    } else {
				    	asyncTask.execute("http://epubdummy.provideal.net/api/annotations/"+ 
				    			annotation.getUPBId(), xml);
				    }
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
	
	/**
	 * Get the number of tags for one paragraph 
	 */
	private int getTagCountByParagraphIndex(int index) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		ZLTextModel textModel = fbreader.BookTextView.getModel();
		
		TOCTree tree = fbreader.getTreeByParagraphIndex(index);
		int thisParagraphStartIndex = tree.getReference().ParagraphIndex;
		
		byte tag = textModel.getParagraphHtmlTag(index);
		byte[] paragraphHtmlTags = textModel.getAllParagraphHtmlTags();
		String startParagraphXPath = textModel.getParagraphXPath(index);
		String[] paragraphXPaths = textModel.getAllParagraphXPaths();
		
		int countTagsByType = 0;
		for (int i = thisParagraphStartIndex; i <= index; i++) {
			if (paragraphXPaths[i] != null && paragraphHtmlTags[i] == tag && paragraphXPaths[i].equals(startParagraphXPath)) {
				countTagsByType++;
			}
		}
		return countTagsByType;
	}
	
	/**
	 * Get the cursor for the xpath start paragraph. Not the app intern paragraph
	 */
	private ZLTextParagraphCursor getStartCursor(int index) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		ZLTextModel textModel = fbreader.BookTextView.getModel();
		
		int backJumpCount = 0;
		for (int i = index; i >= 0; i-=2) {
			ZLTextParagraphCursor tmpEndCursor = ZLTextParagraphCursor.cursor(textModel, i);
			if (!tmpEndCursor.hasZLTextWordElement()) {
				backJumpCount += 2;
			} else {
				backJumpCount = 0;
			}
			if (textModel.getParagraphHtmlTag(i) != HtmlTag.BR) {
				if (tmpEndCursor.hasZLTextWordElement()) {
					return ZLTextParagraphCursor.cursor(textModel, i);
				} else {
					return ZLTextParagraphCursor.cursor(textModel, i+backJumpCount);
				}
			} 
		}
		return null;
	}
	
	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
	}
	
	public void saveToXML(Annotations annotations) {
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
	
	private class HttpHelper extends AsyncTask<String, Void, String> {

		private String url;
		private String xml;
		private ConnectionManager conn;
		private Object[] connectionResult;
		private int myStatusCode;
		private HttpEntity resEntityPost;
		private HttpEntity resEntityGet;
		private HttpEntity resEntityPut;
		private String resEntityGetResult;
		private String resEntityPostResult;
		
		@Override
		protected String doInBackground(String... params) {
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			int annotation_id = -1;
			int upb_id = -1;
			String updated_at = "";
			
			url = params[0];
			xml = params[1];
			
			if (fbreader.isNetworkAvailable()) {
				try {
					conn = ConnectionManager.getInstance();
					conn.authenticate(username, password);
					if (newAnnotation) {
						connectionResult = conn.postStuffPost(url, xml);
						resEntityPost = (HttpEntity) connectionResult[0];
						myStatusCode = ((Integer) connectionResult[1]).intValue();
						if (resEntityPost != null && myStatusCode == conn.OK) {
							resEntityPostResult = EntityUtils.toString(resEntityPost);
							SemAppsAnnotation saAnnotation = 
								XMLUtil.loadSemAppsAnnotationFromXMLString(resEntityPostResult);
							upb_id = saAnnotation.getId();
							updated_at = saAnnotation.getUpdated_at();
						}
						
						if (myStatusCode == conn.AUTHENTICATION_FAILED) {
							return null;
						}
						
						annotation.setUPBId(upb_id);
						annotation.setUpdatedAt(updated_at);
						
					} else {
						connectionResult = conn.postStuffPut(url, xml);
						resEntityPut = (HttpEntity) connectionResult[0];
						myStatusCode = ((Integer) connectionResult[1]).intValue();
						if (resEntityPut != null) {
							try {
								resEntityPut.consumeContent();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (myStatusCode == conn.AUTHENTICATION_FAILED) {
							return null;
						}
					}
				} catch (Exception e) {
				    e.printStackTrace();
				    Log.e("SelectionNoteActivity", e.toString());
				} 
			}
			
			if (annotation_id <= 0) {
				annotation_id = annotation.getId();
			}
			
			SQLiteUtil.writeAnnotationToDatabase(SelectionNoteActivity.this, annotation, annotation.getEPubId());
			
			if (newAnnotation && !fbreader.isNetworkAvailable()) {
				SharedPreferences settings = getSharedPreferences("annotation_stack", 0);
				Set<String> urlset;
				urlset = settings.getStringSet("add", new HashSet<String>());
				urlset.add(url + "/" + annotation_id);
				SharedPreferences.Editor e = settings.edit();
				e.putStringSet("add", urlset);
				e.commit();
			}
			
			if (!newAnnotation && !fbreader.isNetworkAvailable()) {
				SharedPreferences settings = getSharedPreferences("annotation_stack", 0);
				Set<String> urlset;
				urlset = settings.getStringSet("update", new HashSet<String>());
				urlset.add(url);
				SharedPreferences.Editor e = settings.edit();
				e.putStringSet("update", urlset);
				e.commit();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			if (conn == null) {
				fbreader.showToast(getString(R.string.toast_add_noconnection));
				return;
			}
			if (myStatusCode == conn.AUTHENTICATION_FAILED) {
				UIUtil.createDialog(SelectionNoteActivity.this, "Error", getString(R.string.authentication_failed));
				return;
			}
		}
	}
}