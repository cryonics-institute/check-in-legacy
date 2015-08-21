package com.cryonicsinstitute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	Handler handler = new Handler();

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.i("AYO", "AlarmReceiver invoked.");
		AlarmActivity.dismiss(context);

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent alarmIntent = new Intent(context, AlarmActivity.class);
				alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				alarmIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(alarmIntent);
			}
		}, 2000);
	}
}
