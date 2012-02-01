package org.geometerplus.android.fbreader.receiver;

import org.geometerplus.android.fbreader.services.AnnotationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(ConnectivityReceiver.class.getSimpleName(), "action: "
                + intent.getAction());
		Intent service = new Intent(context, AnnotationService.class);
		context.startService(service);
	}
}