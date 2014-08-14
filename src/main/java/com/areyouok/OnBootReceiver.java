package com.areyouok;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("AYO", "OnBootReceiver");
		
		// set up alarm if it isn't already running
		AlarmActivity.setAlarm(context);

	}

}
