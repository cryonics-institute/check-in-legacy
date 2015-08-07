package com.cryonicsinstitute;

import io.fabric.sdk.android.Fabric;
import util.AlarmSounds;
import android.app.Application;
import android.os.Handler;

import com.cryonicsinstitute.prefs.Prefs;
import com.crashlytics.android.Crashlytics;

public class CryonicsCheckinApp extends Application {
	public static CryonicsCheckinApp app;
    public static final int MAX_EMERGENCY_SMS_TO_SEND = 5;

	private static Handler handler = new Handler();
	private static boolean countdownInProgress;
	
	@Override
	public void onCreate() {
		super.onCreate();

		if(!BuildConfig.DEBUG) {
			Fabric.with(this, new Crashlytics());
		}

		app = this;
		Prefs.init(this);
		AlarmSounds.init(this);

        // set up alarm if it isn't already running
        AlarmActivity.setNextAlarm(this);
	}

    final private static Runnable mCountDownRunnable = new Runnable() {
		public void run() {
            // limit help messages to total
			if(Prefs.getSentSMSCount() < MAX_EMERGENCY_SMS_TO_SEND) {

                // dismiss AlarmActivity
                AlarmActivity.dismiss(app);

                // send a/another emergency SMS
                SMSSender.sendEmergencySMS(app);

                // continue sending messages every "respond time"
                handler.postDelayed(mCountDownRunnable, Prefs.getRespondTime());
			} else {
                cancelCountdownTimer();
            }
		}
	};

    /**
     * Start the countdown to sending friends/family a help message
     * Defaults to 20 mins
     */
	public static void startCountdownTimer() {
		if(!countdownInProgress) {
			handler.postDelayed(mCountDownRunnable, Prefs.getRespondTime());
			countdownInProgress = true;
		}
	}

    /**
     * Cancel the countdown to sending friends/family a help message
     */
	public static void cancelCountdownTimer() {
        Prefs.setSentSMSCount(0);
		handler.removeCallbacks(mCountDownRunnable);
		countdownInProgress = false;
	}
}
