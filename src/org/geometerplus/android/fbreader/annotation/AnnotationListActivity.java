package org.geometerplus.android.fbreader.annotation;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import de.upb.android.reader.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AnnotationListActivity extends Activity {
	
	private List<Annotation> listOfAnnotations;
	
	private AnnotationAdapter adapter;
	
	private Annotation selectedAnnotation;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Intent intent = getIntent();
		final ArrayList<Annotation> annotations = intent.getParcelableArrayListExtra("annotations");
		
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		listOfAnnotations = annotations;
		
		setContentView(R.layout.annotation_list);
		
		final ListView list = (ListView) findViewById(R.id.AnnotationListView1);
		
		adapter = new AnnotationAdapter(this, listOfAnnotations, list); 
		
        list.setClickable(true);
        list.setItemsCanFocus(true);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
		list.setAdapter(adapter);
		
		Button showButton = (Button) findViewById(R.id.show_annotation_button);
		showButton.setText(R.string.quickaction_show);
		showButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedAnnotation = listOfAnnotations.get(adapter.getSelectedIndex());
				fbreader.doAction(ActionCode.SELECTION_SHOW_ANNOTATION, selectedAnnotation);
			}
		});
		
		Button editButton = (Button) findViewById(R.id.edit_annotation_button);
		editButton.setText(R.string.quickaction_edit);
		editButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedAnnotation = listOfAnnotations.get(adapter.getSelectedIndex());
				fbreader.doAction(ActionCode.SELECTION_NOTE, selectedAnnotation);
			}
		});
		
		Button removeButton = (Button) findViewById(R.id.remove_annotation_button);
		removeButton.setText(R.string.quickaction_remove);
		removeButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedAnnotation = listOfAnnotations.get(adapter.getSelectedIndex());
				fbreader.doAction(ActionCode.SELECTION_REMOVE_ANNOTATION, selectedAnnotation);
			}
		});
		
		if (adapter.getSelectedIndex() == -1) {
			showButton.setEnabled(false);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
		
		Button cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setText(R.string.quickaction_clear);
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				fbreader.doAction(ActionCode.SELECTION_CLEAR);
				finish();
			}
		});
	}
}