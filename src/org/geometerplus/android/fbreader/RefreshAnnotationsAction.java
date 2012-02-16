package org.geometerplus.android.fbreader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.SelectionNoteActivity;
import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.ScenarioListActivity;
import org.geometerplus.android.fbreader.semapps.SemAppsListActivity;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotation;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotations;
import org.geometerplus.android.util.SQLiteUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.XMLUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication.ZLAction;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.upb.android.reader.R;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class RefreshAnnotationsAction extends ZLAction {

	private HttpHelper asyncTask;
	private FBReaderApp fbreader;
	private FBReader baseActivity;
	private Cursor cursor;
	private int epub_id;
	private int scenario_id; 
	
	RefreshAnnotationsAction(FBReader baseActivity, FBReaderApp fbreader) {
		this.baseActivity = baseActivity;
		this.fbreader = fbreader;
	}
	
	@Override
	protected void run(Object... params) {
		Log.v("RefreshAnnotationsAction", "Refresh has startet.");
		// TODO wohl entfernen weil nicht mehr gebraucht. Szenario ist immer eindeutig das aktuelle.
//		String bookPath = fbreader.Model.Book.File.getPath();
//		EPub epub = fbreader.EPubs.getEPubByLocalPath(bookPath);
//		if (epub == null) {
//			return;
//		}
//		epub_id = epub.getId();
		int scenario_id = fbreader.Scenario.getId();
		if (scenario_id == -1) {
			return;
		}
		asyncTask = new HttpHelper(baseActivity);
		asyncTask.execute("http://epubdummy.provideal.net/api/scenarios/"+scenario_id+"/annotations");
	}
	
	private class HttpHelper extends AsyncTask<String, Void, String> {

		FBReader mActivity;
		
		private String getURL;
		private ConnectionManager conn;
		private HttpEntity resEntityGet;
		private String resEntityGetResult;
		private Object[] connectionResult;
		private int myStatusCode;
		
		HttpHelper(FBReader activity) {
            mActivity = activity;
        }
		
		@Override
		protected String doInBackground(String... params) {
			SharedPreferences settings = mActivity.getSharedPreferences("upblogin", 0);
			String username = settings.getString("user", "Localuser");
			String password = settings.getString("password", null);
			
			try {
				getURL = params[0];
				
				conn = ConnectionManager.getInstance();
				conn.authenticate(username, password);
				connectionResult = conn.postStuffGet(getURL);
				resEntityGet = (HttpEntity) connectionResult[0];
				myStatusCode = ((Integer) connectionResult[1]).intValue();
				if (myStatusCode == conn.AUTHENTICATION_FAILED ||
						myStatusCode == conn.NO_INTERNET_CONNECTION) {
					return null;
				}
		        resEntityGetResult = EntityUtils.toString(resEntityGet);
		        
			} catch (Exception e) {
			    e.printStackTrace();
			    Log.e("RefreshAnnotationAction", e.toString());
			} 
			return resEntityGetResult;
		}
		
		@Override
		protected void onPostExecute(String annotations_xml) {
			if (annotations_xml == null) {
				Handler h = new Handler(baseActivity.getMainLooper());
				
			    h.post(new Runnable() {
			        @Override
			        public void run() {
			        	Log.v("AnnotationService", "Toast wird angezeigt. Fertig.");
			            Toast.makeText(baseActivity, baseActivity.getString(R.string.toast_download_failed), Toast.LENGTH_LONG).show();
			        }
			    });
				return; 
			}
			int upb_id;
			String updated_at;
			Annotation annotation;
			
			epub_id = fbreader.Scenario.getEPubId();
			
			// remove all annotations from object structure
			fbreader.Annotations.getAnnotations().clear();
			// remove all annotations from database belonging to the epub
			Uri uri = DBAnnotations.CONTENT_URI;
			String selection = DBAnnotations.EPUB_ID + "=\"" + epub_id + "\"";
			baseActivity.getContentResolver().delete(uri, selection, null);
			
			SemAppsAnnotations saAnnotations = 
				XMLUtil.loadSemAppsAnnotationsFromXMLString(annotations_xml);
			ArrayList<SemAppsAnnotation> saAnnotationsList = saAnnotations.getAnnotations();
			for (SemAppsAnnotation a : saAnnotationsList) {
				
				if (a.getData().isEmpty()) {
					continue;
				}
				
				upb_id = a.getId();
				updated_at = a.getUpdated_at();
				
				annotation = XMLUtil.loadAnnotationFromXMLString(a.getData());
				annotation.setUPBId(upb_id);
				annotation.setEPubId(epub_id);
				annotation.setUpdatedAt(updated_at);
				fbreader.Annotations.addAnnotation(annotation);
				SQLiteUtil.writeAnnotationToDatabase(baseActivity, annotation, epub_id);
			}
			
			fbreader.loadAnnotationHighlighting();
			
			Handler h = new Handler(baseActivity.getMainLooper());
		    h.post(new Runnable() {
		        @Override
		        public void run() {
		            Toast.makeText(baseActivity, baseActivity.getString(R.string.toast_refresh_ok), Toast.LENGTH_LONG).show();
		        }
		    });
			
			// This was with removing and adding, but clearing all and then fill the annotations is better i think.
			/*
			LinkedList<Annotation> annotationsToRemove = 
				new LinkedList<Annotation>(fbreader.Annotations.getAnnotations());
			SemAppsAnnotations saAnnotations = 
				XMLUtil.loadSemAppsAnnotationsFromXMLString(annotations_xml);
			ArrayList<SemAppsAnnotation> saAnnotationsList = saAnnotations.getAnnotations();
			for (SemAppsAnnotation a : saAnnotationsList) {
				
				if (a.getData().isEmpty()) {
					continue;
				}
				upb_id = a.getId();
				updated_at = a.getUpdated_at();
				
				annotation = fbreader.Annotations.getAnnotationByUPBId(upb_id);
				if (annotation != null) {
					annotationsToRemove.remove(annotation);
				}
				
				// annotation on server is newer
				if (annotation != null && !annotation.getUpdatedAt().equals(updated_at)) {
					Annotation updatedAnnotation = XMLUtil.loadAnnotationFromXMLString(a.getData());
					updatedAnnotation.setUPBId(upb_id);
					annotation.setEPubId(epub_id);
					updatedAnnotation.setUpdatedAt(updated_at);
					
					ListIterator<Annotation> it;
					it = fbreader.Annotations.getAnnotations().listIterator();
					while(it.hasNext()) {
						Annotation current = it.next();
						if (current.getUPBId() == upb_id) {
							it.set(updatedAnnotation);
							SQLiteUtil.writeAnnotationToDatabase(baseActivity, current, epub_id);
							break;
						}
					}
				}
				// annotation on server but is missing locally
				if (annotation == null) {
					annotation = XMLUtil.loadAnnotationFromXMLString(a.getData());
					annotation.setUPBId(upb_id);
					annotation.setEPubId(epub_id);
					annotation.setUpdatedAt(updated_at);
					fbreader.Annotations.addAnnotation(annotation);
					SQLiteUtil.writeAnnotationToDatabase(baseActivity, annotation, epub_id);
				}
			}
			
			if (annotationsToRemove.size() > 0) {
				// entferne weggefallene annotationen
				for (Annotation a : annotationsToRemove) {
					upb_id = a.getUPBId();
					fbreader.Annotations.removeAnnotation(a);
					Uri uri = DBAnnotations.CONTENT_URI;
					String selection = DBAnnotations.UPB_ID + "=\"" + upb_id + "\"";
					baseActivity.getContentResolver().delete(uri, selection, null);
				}
			}
			
			fbreader.loadAnnotationHighlighting();
			*/
		}
	}
}
