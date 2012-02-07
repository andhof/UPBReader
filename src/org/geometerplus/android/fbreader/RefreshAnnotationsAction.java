package org.geometerplus.android.fbreader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.SelectionNoteActivity;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.SemAppsListActivity;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotation;
import org.geometerplus.android.util.SQLiteUtil;
import org.geometerplus.android.util.XMLUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication.ZLAction;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class RefreshAnnotationsAction extends ZLAction {

	private HttpHelper asyncTask;
	private FBReaderApp fbreader;
	private FBReader baseActivity;
	
	RefreshAnnotationsAction(FBReader baseActivity, FBReaderApp fbreader) {
		this.baseActivity = baseActivity;
		this.fbreader = fbreader;
	}
	
	@Override
	protected void run(Object... params) {
		Log.v("RefreshAnnotationsAction", "Refresh has startet.");
		String bookPath = fbreader.Model.Book.File.getPath();
		EPub epub = fbreader.EPubs.getEPubByLocalPath(bookPath);
		String semapp_id = epub.getSemAppId();
		String epub_id = epub.getId();
		asyncTask = new HttpHelper(baseActivity);
		asyncTask.execute("http://epubdummy.provideal.net/api/semapps/"+semapp_id+"/epubs/"+epub_id);
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
			    Log.e("UPBLibraryLoginActivity", e.toString());
			} 
			return resEntityGetResult;
		}
		
		@Override
		protected void onPostExecute(String result) {
			String id;
			String updated_at;
			Annotation annotation;
			
			EPub epub = XMLUtil.loadEPubFromXMLString(result);
			ArrayList<SemAppsAnnotation> saAnnotations = epub.getAnnotations().getAnnotations();
			for (SemAppsAnnotation a : saAnnotations) {
				if (a.getData().isEmpty()) {
					continue;
				}
				id = a.getId();
				updated_at = a.getUpdated_at();
				
				annotation = fbreader.Annotations.getAnnotationByUPBId(id);
				// annotation on server is newer
				if (annotation != null && !annotation.getUpdatedAt().equals(updated_at)) {
					Annotation updatedAnnotation = XMLUtil.loadAnnotationFromXMLString(a.getData());
					updatedAnnotation.setUPBId(id);
					updatedAnnotation.setUpdatedAt(updated_at);
					ListIterator<Annotation> it;
					it = fbreader.Annotations.getAnnotations().listIterator();
					while(it.hasNext()) {
						Annotation current = it.next();
						if (current.getUPBId().equals(id)) {
							it.set(updatedAnnotation);
							SQLiteUtil.writeAnnotationToDatabase(baseActivity, current, epub.getId());
							break;
						}
					}
				}
				// annotation on server but is missing locally
				if (annotation == null) {
					annotation = XMLUtil.loadAnnotationFromXMLString(a.getData());
					annotation.setUPBId(id);
					annotation.setUpdatedAt(updated_at);
					fbreader.Annotations.addAnnotation(annotation);
					SQLiteUtil.writeAnnotationToDatabase(baseActivity, annotation, epub.getId());
				}
			}
			
			if (saAnnotations.size() < fbreader.Annotations.getAnnotations().size()) {
				// TODO entferne weggefallene annotationen
			}
			
		}
	}
}
