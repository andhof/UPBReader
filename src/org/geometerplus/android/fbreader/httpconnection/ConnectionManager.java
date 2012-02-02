package org.geometerplus.android.fbreader.httpconnection;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.util.Log;

public class ConnectionManager {
	public final int OK = 0;
	public final int NO_INTERNET_CONNECTION = 1;
	public final int AUTHENTICATION_FAILED = 2;
	public final int NOT_FOUND = 3;
	
	private static ConnectionManager instance = null;
    private HttpParams httpParams;
    private DefaultHttpClient httpClient;
    private HttpGet get;
    private HttpPut put;
    private HttpPost post;
    private HttpDelete delete;
    private HttpResponse response;
	private HttpEntity resEntity;
	
	private String username;
	private String password;

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

    public void authenticate(String username, String password) {
    	httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1),
				new UsernamePasswordCredentials(username, password));
    	this.username = username;
    	this.password = password;
	}

    public Object[] postStuffGet(String url) {
    	response = null;
    	resEntity = null;
    	try {
	    	get = new HttpGet(url);
	    	response = httpClient.execute(get);
	    	if (response.getStatusLine().getStatusCode() != 200) {
				if (response.getStatusLine().getStatusCode() == 401) {
					return new Object[] {null, AUTHENTICATION_FAILED};
				} else if (response.getStatusLine().getStatusCode() == 404) {
					return new Object[] {null, NOT_FOUND};
				} else {
					throw new RuntimeException("Failed : HTTP error code : "
							   + response.getStatusLine().getStatusCode());
				}
			}
	        resEntity = response.getEntity();  
    	} catch (Exception e) {
		    Log.e("ConnectionManager", e.toString());
    		if (response == null) {
    			return new Object[] {null, NO_INTERNET_CONNECTION};
    		}
    		e.printStackTrace();
		} 
		return new Object[] {resEntity, OK};
	}
    
    public Object[] postStuffPut(String url, String input) {
    	response = null;
    	resEntity = null;
    	try {
	    	put = new HttpPut(url);
	    	Log.v("ConnectionManager", url);
	    	List<NameValuePair> params = new ArrayList<NameValuePair>();
	    	params.add(new BasicNameValuePair("data", input));
	    	put.setEntity(new UrlEncodedFormEntity(params));
	    	response = httpClient.execute(put);
	    	if (response.getStatusLine().getStatusCode() != 200) {
				if (response.getStatusLine().getStatusCode() == 401) {
					return new Object[] {null, AUTHENTICATION_FAILED};
				} else if (response.getStatusLine().getStatusCode() == 404) {
					return new Object[] {null, NOT_FOUND};
				} else {
					throw new RuntimeException("Failed : HTTP error code : "
							   + response.getStatusLine().getStatusCode());
				}
			}
	        resEntity = response.getEntity();  
    	} catch (Exception e) {
    		Log.e("ConnectionManager", e.toString());
    		if (response == null) {
    			return new Object[] {null, NO_INTERNET_CONNECTION};
    		}
		    e.printStackTrace();
		} 
    	return new Object[] {resEntity, OK};
	}
    
    public Object[] postStuffPost(String url, String input) {
    	response = null;
    	resEntity = null;
    	try {
	    	post = new HttpPost(url);
	    	Log.v("ConnectionManager", url);
	    	List<NameValuePair> params = new ArrayList<NameValuePair>();
	    	params.add(new BasicNameValuePair("data", input));
	    	post.setEntity(new UrlEncodedFormEntity(params));
	    	response = httpClient.execute(post);
	    	if (response.getStatusLine().getStatusCode() != 200) {
				if (response.getStatusLine().getStatusCode() == 401) {
					return new Object[] {null, AUTHENTICATION_FAILED};
				} else if (response.getStatusLine().getStatusCode() == 404) {
					return new Object[] {null, NOT_FOUND};
				} else {
					throw new RuntimeException("Failed : HTTP error code : "
							   + response.getStatusLine().getStatusCode());
				}
			}
	        resEntity = response.getEntity();  
    	} catch (Exception e) {
    		Log.e("ConnectionManager", e.toString());
    		if (response == null) {
    			return new Object[] {null, NO_INTERNET_CONNECTION};
    		}
		    e.printStackTrace();
		} 
    	return new Object[] {resEntity, OK};
	}
    
    public Object[] postStuffDelete(String url) {
    	response = null;
    	resEntity = null;
    	try {
	    	delete = new HttpDelete(url);
	    	Log.v("ConnectionManager", url);
	    	response = httpClient.execute(delete);
	    	Log.v("ConnectionManager", "delete aufruf. direkt nach execute");
	    	if (response.getStatusLine().getStatusCode() != 200) {
				if (response.getStatusLine().getStatusCode() == 401) {
					Log.v("ConnectionManager", "statuscode: "+response.getStatusLine().getStatusCode());
					return new Object[] {null, AUTHENTICATION_FAILED};
				} else if (response.getStatusLine().getStatusCode() == 404) {
					Log.v("ConnectionManager", "statuscode: "+response.getStatusLine().getStatusCode());
					return new Object[] {null, NOT_FOUND};
				} else {
					throw new RuntimeException("Failed : HTTP error code : "
							   + response.getStatusLine().getStatusCode());
				}
			}
	        resEntity = response.getEntity();  
    	} catch (Exception e) {
		    Log.e("ConnectionManager", e.toString());
    		if (response == null) {
    			return new Object[] {null, NO_INTERNET_CONNECTION};
    		}
    		Log.v("ConnectionManager", "statuscode: "+response.getStatusLine().getStatusCode());
		    e.printStackTrace();
		} 
    	return new Object[] {resEntity, OK};
	}
    
    public String getLoginUsername() {
    	return username;
    }
    
    public String getLoginPassword() {
    	return password;
    }
}