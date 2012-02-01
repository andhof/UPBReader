package org.geometerplus.android.fbreader;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.database.AnnotationsDbAdapter;
import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SelectionRemoveAnnotationAction extends FBAndroidAction {
	private FBReaderApp fbreader;
	private AnnotationsDbAdapter dbHelper;
	private Cursor cursor;
	private HttpEntity resEntityPost;
	private Object[] connectionResult;
	private int myStatusCode;
	private String url;
	private String username;
	private String password;
	private ConnectionManager conn;
	
	SelectionRemoveAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		this.fbreader = fbreader;
	}

	@Override
	protected void run(Object... params) {
		final Annotation annotation = (Annotation) params[0];
		fbreader.Annotations.removeAnnotation(annotation);
		BaseActivity.hideAnnotationSelectionPanel();
		fbreader.BookTextView.removeAnnotationHighlight(annotation);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				conn = ConnectionManager.getInstance();
				if (annotation.getUPBId() != null && !annotation.getUPBId().isEmpty()) {
					SharedPreferences settings = BaseActivity.getSharedPreferences("upblogin", 0);
					username = settings.getString("user", "Localuser");
					password = settings.getString("password", null);
					
					String annotation_id = annotation.getUPBId();
					String epub_id = annotation.getEPubId();
					String semapp_id = fbreader.EPubs.getEPubById(epub_id).getSemAppId();
					url = "http://epubdummy.provideal.net/api/semapps/"+ 
			    			semapp_id + "/epubs/" + epub_id + "/annotations/" + annotation_id; 
					
					conn.authenticate(username, password);
					connectionResult = conn.postStuffDelete(url);
					resEntityPost = (HttpEntity) connectionResult[0];
					myStatusCode = ((Integer) connectionResult[1]).intValue();
					
					if (myStatusCode == conn.AUTHENTICATION_FAILED) {
						Log.v("SelectionRemoveAnnotationAction", "Authentication failed. Return.");
						return;
					}
				}
				
				Uri uri = DBAnnotations.CONTENT_URI;
				String selection = DBAnnotations.ANNOTATION_ID + "=\"" + annotation.getId() + "\"";
				BaseActivity.getContentResolver().delete(uri, selection, null);
				
				if (myStatusCode != conn.OK) {
					SharedPreferences settings = BaseActivity.getSharedPreferences("annotation_stack", 0);
					Set<String> urlset;
					urlset = settings.getStringSet("delete", new HashSet<String>());
					urlset.add(url);
					SharedPreferences.Editor e = settings.edit();
					e.putStringSet("delete", urlset);
					e.commit();
					settings = BaseActivity.getSharedPreferences("annotation_stack", 0);
				}
				
				// TODO hier entfernen
				// alles andere auskommentieren, dann kann man selber bestimmte annotationen löschen
				
//				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
//						"4f287777d0434c17a6000011");
//				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
//						"4f287e0ad0434c17a6000012");
			}
			
			private void deleteByHand(String url) {
				username = "admin";
				password = "123456";
				
				conn.authenticate(username, password);
				connectionResult = conn.postStuffDelete(url);
				resEntityPost = (HttpEntity) connectionResult[0];
			}
		}).start();
	}
}
