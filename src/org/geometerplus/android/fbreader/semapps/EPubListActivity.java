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
import org.geometerplus.android.util.UIUtil;
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
	
	String annotationsXMLString;
	String local_path;
	
	ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		dbHelper = new AnnotationsDbAdapter(this);
		
		Intent intent = getIntent();
		semApp = intent.getParcelableExtra("semapp");

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
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		
		String item = (String) getListAdapter().getItem(position);
//		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		
		// Make one string of the annotations of one epub
	    annotationsXMLString = "<annotations>";
	    for (SemAppsAnnotation annotation : ePubAnnotationsList.get(position).getAnnotations()) {
	    	annotationsXMLString += annotation.getData();
	    }
	    annotationsXMLString += "</annotations>";
		
	    // save book information in database
	    ePub = ePubList.get(position);
	    if (!fbreader.EPubs.getEPubs().contains(ePub)) {
	    	fbreader.EPubs.getEPubs().add(ePub);
	    }
	    local_path = Paths.BooksDirectoryOption().getValue()+"/"+ePubIDsList.get(position)+"/"+ePubFileNamesList.get(position);
	    fbreader.writeEPubToDatabase(EPubListActivity.this, ePub, local_path, semApp.getId());
	    
		if (asyncTask != null) asyncTask.cancel(true);
		progressDialog = new ProgressDialog(EPubListActivity.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		String progressText = String.format((String) this.getText(R.string.downloadingFile), ePubNamesList.get(position));
		progressDialog.setMessage(progressText);
		asyncTask = new HttpHelper();
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
	
	private class HttpHelper extends AsyncTask<String, Void, String> {

		private HttpEntity resEntityGet;
		private Object[] connectionResult;
		private String myStatusCode;
		
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
				
				ConnectionManager conn = ConnectionManager.getInstance();
				connectionResult = conn.postStuffGet(getURL);
				resEntityGet = (HttpEntity) connectionResult[0];
				myStatusCode = (String) connectionResult[1];
				if (myStatusCode.equals(conn.AUTHENTICATION_FAILED) ||
						myStatusCode.equals(conn.NO_INTERNET_CONNECTION)) {
					return myStatusCode;
				}
				
				InputStream inputStream = resEntityGet.getContent();
				
				File bookDirectory = new File(Paths.BooksDirectoryOption().getValue()+"/"+ePubID);
				bookDirectory.mkdirs();
				File file = new File(bookDirectory, fileName);
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
					
					updateProgress(downloadedSize, totalSize);
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
		
		public void updateProgress(int currentSize, int totalSize){ 
			double value = ((double) currentSize / totalSize)*100;
			progressDialog.setProgress((int) value);
//			mProgressText.setText(Long.toString()+"%"); 
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (progressDialog.isShowing()) {
                progressDialog.dismiss();
			}
			ConnectionManager conn = ConnectionManager.getInstance();
			if (result.equals(conn.AUTHENTICATION_FAILED)) {
				UIUtil.createDialog(EPubListActivity.this, "Error", getString(R.string.authentication_failed));
				return;
			}
			if (result.equals(conn.NO_INTERNET_CONNECTION)) {
				UIUtil.createDialog(EPubListActivity.this, "Error", getString(R.string.no_internet_connection));
				return;
			}
			
			setResult(4);
			finish();
			
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			// load the annotations into object structure
			Annotations newAnnotations = loadAnnotationsFromXMLString(annotationsXMLString);
			// add the missing annotations to local structure
			for (Annotation annotation : newAnnotations.getAnnotations()) {
				if (!fbreader.Annotations.getAnnotations().contains(annotation)) {
					fbreader.Annotations.addAnnotation(annotation);
					fbreader.writeAnnotationToDatabase(EPubListActivity.this, annotation, ePub.getId());
				}
				// TODO wenn updated_at neuer ist als lokal, dann sollte lokal überschrieben werden. natürlich
				// nur wenn die annotation schon existiert
			}
			
			// load the downloaded book
			fbreader.openFile(ZLFile.createFileByPath(result));
		}
		
		/**
		 * load an XML String of annotations into the annotations object structure
		 * @param xml
		 */
		public Annotations loadAnnotationsFromXMLString(String xml) {
			Annotations annotations = null;
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
				annotations = serializer.read(Annotations.class, xml);
	    	} catch (Exception e) {
	    		Log.e("loadFromXMLString", e.toString());
	    	}
	    	return annotations;
		}
	}
}