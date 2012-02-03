package org.geometerplus.android.fbreader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.database.AnnotationsDbAdapter;
import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.services.AnnotationService;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import de.upb.android.reader.R;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
	private ArrayList<Annotation> annotationsToRemove;
	private SharedPreferences settings;
	
	SelectionRemoveAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		this.fbreader = fbreader;
	}

	@Override
	protected void run(Object... params) {
		final Annotation annotation = (Annotation) params[0];
		BaseActivity.hideAnnotationSelectionPanel();
		fbreader.BookTextView.removeAnnotationHighlight(annotation);
		annotationsToRemove = new ArrayList<Annotation>();
		annotationsToRemove.add(annotation);
		annotationsToRemove.addAll(
				fbreader.Annotations.getAnnotationsByTargetAnnotationId(annotation.getUPBId()));
		fbreader.Annotations.getAnnotations().removeAll(annotationsToRemove);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Set<String> urlset = new HashSet<String>();
				
				for (Annotation a : annotationsToRemove) {
					Uri uri = DBAnnotations.CONTENT_URI;
					String selection = DBAnnotations.ANNOTATION_ID + "=\"" + a.getId() + "\"";
					BaseActivity.getContentResolver().delete(uri, selection, null);
				}
				
				Log.v("SelectionRemoveAnnotationAction", "remove count: "+annotationsToRemove.size());
				
				conn = ConnectionManager.getInstance();
				if (annotation.getUPBId() != null && !annotation.getUPBId().isEmpty()) {
					settings = BaseActivity.getSharedPreferences("upblogin", 0);
					username = settings.getString("user", "Localuser");
					password = settings.getString("password", null);
					
					conn.authenticate(username, password);
					
					String epub_id = annotation.getEPubId();
					String semapp_id = fbreader.EPubs.getEPubById(epub_id).getSemAppId();
					for (Annotation a : annotationsToRemove) {
						url = "http://epubdummy.provideal.net/api/semapps/"+ 
							semapp_id + "/epubs/" + epub_id + "/annotations/" + a.getUPBId();
						
						connectionResult = conn.postStuffDelete(url);
						resEntityPost = (HttpEntity) connectionResult[0];
						myStatusCode = ((Integer) connectionResult[1]).intValue();
						
						if (resEntityPost != null) {
							try {
								resEntityPost.consumeContent();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						if (myStatusCode == conn.AUTHENTICATION_FAILED) {
							Log.v("SelectionRemoveAnnotationAction", "Authentication failed. Return.");
							return;
						}
						
						if (myStatusCode != conn.OK) {
							settings = BaseActivity.getSharedPreferences("annotation_stack", 0);
							urlset = settings.getStringSet("delete", new HashSet<String>());
							urlset.add(url);
						}
					}
				}
				settings = BaseActivity.getSharedPreferences("annotation_stack", 0);
				SharedPreferences.Editor e = settings.edit();
				e.putStringSet("delete", urlset);
				e.commit();
				
				if (myStatusCode == conn.NO_INTERNET_CONNECTION) {
					Handler h = new Handler(BaseActivity.getMainLooper());

				    h.post(new Runnable() {
				        @Override
				        public void run() {
				        	Log.v("AnnotationService", "Toast wird angezeigt. Fertig.");
				            Toast.makeText(BaseActivity, BaseActivity.getString(R.string.toast_remove_noconnection), Toast.LENGTH_LONG).show();
				        }
				    });
					return;
				}
				
				// TODO hier entfernen
				// alles andere auskommentieren, dann kann man selber bestimmte annotationen löschen
				
//				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
//						"4f2ac582d0434c0f04000029");
//				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
//						"4f2ac585d0434c0f0400002a");
//				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
//						"4f2ac4acd0434c0f04000027");
//				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
//						"4f2ac581d0434c0f04000028");
			}
			
			private void deleteByHand(String url) {
				username = "user1";
				password = "123456";
				
				conn = ConnectionManager.getInstance();
				conn.authenticate(username, password);
				connectionResult = conn.postStuffDelete(url);
				resEntityPost = (HttpEntity) connectionResult[0];
				if (resEntityPost != null) {
					try {
						resEntityPost.consumeContent();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
