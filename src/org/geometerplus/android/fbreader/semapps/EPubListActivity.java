package org.geometerplus.android.fbreader.semapps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.semapps.model.EPub;
import org.geometerplus.android.fbreader.semapps.model.SemApp;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
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
import android.widget.ListView;

public class EPubListActivity extends ListActivity {
	
	private HttpHelper asyncTask;
	ArrayList<String> ePubNamesList = new ArrayList<String>();
	ArrayList<String> ePubIDsList = new ArrayList<String>();
	ArrayList<String> ePubFileNamesList = new ArrayList<String>();
	ArrayList<String> ePubFilePathsList = new ArrayList<String>();
	
	ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Intent intent = getIntent();
		SemApp semApp = intent.getParcelableExtra("semapp");

		ArrayList<EPub> ePubList = semApp.getEPubs().getEPubs();
		for (EPub ePub : ePubList) {
			ePubNamesList.add(ePub.getName());
			ePubIDsList.add(ePub.getId());
			ePubFileNamesList.add(ePub.getFile().getName());
			ePubFilePathsList.add(ePub.getFile().getPath());
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.simple_list_item_1, ePubNamesList);
		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
//		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		
		// TODO Connect to Server by async task to download epub file
		
		if (asyncTask != null) asyncTask.cancel(true);
		progressDialog = new ProgressDialog(EPubListActivity.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Downloading Book...");
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

		@Override
		protected void onPreExecute() {
            progressDialog.show();
        }
		
		@Override
		protected String doInBackground(String ... params) {
			String path = null;
			
			try {
				String filePath = params[0];
				String fileName = params[1];
				String ePubID = params[2];
				
				URL url = new URL(filePath);

				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				urlConnection.connect();

				File bookDirectory = new File(Paths.BooksDirectoryOption().getValue()+"/"+ePubID);
				bookDirectory.mkdirs();
				File file = new File(bookDirectory, fileName);

				FileOutputStream fileOutput = new FileOutputStream(file);

				InputStream inputStream = urlConnection.getInputStream();

				int totalSize = urlConnection.getContentLength();
				int downloadedSize = 0;

				byte[] buffer = new byte[1024];
				int bufferLength = 0; //used to store a temporary size of the buffer

				while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
					//add the data in the buffer to the file in the file output stream (the file on the sd card
					fileOutput.write(buffer, 0, bufferLength);
					//add up the size so we know how much is downloaded
					downloadedSize += bufferLength;
					
					//this is where you would do something to report the prgress, like this maybe
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
		protected void onPostExecute(String path) {
			if (progressDialog.isShowing()) {
                progressDialog.dismiss();
			}
			
			setResult(4);
			finish();
			
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			
			fbreader.openFile(ZLFile.createFileByPath(path));
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