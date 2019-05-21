package com.cryonicsinstitute;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.WindowManager;

/**
 * Dismisses the keyguard in preparation for the AlarmActivity
 * See: http://stackoverflow.com/questions/27948419/flag-dismiss-keyguard-no-longer-working-on-android-lollipop
 */
public class PreAlarmActivity extends Activity {
    private ScreenOnReceiver receiver;
    private boolean alarmActivityStarted;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent alarmIntent = new Intent(this, AlarmActivity.class);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(alarmIntent);
            finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        handler = new Handler();
        receiver = new ScreenOnReceiver();
        registerReceiver(receiver, receiver.getFilter());

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, "wakeup");
        wl.acquire();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runAlarmActivity();
                wl.release();
            }
        }, 1000);
    }

    private void runAlarmActivity() {
        if(alarmActivityStarted) {
           return;
        }
        alarmActivityStarted = true;
        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(alarmIntent);;
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        finish();
    }

    private class ScreenOnReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // screen on, ready to start alarm without it bombing
            runAlarmActivity();
        }

        public IntentFilter getFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            return filter;
        }
    }
}
