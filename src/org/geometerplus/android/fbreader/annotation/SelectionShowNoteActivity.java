package org.geometerplus.android.fbreader.annotation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.database.DBEPub.DBEPubs;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.fragments.AnnotationShowNoteFragment1;
import org.geometerplus.android.fbreader.fragments.AnnotationShowNoteFragment2;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.SemAppDummy;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
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
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Intent intent = getIntent();
		final Annotation annotation = (Annotation) intent.getParcelableExtra("annotation");
		
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		
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
			
			final List<Annotation> listOfComments = 
				fbreader.getAnnotationsByCategory(getString(R.string.selectionnote_category4));
			
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
					Annotation newAnnotation = fbreader.Annotations.addAnnotation();
					
					String startPart = annotation.getAnnotationTarget().getRange().getStart().getPart();
					String endPart = annotation.getAnnotationTarget().getRange().getEnd().getPart();
					String content = textInput.getText().toString();
					
					// using the deviceid for user identification, should be the imt account name
					String author_name = Secure.getString(SelectionShowNoteActivity.this.getContentResolver(), Secure.ANDROID_ID);
					newAnnotation.getAuthor().setName(author_name);
					
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
					
					newAnnotation.setId(fbreader.md5(book.getContentHashCode() + newAnnotation.toString()));
					
					reload();
					
					String bookPath = book.File.getPath();
					EPub epub = fbreader.EPubs.getEPubByLocalPath(bookPath);
					String eid = epub.getId();
					if (epub == null) {
						eid = fbreader.md5(book.getContentHashCode());
					}
					
					fbreader.writeAnnotationToDatabase(SelectionShowNoteActivity.this, newAnnotation, eid);
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
}