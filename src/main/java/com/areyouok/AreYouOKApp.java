package com.areyouok;

import util.AlarmSounds;
import android.app.Application;
import android.os.Handler;

import com.areyouok.prefs.Prefs;

public class AreYouOKApp extends Application {
	public static AreYouOKApp app;
	static Handler mHandler = new Handler();
	static boolean countdownInProgress;
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		Prefs.init(this);
		AlarmSounds.init(this);
	}
	
	final private static Runnable mCountDownRunnable = new Runnable() {
		public void run() {
			if(Prefs.getSentSMSCount()<5) {
				SMSSender.sendEmergencySMS(app);
				cancelCountdownTimer();
			}
		}
	};
	
	public static void startCountdownTimer() {
		if(!countdownInProgress) {
			mHandler.postDelayed(mCountDownRunnable, Prefs.getRespondTime());
			countdownInProgress = true;
		}
	}
	
	public static void cancelCountdownTimer() {
		mHandler.removeCallbacks(mCountDownRunnable);
		countdownInProgress = false;
	}
}
