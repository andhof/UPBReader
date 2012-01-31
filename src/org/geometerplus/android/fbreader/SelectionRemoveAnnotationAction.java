package org.geometerplus.android.fbreader;

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

public class SelectionRemoveAnnotationAction extends FBAndroidAction {
	private FBReaderApp fbreader;
	private AnnotationsDbAdapter dbHelper;
	private Cursor cursor;
	private HttpEntity resEntityPost;
	private Object[] connectionResult;
	private String myStatusCode;
	private String url;
	private String username;
	private String password;
	
	SelectionRemoveAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		this.fbreader = fbreader;
	}

	@Override
	protected void run(Object... params) {
		final Annotation annotation = (Annotation) params[0];
		BaseActivity.hideAnnotationSelectionPanel();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (!annotation.getUPBId().isEmpty()) {
					SharedPreferences settings = BaseActivity.getSharedPreferences("upblogin", 0);
					username = settings.getString("user", "Localuser");
					password = settings.getString("password", null);
					
					String annotation_id = annotation.getUPBId();
					String epub_id = annotation.getEPubId();
					String semapp_id = fbreader.EPubs.getEPubById(epub_id).getSemAppId();
					url = "http://epubdummy.provideal.net/api/semapps/"+ 
			    			semapp_id + "/epubs/" + epub_id + "/annotations/" + annotation_id; 
					
					ConnectionManager conn = ConnectionManager.getInstance();
					conn.authenticate(username, password);
					connectionResult = conn.postStuffGet(url);
					resEntityPost = (HttpEntity) connectionResult[0];
					myStatusCode = (String) connectionResult[1];
					
					if (myStatusCode.equals(conn.AUTHENTICATION_FAILED) ||
							myStatusCode.equals(conn.NO_INTERNET_CONNECTION)) {
						return;
					}
				}
				
				fbreader.Annotations.removeAnnotation(annotation);
				BaseActivity.hideAnnotationSelectionPanel();
				fbreader.BookTextView.removeAnnotationHighlight(annotation);
				
				Uri uri = DBAnnotations.CONTENT_URI;
				String selection = DBAnnotations.ANNOTATION_ID + "=\"" + annotation.getId() + "\"";
				BaseActivity.getContentResolver().delete(uri, selection, null);
				
				fbreader.Annotations.removeAnnotation(annotation);
				fbreader.BookTextView.removeAnnotationHighlight(annotation);
				
				// TODO hier entfernen
				// alles andere auskommentieren, dann kann man selber bestimmte annotationen löschen
				
//				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/4f25be9ed0434c6570000013");
//				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/4f25bab5d0434c6570000012");
			}
			
			private void deleteByHand(String url) {
				username = "admin";
				password = "123456";
				
				ConnectionManager conn = ConnectionManager.getInstance();
				conn.authenticate(username, password);
				connectionResult = conn.postStuffGet(url);
				resEntityPost = (HttpEntity) connectionResult[0];
			}
		}).start();
	}
}
