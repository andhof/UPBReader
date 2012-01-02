package org.geometerplus.android.fbreader.semapps;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.android.fbreader.semapps.model.SemAppDummy;
import org.geometerplus.android.fbreader.semapps.model.SemApps;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class SemAppsListActivity extends ListActivity {
	
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
				R.layout.simple_list_item_1, semAppNamesList);
		setListAdapter(adapter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		finishActivity(5);
		finishActivity(4);
		
		
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == 4) {
	        finish();
	    }
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
//		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		
		if (asyncTask != null) asyncTask.cancel(true);
		progressDialog = new ProgressDialog(SemAppsListActivity.this);
		progressDialog.setMessage("Downloading Book...");
	    asyncTask = new HttpHelper();
	    asyncTask.execute("http://epubdummy.provideal.net/api/semapps/"+semAppIDsList.get(position));
	    
	}
	
	private class HttpHelper extends AsyncTask<String, Void, String> {

		private HttpClient client;
		private String getURL;
		private HttpGet get;
		private HttpResponse responseGet;
		private HttpEntity resEntityGet;
		private String resEntityGetResult;
		
		protected void onPreExecute() {
            progressDialog.show();
        }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				client = new DefaultHttpClient();  
		        getURL = params[0];
		        get = new HttpGet(getURL);
		        responseGet = client.execute(get);  
		        resEntityGet = responseGet.getEntity();  
		        resEntityGetResult = EntityUtils.toString(resEntityGet);
			} catch (Exception e) {
			    e.printStackTrace();
			    Log.e("UPBLibraryLoginActivity", e.toString());
			} finally {
                if (get != null) 
                	get.abort();
            }
			return resEntityGetResult;
		}
		
		protected void onPostExecute(String result) {
			if (progressDialog.isShowing()) {
                progressDialog.dismiss();
			}
			
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			Log.v("UPBLibraryLoginActivity.HttpHelper", result);
			SemApp semApp = loadSemAppFromXMLString(result);
			
			SemAppsListActivity.this.startActivityForResult(
				new Intent(SemAppsListActivity.this.getApplicationContext(), EPubListActivity.class)
					.putExtra("semapp", semApp), 
				4
			);
		}
		
		/**
		 * load an XML String of annotations into the annotations object structure
		 * @param xml
		 */
		public SemApp loadSemAppFromXMLString(String xml) {
			SemApp semApp = null;
			try {
				Serializer serializer = new Persister();
//				xml = "<?xml version='1.0' encoding='UTF-8'?>\n<semapps type='array'>" +
//						"\n  <semapp>\n    <id>4eef5aadd0434c1fa6000001</id>\n    <name>" +
//						"Test Semapp</name>\n    <updated_at>2011-12-19 16:39:25 +0100" +
//						"</updated_at>\n  </semapp>\n<semapp>\n    <id>4eef5aadd0434c1f" +
//						"a6000002</id>\n    <name>Test Semapp2</name>\n    <updated_at>201" +
//						"1-12-19 16:39:25 +0100</updated_at>\n  </semapp>\n<semapp>\n    " +
//						"<id>4eef5aadd0434c1fa6000003</id>\n    <name>Test Semapp3</name" +
//						">\n    <updated_at>2011-12-19 16:39:25 +0100</updated_at>\n  </sem" +
//						"app>\n</semapps>\n";
	    		semApp = serializer.read(SemApp.class, xml);
	    		System.out.println();
	    	} catch (Exception e) {
	    		Log.e("loadFromXMLString", e.toString());
	    	}
	    	return semApp;
		}
	}
}