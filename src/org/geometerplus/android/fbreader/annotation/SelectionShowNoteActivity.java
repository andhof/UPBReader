package org.geometerplus.android.fbreader.annotation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.database.DBEPub.DBEPubs;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.fragments.AnnotationShowNoteFragment1;
import org.geometerplus.android.fbreader.fragments.AnnotationShowNoteFragment2;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.SemAppDummy;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotation;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import de.upb.android.reader.R;

public class SelectionShowNoteActivity extends Activity {
	
	private HttpHelper asyncTask;
	private String username;
	private String password;
	private List<Annotation> listOfComments;
	private Annotation newAnnotation;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Intent intent = getIntent();
		final Annotation annotation = (Annotation) intent.getParcelableExtra("annotation");
		
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		
		SharedPreferences settings = getSharedPreferences("upblogin", 0);
		username = settings.getString("user", "Localuser");
		password = settings.getString("password", null);
		
		String semapp_id = null;
		if (annotation != null && fbreader.EPubs.getEPubById(annotation.getEPubId()) != null) {
			semapp_id = fbreader.EPubs.getEPubById(annotation.getEPubId()).getSemAppId();
		}
		if (bundle == null) {
			if (semapp_id.isEmpty()) {
				setContentView(R.layout.annotation_show_note_fragment1);
			} else {
				setContentView(R.layout.annotation_show_note);
			}
		}
		
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
		
	    findTextView(R.id.show_note_title).setText(R.string.shownote_title);
	    
		findTextView(R.id.show_note_selection_label).setText(R.string.shownote_selection);
		findTextView(R.id.show_note_author_label).setText(R.string.shownote_author);
		findTextView(R.id.show_note_category_label).setText(R.string.shownote_category);
		findTextView(R.id.show_note_tags_label).setText(R.string.shownote_tags);
		findTextView(R.id.show_note_modified_label).setText(R.string.shownote_modified);
		findTextView(R.id.show_note_content_label).setText(R.string.shownote_content);
		
		findTextView(R.id.show_note_selection_text).setText("Muss noch ermittelt werden.");
		findTextView(R.id.show_note_author_text).setText(annotation.getAuthor().getName());
		findTextView(R.id.show_note_category_text).setText(annotation.getCategory());
		findTextView(R.id.show_note_tags_text).setText(tagsString);
		findTextView(R.id.show_note_modified_text).setText(annotation.getUpdatedAt());
		findTextView(R.id.show_note_content_text).setText(
				annotation.getAnnotationContent().getAnnotationText());
		
		// Comment fragment
		if (!semapp_id.isEmpty()){
			findTextView(R.id.comment_title).setText("Kommentare");
			
			listOfComments = fbreader.getAnnotationsByCategory(
					getString(R.string.selectionnote_category4), annotation.getUPBId());
			
			CommentAdapter adapter = new CommentAdapter(this, listOfComments);
			
			ListView list = (ListView) findViewById(R.id.ListView01);
	        list.setClickable(false);
			
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					System.out.println("sadsfsf");
				}
	        });
			
			list.setAdapter(adapter);
			
