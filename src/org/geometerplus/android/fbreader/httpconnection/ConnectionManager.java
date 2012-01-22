package org.geometerplus.android.fbreader.httpconnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.util.Log;

public class ConnectionManager {
    private static ConnectionManager instance = null;
    private HttpParams httpParams;
    private DefaultHttpClient httpClient;
    private HttpGet get;
    private HttpResponse responseGet;
	private HttpEntity resEntityGet;

    private ConnectionManager() {
    	httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
		httpClient = new DefaultHttpClient(httpParams);
		
    }
    //public method that will be invoked from other classes.
    public static ConnectionManager getInstance() {
        if(instance == null) {
        	instance = new ConnectionManager();
        }
       return instance;
    }

    public void authenticate(String user, String password){
    	httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1),
				new UsernamePasswordCredentials(user, password));
	}

    public HttpEntity postStuff(String url){
    	try {
	    	get = new HttpGet(url);
	    	responseGet = httpClient.execute(get);
	    	if (responseGet.getStatusLine().getStatusCode() != 200) {
				if (responseGet.getStatusLine().getStatusCode() == 401) {
					return null;
				} else {
					throw new RuntimeException("Failed : HTTP error code : "
							   + responseGet.getStatusLine().getStatusCode());
				}
			}
	        resEntityGet = responseGet.getEntity();  
    	} catch (Exception e) {
		    e.printStackTrace();
		    Log.e("UPBLibraryLoginActivity", e.toString());
		} 
		return resEntityGet;
	}
}