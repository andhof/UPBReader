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
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.android.fbreader.semapps.model.SemApps;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.XMLUtil;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;


public class UPBLibraryLoginActivity extends Activity {
	private ZLResource myResource;
	private Button myOkButton;
	private Button cancelButton;
	private static final String TAG = "SelectionHighlightActivity";
	private HttpHelper asyncTask;
	private ProgressDialog progressDialog;
	private boolean checked;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		
		setContentView(R.layout.upb_login);
		
		SharedPreferences settings = getSharedPreferences("upblogin", 0);
		String username = settings.getString("user", null);
		String password = settings.getString("password", null);
		
		setTitle(R.string.upblogin_title);
		
		findTextView(R.id.authentication_username_label).setText(R.string.upblogin_login);
		final EditText userInput = (EditText) findViewById(R.id.authentication_username);
		if (username != null) userInput.setText(username);
		findTextView(R.id.authentication_password_label).setText(R.string.upblogin_password);
		final EditText passwordInput = (EditText) findViewById(R.id.authentication_password);
		if (password != null) passwordInput.setText(password);
		final CheckBox checkBox = (CheckBox) findViewById(R.id.authentication_download_checkbox);
		checkBox.setChecked(false);
		findTextView(R.id.authentication_download_label).setText(R.string.upblogin_checkbox);
		
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		
		final View buttonsView = findViewById(R.id.authentication_buttons);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		myOkButton = (Button)buttonsView.findViewById(R.id.ok_button);
		myOkButton.setText(buttonResource.getResource("ok").getValue());
		myOkButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				checked = checkBox.isChecked() ? true : false;
				int usersize = userInput.getText().length();
                int passsize = passwordInput.getText().length();
				if(usersize > 0 && passsize > 0) {
					progressDialog = new ProgressDialog(UPBLibraryLoginActivity.this);
					if (checked) {
						progressDialog.setMessage(getApplicationContext().getText(R.string.loadingsemlist));
					} else {
						progressDialog.setMessage(getApplicationContext().getText(R.string.loggingin));
					}
					
				    asyncTask.execute("http://epubdummy.provideal.net/api/semapps", 
				    		userInput.getText().toString(), passwordInput.getText().toString());
				} else {
					UIUtil.createDialog(UPBLibraryLoginActivity.this, "Error","Please enter Username and Password");
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
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
        return asyncTask;
    }
	
	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
	}
	
	private class HttpHelper extends AsyncTask<String, Void, String> {

		UPBLibraryLoginActivity mActivity;
		
		private Object[] connectionResult;
		private HttpEntity resEntityGet;
		private int myStatusCode;
		private String resEntityGetResult;
		private ConnectionManager conn;
		
		HttpHelper(UPBLibraryLoginActivity activity) {
            mActivity = activity;
        }
		
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
				
				conn = ConnectionManager.getInstance();
				conn.authenticate(user, password);
				connectionResult = conn.postStuffGet(getURL);
				resEntityGet = (HttpEntity) connectionResult[0];
				myStatusCode = ((Integer) connectionResult[1]).intValue();
				
				if (myStatusCode == conn.AUTHENTICATION_FAILED ||
						myStatusCode == conn.NO_INTERNET_CONNECTION) {
					return null;
				}
				
		    	SharedPreferences settings = getSharedPreferences("upblogin", 0);
		    	SharedPreferences.Editor e = settings.edit();
		    	e.putString("user", user);
		    	e.putString("password", password);
		    	e.commit();
				
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
				UIUtil.createDialog(UPBLibraryLoginActivity.this, "Error", getString(R.string.authentication_failed));
				asyncTask = new HttpHelper(UPBLibraryLoginActivity.this);
				return;
			}
			if (myStatusCode == conn.NO_INTERNET_CONNECTION) {
				UIUtil.createDialog(UPBLibraryLoginActivity.this, "Error", getString(R.string.no_internet_connection));
				asyncTask = new HttpHelper(UPBLibraryLoginActivity.this);
				return;
			}
			finish();
			if (!checked) {
				return;
			}
			
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			Log.v("UPBLibraryLoginActivity.HttpHelper", result);
			SemApps semApps = XMLUtil.loadSemAppsFromXMLString(result);
			
			UPBLibraryLoginActivity.this.startActivityForResult(
				new Intent(UPBLibraryLoginActivity.this.getApplicationContext(), SemAppsListActivity.class)
					.putExtra("semapps", semApps),
				4
			);
		}
	}
}