//			findTextView(R.id.new_comment_label).setText("Neuer Kommentar");
			
			final EditText textInput = (EditText) findViewById(R.id.comment_input);
			
			Button addButton = (Button) findViewById(R.id.add_comment_button);
			addButton.setText("add");
			addButton.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					if (textInput.getText().toString().isEmpty()) {
						return;
					}
					
					Book book = fbreader.Model.Book;
					newAnnotation = fbreader.Annotations.addAnnotation();
					
					String startPart = annotation.getAnnotationTarget().getRange().getStart().getPart();
					String endPart = annotation.getAnnotationTarget().getRange().getEnd().getPart();
					String content = textInput.getText().toString();
					
					newAnnotation.getAuthor().setName(username);
					
					newAnnotation.setCreated(new Date().getTime());
					newAnnotation.setModified(new Date().getTime());
					newAnnotation.setCategory(getString(R.string.selectionnote_category4));
					if (annotation.getUPBId().isEmpty()) {
						newAnnotation.getAnnotationTarget().setTargetAnnotationId(annotation.getId());
					} else {
						newAnnotation.getAnnotationTarget().setTargetAnnotationId(annotation.getUPBId());
					}
					newAnnotation.getAnnotationTarget().getRange().getStart().setPart(startPart);
					newAnnotation.getAnnotationTarget().getRange().getEnd().setPart(endPart);
					newAnnotation.getAnnotationContent().setAnnotationText(content);
					
					newAnnotation.setEPubId(annotation.getEPubId());
					newAnnotation.setId(fbreader.md5(book.getContentHashCode() + newAnnotation.toString()));
					
					String epub_id = newAnnotation.getEPubId();
					EPub epub = fbreader.EPubs.getEPubById(epub_id);
					String semapp_id = epub.getSemAppId();
					
					// Start asynctask for uploading annotation
					asyncTask = new HttpHelper();
				    String xml = fbreader.saveAnnotationToString(newAnnotation);
					
					asyncTask.execute("http://epubdummy.provideal.net/api/semapps/"+ 
			    			semapp_id + "/epubs/" + epub_id + "/annotations", xml);
					
					fbreader.writeAnnotationToDatabase(SelectionShowNoteActivity.this, newAnnotation, epub_id);
					
					reload();
				}
			});
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		reload();
	}
	
	public void reload() {
	    Intent intent = getIntent();
	    overridePendingTransition(0, 0);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	    finish();

	    overridePendingTransition(0, 0);
	    startActivity(intent);
	}
	
	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
	}
	
	private class HttpHelper extends AsyncTask<String, Void, String> {

		private String url;
		private String xml;
		private ConnectionManager conn;
		private HttpEntity resEntityPost;
		private String resEntityPostResult;
		private Object[] connectionResult;
		private int myStatusCode;
		
		@Override
		protected String doInBackground(String... params) {
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			String annotation_id = "";
			String upb_id = "";
			String updated_at = "";
			
			url = params[0];
			xml = params[1];
			
			try {
				conn = ConnectionManager.getInstance();
				conn.authenticate(username, password);
				connectionResult = conn.postStuffPost(url, xml);
				resEntityPost = (HttpEntity) connectionResult[0];
				myStatusCode = ((Integer) connectionResult[1]).intValue();
				if (resEntityPost != null && myStatusCode == conn.OK) {
					resEntityPostResult = EntityUtils.toString(resEntityPost);
					SemAppsAnnotation saAnnotation = 
						fbreader.loadAnnotationFromXMLString(resEntityPostResult);
					upb_id = saAnnotation.getId();
					updated_at = saAnnotation.getUpdated_at();
				}
				if (myStatusCode == conn.AUTHENTICATION_FAILED) {
					return null;
				}
				
				newAnnotation.setUPBId(upb_id);
				newAnnotation.setUpdatedAt(updated_at);
				
			} catch (Exception e) {
			    e.printStackTrace();
			    Log.e("SelectionNoteActivity", e.toString());
			} 
			
			if (annotation_id.isEmpty()) {
				annotation_id = newAnnotation.getId();
			}
			
			fbreader.writeAnnotationToDatabase(SelectionShowNoteActivity.this, newAnnotation, newAnnotation.getEPubId());
			
			if (myStatusCode != conn.OK) {
				SharedPreferences settings = getSharedPreferences("annotation_stack", 0);
				Set<String> urlset;
				urlset = settings.getStringSet("add", new HashSet<String>());
				urlset.add(url);
				SharedPreferences.Editor e = settings.edit();
				e.putStringSet("add", urlset);
				e.commit();
			}
			
			return resEntityPostResult;
		}
		
		@Override
		protected void onPostExecute(String result) {
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			if (myStatusCode == conn.AUTHENTICATION_FAILED) {
				UIUtil.createDialog(SelectionShowNoteActivity.this, "Error", getString(R.string.authentication_failed));
				return;
			}
			if (myStatusCode == conn.NO_INTERNET_CONNECTION) {
				fbreader.showToast(getString(R.string.toast_add_noconnection));
				return;
			}
			reload();
		}
	}
}