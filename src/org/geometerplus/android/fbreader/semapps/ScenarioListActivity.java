package org.geometerplus.android.fbreader.semapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.annotation.model.Annotations;
import org.geometerplus.android.fbreader.annotation.model.TargetAuthor;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
import org.geometerplus.android.fbreader.semapps.model.EPubs;
import org.geometerplus.android.fbreader.semapps.model.Scenario;
import org.geometerplus.android.fbreader.semapps.model.Scenarios;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotation;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.android.fbreader.semapps.model.SemAppsAnnotations;
import org.geometerplus.android.util.SQLiteUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.XMLUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.upb.android.reader.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ScenarioListActivity extends ListActivity {
	private HttpHelper asyncTask;
	Scenarios scenarios;
	Scenario scenario;
	ArrayList<Scenario> scenarioList;
	ArrayList<String> scenarioNamesList = new ArrayList<String>();
	ArrayList<Integer> scenarioIdsList = new ArrayList<Integer>();
	ArrayList<SemAppsAnnotations> annotationsList = new ArrayList<SemAppsAnnotations>();
	
	String annotationsXMLString;
	String local_path;
	
	ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		Intent intent = getIntent();
		scenarios = intent.getParcelableExtra("scenarios");
		
		setTitle(getString(R.string.scenarioslist_title));

		scenarioList = scenarios.getScenarios();
		for (Scenario scenario : scenarioList) {
			scenarioNamesList.add(scenario.getName());
			scenarioIdsList.add(scenario.getId());
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, scenarioNamesList);
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
    public Object onRetainNonConfigurationInstance() {
        return asyncTask;
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        if (asyncTask != null) {
        	asyncTask.mActivity = null;
        }
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		String item = (String) getListAdapter().getItem(position);
//		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		
	    // save book information in database
	    scenario = scenarioList.get(position);
	    if (!fbreader.Scenario.equals(scenario)) {
	    	fbreader.Scenario = scenario;
	    }
	    SQLiteUtil.writeScenarioToDatabase(ScenarioListActivity.this, scenario);
	    
	    progressDialog = new ProgressDialog(ScenarioListActivity.this);
		progressDialog.setMessage(this.getText(R.string.loadingscenariolist));
        
        asyncTask.execute(
        		"http://epubdummy.provideal.net/api/scenarios/" + scenarioIdsList.get(position) + "/annotations");
	}
	
	private void openBook(Book book) {
		startActivity(
			new Intent(getApplicationContext(), FBReader.class)
				.setAction(Intent.ACTION_VIEW)
				.putExtra(FBReader.BOOK_PATH_KEY, book.File.getPath())
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		);
	}
	
	private class HttpHelper extends AsyncTask<String, Integer, String> {

		ScenarioListActivity mActivity;

		private ConnectionManager conn;
		private HttpEntity resEntityGet;
		private String resEntityGetResult;
		private Object[] connectionResult;
		private int myStatusCode;
		private String filePath;
		
		HttpHelper(ScenarioListActivity activity) {
            mActivity = activity;
        }
		
		@Override
		protected void onPreExecute() {
            progressDialog.show();
        }
		
		@Override
		protected String doInBackground(String ... params) {
			
			try {
				String getURL = params[0];
				
				conn = ConnectionManager.getInstance();
				connectionResult = conn.postStuffGet(getURL);
				resEntityGet = (HttpEntity) connectionResult[0];
				myStatusCode = ((Integer) connectionResult[1]).intValue();
				if (myStatusCode == conn.AUTHENTICATION_FAILED ||
						myStatusCode == conn.NO_INTERNET_CONNECTION) {
					return null;
				}
		        resEntityGetResult = EntityUtils.toString(resEntityGet);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return resEntityGetResult;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressDialog.setProgress(progress[0]);
	    }
		
		@Override
		protected void onPostExecute(String result) {
			if (progressDialog.isShowing()) {
                progressDialog.dismiss();
			}
			if (myStatusCode == conn.AUTHENTICATION_FAILED) {
				UIUtil.createDialog(ScenarioListActivity.this, "Error", getString(R.string.authentication_failed));
				asyncTask = new HttpHelper(ScenarioListActivity.this);
				return;
			}
			if (myStatusCode == conn.NO_INTERNET_CONNECTION) {
				UIUtil.createDialog(ScenarioListActivity.this, "Error", getString(R.string.no_internet_connection));
				asyncTask = new HttpHelper(ScenarioListActivity.this);
				return;
			}
//			if (result == null) {
//				UIUtil.createDialog(EPubListActivity.this, "Error", getString(R.string.complete_file_exists));
//			}
			setResult(5);
			finish();
			
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			SemAppsAnnotations semAppsAnnotations = XMLUtil.loadSemAppsAnnotationsFromXMLString(result);
			
			// add the missing annotations to local structure
			Annotation annotation;
			int upb_id = -1;
			String updated_at = "";
			for (SemAppsAnnotation a : semAppsAnnotations.getAnnotations()) {
				String data = a.getData();
				if (data.isEmpty()) {
					continue;
				}
				annotation = XMLUtil.loadAnnotationFromXMLString(data);
				upb_id = a.getId();
				updated_at = a.getUpdated_at();
				if (!fbreader.Annotations.getAnnotations().contains(annotation)) {
					annotation.setUPBId(upb_id);
					annotation.setUpdatedAt(updated_at);
					fbreader.Annotations.addAnnotation(annotation);
					SQLiteUtil.writeAnnotationToDatabase(ScenarioListActivity.this, annotation, scenario.getEPubId());
				}
			}
			
			fbreader.loadAnnotationHighlighting();
		}
	}
}