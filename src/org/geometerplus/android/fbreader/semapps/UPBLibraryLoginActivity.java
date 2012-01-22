package org.geometerplus.android.fbreader.semapps;

import android.app.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import de.upb.android.reader.R;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.android.fbreader.annotation.database.AnnotationsDbAdapter;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.android.fbreader.semapps.model.SemApps;
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
		
		findTextView(R.id.authentication_username_label).setText(R.string.upblogin_login);
		final EditText userInput = (EditText) findViewById(R.id.authentication_username);
		findTextView(R.id.authentication_password_label).setText(R.string.upblogin_password);
		final EditText passwordInput = (EditText) findViewById(R.id.authentication_password);
		
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		
		final View buttonsView = findViewById(R.id.authentication_buttons);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		myOkButton = (Button)buttonsView.findViewById(R.id.ok_button);
		myOkButton.setText(buttonResource.getResource("ok").getValue());
		myOkButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				int usersize = userInput.getText().length();
                int passsize = passwordInput.getText().length();
				if(usersize > 0 && passsize > 0) {
					if (asyncTask != null) asyncTask.cancel(true);
					progressDialog = new ProgressDialog(UPBLibraryLoginActivity.this);
					progressDialog.setMessage(getApplicationContext().getText(R.string.loadingsemlist));
				    asyncTask = new HttpHelper();
				    asyncTask.execute("http://epubdummy.provideal.net/api/semapps", 
				    		userInput.getText().toString(), passwordInput.getText().toString());
	//			    asyncTask.execute("http://epubdummy.provideal.net/api/semapps/4eef5aadd0434c1fa6000001");
	//				finish();
				} else {
					createDialog("Error","Please enter Username and Password");
				}
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
	
	private void createDialog(String title, String text) {
        AlertDialog ad = new AlertDialog.Builder(this)
        .setPositiveButton("Ok", null)
        .setTitle(title)
        .setMessage(text)
        .create();
        ad.show();
    }
	
	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
	}
	
	private class HttpHelper extends AsyncTask<String, Void, String> {

		private HttpParams httpParams;
		private DefaultHttpClient httpClient;
		private HttpGet get;
		private HttpResponse responseGet;
		private HttpEntity resEntityGet;
		private String resEntityGetResult;
		
		@Override
		protected void onPreExecute() {
            progressDialog.show();
        }
		
		@Override
		protected String doInBackground(String... params) {
			try {
				String getURL = params[0];
				String user = params[1];
				String password = params[2];
				
				ConnectionManager conn = ConnectionManager.getInstance();
				conn.authenticate(user, password);
				resEntityGet = conn.postStuff(getURL);
				if (resEntityGet == null) {
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
			if (result == null) {
				createDialog("Error","Wrong Username or Password");
				return;
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
		private SemApps loadSemAppsListFromXMLString(String xml) {
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