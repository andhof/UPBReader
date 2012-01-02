package org.geometerplus.android.fbreader.semapps;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import android.app.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import de.upb.android.reader.R;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.android.fbreader.semapps.model.SemApps;
import org.geometerplus.android.util.UIUtil;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;


public class UPBLibraryLoginActivity extends Activity {
	
	private ZLResource myResource;
	private Button myOkButton;
	private Button cancelButton;
	private static final String TAG = "SelectionHighlightActivity";
	private HttpHelper asyncTask;
	private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		
		setContentView(R.layout.upb_login);
		
		myResource = ZLResource.resource("dialog").getResource("UPBLoginDialog");
		
		setTitle(myResource.getResource("title").getValue());
		
		findTextView(R.id.authentication_username_label).setText(
			myResource.getResource("login").getValue()
		);
		findTextView(R.id.authentication_password_label).setText(
			myResource.getResource("password").getValue()
		);
		
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		
		final View buttonsView = findViewById(R.id.authentication_buttons);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		myOkButton = (Button)buttonsView.findViewById(R.id.ok_button);
		myOkButton.setText(buttonResource.getResource("ok").getValue());
		myOkButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (asyncTask != null) asyncTask.cancel(true);
				progressDialog = new ProgressDialog(UPBLibraryLoginActivity.this);
				progressDialog.setMessage("Loading Seminarapparate List...");
			    asyncTask = new HttpHelper();
			    asyncTask.execute("http://epubdummy.provideal.net/api/semapps");
//			    asyncTask.execute("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001");
//				finish();
			}
		});
		
		cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
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
//		        if (resEntityGet != null) {  
//			        //do something with the response
//			        Log.i("GET RESPONSE",EntityUtils.toString(resEntityGet));
//		        }
			} catch (Exception e) {
			    e.printStackTrace();
			    Log.e("UPBLibraryLoginActivity", e.toString());
			} finally {
                // auf jeden Fall Verbindung beenden
                if (get != null) get.abort();
                // if (httpClient != null) httpClient.close();
            }
			return resEntityGetResult;
		}
		
		protected void onPostExecute(String result) {
			if (progressDialog.isShowing()) {
                progressDialog.dismiss();
			}
			finish();
			
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			Log.v("UPBLibraryLoginActivity.HttpHelper", result);
			SemApps semApps = loadSemAppsListFromXMLString(result);
			
			UPBLibraryLoginActivity.this.startActivityForResult(
				new Intent(UPBLibraryLoginActivity.this.getApplicationContext(), SemAppsListActivity.class)
					.putExtra("semapps", semApps),
				4
			);
		}
		
		/**
		 * load an XML String of annotations into the annotations object structure
		 * @param xml
		 */
		public SemApps loadSemAppsListFromXMLString(String xml) {
			SemApps semApps = null;
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
	    		semApps = serializer.read(SemApps.class, xml);
	    		System.out.println();
	    	} catch (Exception e) {
	    		Log.e("loadFromXMLString", e.toString());
	    	}
	    	return semApps;
		}
	}
}