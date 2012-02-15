package org.geometerplus.android.util;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class StorageUtil {
	
	public static final String COUNT = "UPBreader_Storage";
	
	public static int getCurrentCounterAndIncrement(Context context, String key) {
		SharedPreferences settings = context.getSharedPreferences(COUNT, 0);
		int counter = settings.getInt(key, 0);
		SharedPreferences.Editor e = settings.edit();
		counter++;
		e.putInt(key, counter);
		e.commit();
		
		return counter;
	}
	
}