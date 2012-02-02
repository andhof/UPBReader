package org.geometerplus.android.fbreader.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotation;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AnnotationService extends Service {

	private final IBinder mBinder = new MyBinder();
	
	private String username;
	private String password;
	private ConnectionManager conn;
	private Object[] connectionResult;
	private HttpEntity resEntityPost;
	private HttpEntity resEntityPut;
	private HttpEntity resEntityDelete;
	private int myStatusCode;
	private String resEntityPostResult;
	
	private SharedPreferences settings;
	private Set<String> urlset;
	private ArrayList<String> urlList;
	private String xml;
	private int successfulJobs;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		conn = ConnectionManager.getInstance();
		
		settings = getSharedPreferences("upblogin", 0);
		username = settings.getString("user", "Localuser");
		password = settings.getString("password", null);
		
		Log.v("AnnotationService", "Der Service läuft soweit!");
		
		settings = getSharedPreferences("annotation_stack", 0);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				String annotation_id = "";
				String upb_id = "";
				String updated_at = "";
				SharedPreferences.Editor edit = settings.edit();
				successfulJobs = 0; 
				Iterator<String> it;
				
				// Add new annotations to the server
				myStatusCode = -1;
				urlset = settings.getStringSet("add", new HashSet<String>());
				urlList = new ArrayList<String>(urlset);
				
				conn.authenticate(username, password);
				
				it = urlList.iterator();
				while(it.hasNext()){
					String current = it.next();
					
					Log.v("AnnotationService", "add: "+ current);
					annotation_id = current.substring(current.lastIndexOf("/")+1);
					current = current.substring(0, current.lastIndexOf("/"));
					Annotation annotation = fbreader.Annotations.getAnnotationById(annotation_id);
					xml = fbreader.saveAnnotationToString(annotation);
					
					try {
						connectionResult = conn.postStuffPost(current, xml);
						resEntityPost = (HttpEntity) connectionResult[0];
						myStatusCode = ((Integer) connectionResult[1]).intValue();
						Log.v("AnnotationService", "statuscode: "+myStatusCode);
						
						if (resEntityPost != null) {
							resEntityPostResult = EntityUtils.toString(resEntityPost);
							SemAppsAnnotation saAnnotation = 
								fbreader.loadAnnotationFromXMLString(resEntityPostResult);
							upb_id = saAnnotation.getId();
							updated_at = saAnnotation.getUpdated_at();
							annotation.setUPBId(upb_id);
							annotation.setUpdatedAt(updated_at);
							
							fbreader.writeAnnotationToDatabase(AnnotationService.this, annotation, annotation.getEPubId());
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (myStatusCode == conn.OK) {
						it.remove();
						successfulJobs++;
					}
				}
				
				urlset = new HashSet<String>(urlList);
				edit = settings.edit();
				edit.putStringSet("add", urlset);
				edit.commit();
				
				// Update annotations on server
				myStatusCode = -1;
				urlset = settings.getStringSet("update", new HashSet<String>());
				urlList = new ArrayList<String>(urlset);
				
				it = urlList.iterator();
				while(it.hasNext()){
					String current = it.next();
					
					Log.v("AnnotationService", "update: "+ current);
					upb_id = current.substring(current.lastIndexOf("/")+1);
					Annotation annotation = fbreader.Annotations.getAnnotationByUPBId(upb_id);
					xml = fbreader.saveAnnotationToString(annotation);
					
					connectionResult = conn.postStuffPut(current, xml);
					resEntityPut = (HttpEntity) connectionResult[0];
					myStatusCode = ((Integer) connectionResult[1]).intValue();
					if (resEntityPut != null) {
						try {
							resEntityPut.consumeContent();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (myStatusCode == conn.OK) {
						it.remove();
						successfulJobs++;
					}
				}
				
				urlset = new HashSet<String>(urlList);
				edit = settings.edit();
				edit.putStringSet("update", urlset);
				edit.commit();
				
				// Remove the former annotations from server  
				myStatusCode = -1;
				urlset = settings.getStringSet("delete", new HashSet<String>());
				urlList = new ArrayList<String>(urlset);
				
				it = urlList.iterator();
				while(it.hasNext()){
					String current = it.next();
					
					Log.v("AnnotationService", "delete: "+ current);
					connectionResult = conn.postStuffDelete(current);
					resEntityDelete = (HttpEntity) connectionResult[0];
					myStatusCode = ((Integer) connectionResult[1]).intValue();
					if (resEntityDelete != null) {
						try {
							resEntityDelete.consumeContent();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (myStatusCode == conn.OK) {
						it.remove();
						successfulJobs++;
					}
				}
				
				urlset = new HashSet<String>(urlList);
				edit.putStringSet("delete", urlset);
				edit.commit();
				
				if (successfulJobs > 0) {
					Handler h = new Handler(AnnotationService.this.getMainLooper());

				    h.post(new Runnable() {
				        @Override
				        public void run() {
				        	Log.v("AnnotationService", "Toast wird angezeigt. Fertig.");
				            Toast.makeText(AnnotationService.this, "UPBReader - " + successfulJobs + " Jobs processed.", Toast.LENGTH_LONG).show();
				        }
				    });
				}
			}
			
		}).start();
		
		stopSelf();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public class MyBinder extends Binder {
		AnnotationService getService() {
			return AnnotationService.this;
		}
	}
}