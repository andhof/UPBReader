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
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.annotation.database.AnnotationsDbAdapter;
import org.geometerplus.android.fbreader.annotation.model.Annotation;
import org.geometerplus.android.fbreader.annotation.model.Annotations;
import org.geometerplus.android.fbreader.annotation.model.TargetAuthor;
import org.geometerplus.android.fbreader.httpconnection.ConnectionManager;
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

public class EPubListActivity extends ListActivity {
	private AnnotationsDbAdapter dbHelper;
	private Cursor cursor;
	
	private HttpHelper asyncTask;
	SemApp semApp;
	EPub ePub;
	ArrayList<EPub> ePubList;
	ArrayList<String> ePubNamesList = new ArrayList<String>();
	ArrayList<String> ePubIDsList = new ArrayList<String>();
	ArrayList<String> ePubFileNamesList = new ArrayList<String>();
	ArrayList<String> ePubFilePathsList = new ArrayList<String>();
	ArrayList<SemAppsAnnotations> ePubAnnotationsList = new ArrayList<SemAppsAnnotations>();
	ArrayList<SemAppsAnnotation> semAppsAnnotations = new ArrayList<SemAppsAnnotation>();
	
	String annotationsXMLString;
	String local_path;
	
	ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		dbHelper = new AnnotationsDbAdapter(this);
		
		Intent intent = getIntent();
		semApp = intent.getParcelableExtra("semapp");
		
		setTitle(getString(R.string.epubslist_title) + " - " + semApp.getName());

		ePubList = semApp.getEPubs().getEPubs();
		for (EPub ePub : ePubList) {
			ePubNamesList.add(ePub.getName());
			ePubIDsList.add(ePub.getId());
			ePubFileNamesList.add(ePub.getFile().getName());
			ePubFilePathsList.add(ePub.getFile().getPath());
			ePubAnnotationsList.add(ePub.getAnnotations());
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, ePubNamesList);
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
		
		semAppsAnnotations = ePubAnnotationsList.get(position).getAnnotations();
		
	    // save book information in database
	    ePub = ePubList.get(position);
	    if (!fbreader.EPubs.getEPubs().contains(ePub)) {
	    	fbreader.EPubs.getEPubs().add(ePub);
	    }
	    local_path = Paths.BooksDirectoryOption().getValue()+"/"+ePubIDsList.get(position)+"/"+ePubFileNamesList.get(position);
	    SQLiteUtil.writeEPubToDatabase(EPubListActivity.this, ePub, local_path, semApp.getId());
	    
		progressDialog = new ProgressDialog(EPubListActivity.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		String progressText = String.format((String) this.getText(R.string.downloadingFile), ePubNamesList.get(position));
		progressDialog.setMessage(progressText);
	    
        
        asyncTask.execute("http://epubdummy.provideal.net" + ePubFilePathsList.get(position)
	    		, ePubFileNamesList.get(position), ePubIDsList.get(position));
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

		EPubListActivity mActivity;

		private ConnectionManager conn;
		private HttpEntity resEntityGet;
		private Object[] connectionResult;
		private int myStatusCode;
		private String filePath;
		
		HttpHelper(EPubListActivity activity) {
            mActivity = activity;
        }
		
		@Override
		protected void onPreExecute() {
            progressDialog.show();
        }
		
		@Override
		protected String doInBackground(String ... params) {
			String path = null;
			
			try {
				String getURL = params[0];
				String fileName = params[1];
				String ePubID = params[2];
				
				conn = ConnectionManager.getInstance();
				connectionResult = conn.postStuffGet(getURL);
				resEntityGet = (HttpEntity) connectionResult[0];
				myStatusCode = ((Integer) connectionResult[1]).intValue();
				if (myStatusCode == conn.AUTHENTICATION_FAILED ||
				myStatusCode == conn.NO_INTERNET_CONNECTION) {
					return null;
				}
				
				InputStream inputStream = resEntityGet.getContent();
				
				File bookDirectory = new File(Paths.BooksDirectoryOption().getValue()+"/"+ePubID);
				bookDirectory.mkdirs();
				File file = new File(bookDirectory, fileName);
				filePath = file.getPath();
				if (file.length() == resEntityGet.getContentLength()) {
					if (resEntityGet != null) {
						try {
							resEntityGet.consumeContent();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return null;
				}
				
				FileOutputStream fileOutput = new FileOutputStream(file);

				int totalSize = (int) resEntityGet.getContentLength();
				int downloadedSize = 0;

				byte[] buffer = new byte[1024];
				int bufferLength = 0; //used to store a temporary size of the buffer

				while ( (bufferLength = inputStream.read(buffer)) != -1 ) {
					//add the data in the buffer to the file in the file output stream (the file on the sd card
					fileOutput.write(buffer, 0, bufferLength);
					//add up the size so we know how much is downloaded
					downloadedSize += bufferLength;
					
					publishProgress((int) (((double) downloadedSize / totalSize)*100));
				}
				path = file.getPath();
				
				fileOutput.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return path;
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
				UIUtil.createDialog(EPubListActivity.this, "Error", getString(R.string.authentication_failed));
				asyncTask = new HttpHelper(EPubListActivity.this);
				return;
			}
			if (myStatusCode == conn.NO_INTERNET_CONNECTION) {
				UIUtil.createDialog(EPubListActivity.this, "Error", getString(R.string.no_internet_connection));
				asyncTask = new HttpHelper(EPubListActivity.this);
				return;
			}
//			if (result == null) {
//				UIUtil.createDialog(EPubListActivity.this, "Error", getString(R.string.complete_file_exists));
//			}
			setResult(4);
			finish();
			
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			// add the missing annotations to local structure
			Annotation annotation;
			String upb_id = "";
			String updated_at = "";
			for (SemAppsAnnotation a : semAppsAnnotations) {
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
					SQLiteUtil.writeAnnotationToDatabase(EPubListActivity.this, annotation, ePub.getId());
				}
			}
			
			if (result == null) {
				fbreader.openFile(ZLFile.createFileByPath(filePath));
			} else {
				fbreader.openFile(ZLFile.createFileByPath(result));
			}
			fbreader.loadAnnotationHighlighting();
		}
	}
}