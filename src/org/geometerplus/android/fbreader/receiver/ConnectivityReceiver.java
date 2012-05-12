package org.geometerplus.android.fbreader.receiver;

import org.geometerplus.android.fbreader.services.AnnotationService;
import org.geometerplus.android.util.NetworkUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(ConnectivityReceiver.class.getSimpleName(), "action: "
                + intent.getAction());
		if (NetworkUtil.isOnline(context)) {
			Intent service = new Intent(context, AnnotationService.class);
			context.startService(service);
		} else {
			Log.v("ConnectivityReceiver", "No connection to internet.");
		}
	}
}