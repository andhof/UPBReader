package org.geometerplus.android.fbreader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.SelectionNoteActivity;
import org.geometerplus.android.fbreader.annotation.database.DBAnnotation.DBAnnotations;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.services.AnnotationService;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import de.upb.android.reader.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class SelectionRemoveAnnotationAction extends FBAndroidAction {
	private FBReaderApp fbreader;
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
	private boolean waitingJobs;
	
	SelectionRemoveAnnotationAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		this.fbreader = fbreader;
	}

	@Override
	protected void run(final Object... params) {
		AlertDialog confirmationDialog = new AlertDialog.Builder(BaseActivity)
        .setCancelable(false)
        .setTitle(R.string.confirmation_title)
        .setMessage(R.string.confirmation_message)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   final Annotation annotation = (Annotation) params[0];
	       		BaseActivity.hideAnnotationSelectionPanel();
	       		waitingJobs = false;
	       		
	       		settings = BaseActivity.getSharedPreferences("upblogin", 0);
	       		username = settings.getString("user", "Localuser");
	       		password = settings.getString("password", null);
	       		
	       		// Only delete if the user has the rights to do it
	//       		if (!annotation.getAuthor().getName().equals(username)) {
	//       			UIUtil.createDialog(BaseActivity, "Error", BaseActivity.getString(R.string.not_allowed));
	//       			return;
	//       		}
	       		
	       		annotationsToRemove = new ArrayList<Annotation>();
	       		annotationsToRemove.add(annotation);
	       		annotationsToRemove.addAll(
	       				fbreader.Annotations.getAnnotationsByTargetAnnotationId(annotation.getUPBId()));
	       		
	       		new Thread(new Runnable() {
	       			
	       			@Override
	       			public void run() {
	       				Set<String> urlset = new HashSet<String>();
	       				
	       				Log.v("SelectionRemoveAnnotationAction", "remove count: "+annotationsToRemove.size());
	       				
	       				conn = ConnectionManager.getInstance();
	       				conn.authenticate(username, password);
	       				
	       				if (annotation.getUPBId() > 0) {
	       					for (Annotation a : annotationsToRemove) {
	       						url = "http://epubdummy.provideal.net/api/annotations/" + a.getUPBId();
	       						
	       						if (!fbreader.isNetworkAvailable()) {
	       							waitingJobs = true;
	       							settings = BaseActivity.getSharedPreferences("annotation_stack", 0);
	       							urlset = settings.getStringSet("delete", new HashSet<String>());
	       							urlset.add(url);
	       							continue;
	       						}
	       						
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
	       						
	       						if (myStatusCode == conn.OK) {
	       							Log.v("SelectionRemoveAnnotationAction", "Berechtigung zum Entfernen einer Annotation gegeben");
	       							Uri uri = DBAnnotations.CONTENT_URI;
	       							String selection = DBAnnotations.UPB_ID + "=\"" + a.getUPBId() + "\"";
	       							BaseActivity.getContentResolver().delete(uri, selection, null);
	       							fbreader.Annotations.getAnnotations().remove(a);
	       							fbreader.BookTextView.removeAnnotationHighlight(annotation);
	       						} else {
	       							if (myStatusCode == conn.FORBIDDEN) {
	       								if (a.equals(annotation)) {
	       									UIUtil.createDialog(BaseActivity, "Error", BaseActivity.getString(R.string.not_allowed));
	       									fbreader.BookTextView.clearSelectionHighlight();
		       								fbreader.BookTextView.repaintAll();
	       								}
	       								continue;
	       							}
	       						}
	       					}
	       					settings = BaseActivity.getSharedPreferences("annotation_stack", 0);
	       					SharedPreferences.Editor e = settings.edit();
	       					e.putStringSet("delete", urlset);
	       					e.commit();
	       				} else {
	       					fbreader.BookTextView.removeAnnotationHighlight(annotation);
	       					for (Annotation a : annotationsToRemove) {
	       						Uri uri = DBAnnotations.CONTENT_URI;
	       						String selection = DBAnnotations.ANNOTATION_ID + "=\"" + a.getId() + "\"";
	       						BaseActivity.getContentResolver().delete(uri, selection, null);
	       						fbreader.Annotations.getAnnotations().remove(a);
	       					}
	       				}
	       				
	       				if (waitingJobs) {
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
	       				
	//       				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
	//       						"4f2bc93bd0434c0f04000038");
	//       				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
	//       						"4f2bc94dd0434c0f04000039");
	//       				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
	//       						"4f3059fad0434c6fa0000002");
	//       				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
	//       						"4f305abfd0434c6fa0000003");
	//       				deleteByHand("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001/epubs/4eef5aadd0434c1fa6000002/annotations/" +
	//       						"4f305af9d0434c6fa0000004");
	       			}
	       			
	       			private void deleteByHand(String url) {
	    				username = "admin";
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
        })
       	.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
           }
       	})
       	.create();
		confirmationDialog.show();
	}
}
