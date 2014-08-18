package com.areyouok;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBootReceiver extends BroadcastReceiver {

    private static final String TAG = "OnBootReceiver";

    @Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "OnBootReceiver");
		
		// set up alarm if it isn't already running
		AlarmActivity.setNextAlarm(context);
	}
}
