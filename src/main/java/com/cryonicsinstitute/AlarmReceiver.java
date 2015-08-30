package com.cryonicsinstitute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	Handler handler = new Handler();

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.i("AYO", "AlarmReceiver invoked.");
//		AlarmActivity.dismiss(context);

		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
						| PowerManager.ON_AFTER_RELEASE, "wakeup");
		wl.acquire();

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
                wl.release();
			}
		}, 2000);

        Intent alarmIntent = new Intent(context, PreAlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(alarmIntent);


	}
}
