package org.geometerplus.android.fbreader.semapps;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.database.AnnotationsDbAdapter;
import org.geometerplus.android.fbreader.annotation.database.DBEPub.DBEPubs;
import org.geometerplus.android.fbreader.annotation.database.DBSemApp.DBSemApps;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.android.fbreader.semapps.model.SemAppDummy;
import org.geometerplus.android.fbreader.semapps.model.SemApps;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.upb.android.reader.R;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SemAppsListActivity extends ListActivity {
	private Cursor cursor;
	
	private HttpHelper asyncTask;
	ArrayList<String> semAppNamesList = new ArrayList<String>();
	ArrayList<String> semAppIDsList = new ArrayList<String>();
	private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Intent intent = getIntent();
		SemApps semApps = intent.getParcelableExtra("semapps");

		ArrayList<SemAppDummy> semAppsList = semApps.getSemApps();
		for (SemAppDummy semApp : semAppsList) {
			semAppNamesList.add(semApp.getName());
			semAppIDsList.add(semApp.getId());
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, semAppNamesList);
		setListAdapter(adapter);
		
		asyncTask = (HttpHelper) getLastNonConfigurationInstance();
        if (asyncTask != null) {
        	asyncTask.mActivity = this;
        } else {
        	if (asyncTask != null) asyncTask.cancel(true);
        	asyncTask = new HttpHelper(this);
        }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (asyncTask != null) {
        	asyncTask.mActivity = null;
        }
		
		finishActivity(5);
		finishActivity(4);
		
		
		finish();
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
        return asyncTask;
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == 4) {
	        finish();
	    }
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
//		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		
		progressDialog = new ProgressDialog(SemAppsListActivity.this);
		progressDialog.setMessage(this.getText(R.string.loadingepublist));

		asyncTask.execute("http://epubdummy.provideal.net/api/semapps/"+semAppIDsList.get(position));
	}
	
	private class HttpHelper extends AsyncTask<String, Void, String> {

		SemAppsListActivity mActivity;
		
		private String getURL;
		private ConnectionManager conn;
		private HttpEntity resEntityGet;
		private String resEntityGetResult;
		private Object[] connectionResult;
		private int myStatusCode;
		
		HttpHelper(SemAppsListActivity activity) {
            mActivity = activity;
        }
		
		@Override
		protected void onPreExecute() {
            progressDialog.show();
        }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				getURL = params[0];
				
				conn = ConnectionManager.getInstance();
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
			if (progressDialog.isShowing()) {
                progressDialog.dismiss();
			}
			if (myStatusCode == conn.AUTHENTICATION_FAILED) {
				UIUtil.createDialog(SemAppsListActivity.this, "Error", getString(R.string.authentication_failed));
				asyncTask = new HttpHelper(SemAppsListActivity.this);
				return;
			}
			if (myStatusCode == conn.NO_INTERNET_CONNECTION) {
				UIUtil.createDialog(SemAppsListActivity.this, "Error", getString(R.string.no_internet_connection));
				asyncTask = new HttpHelper(SemAppsListActivity.this);
				return;
			}
			
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			Log.v("UPBLibraryLoginActivity.HttpHelper", result);
			SemApp semApp = loadSemAppFromXMLString(result);
			SemAppDummy semAppDummy = new SemAppDummy();
			semAppDummy.setId(semApp.getId());
			semAppDummy.setName(semApp.getName());
			semAppDummy.setUpdatedAt(semApp.getUpdated_at());
			if (!fbreader.SemApps.getSemApps().contains(semAppDummy)) {
		    	fbreader.SemApps.getSemApps().add(new SemAppDummy());
		    }
			
			writeSemAppToDatabase(semApp);
			
			SemAppsListActivity.this.startActivityForResult(
				new Intent(SemAppsListActivity.this.getApplicationContext(), EPubListActivity.class)
					.putExtra("semapp", semApp), 
				4
			);
		}
		
		/**
		 * Insert a SemApp into the database 
		 * @param semApp
		 */
		private void writeSemAppToDatabase(SemApp semApp) {
			final String id = semApp.getId();
			final String name = semApp.getName();
			final String updated_at = semApp.getUpdated_at();
			
			// working on database in a different thread
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					String[] projection = DBSemApps.Projection;
					Uri uri = DBSemApps.CONTENT_URI;
					cursor = getContentResolver().query(uri, projection, null, null, null);
					
					ContentValues values = new ContentValues();
					values.put(DBSemApps.SEMAPP_ID, id);
					values.put(DBSemApps.NAME, name);
					values.put(DBSemApps.UPDATED_AT, updated_at);
					if (cursor.getCount() == 0) {
						getContentResolver().insert(uri, values);
					} else {
						getContentResolver().update(uri, values, null, null);
					}
					cursor.close();
				}
			}).start();
			
		}
		
		/**
		 * load an XML String of semapps into the semapps object structure
		 * @param xml
		 */
		public SemApp loadSemAppFromXMLString(String xml) {
			SemApp semApp = null;
			try {
				Serializer serializer = new Persister();
	    		semApp = serializer.read(SemApp.class, xml);
	    		System.out.println();
	    	} catch (Exception e) {
	    		Log.e("loadFromXMLString", e.toString());
	    	}
	    	return semApp;
		}
	}
}