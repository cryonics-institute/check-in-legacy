package com.areyouok;

import org.joda.time.DateTime;

import util.AlarmSounds;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.areyouok.prefs.Prefs;

public class AlarmActivity extends Activity {
	
	private Vibrator mVibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		
		setContentView(R.layout.alarm_activity);
		
		findViewById(R.id.yesButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AreYouOKApp.cancelCountdownTimer();
				Prefs.setSentSMSCount(0);
				stopAlertSound();
				stopVibrator();
				finish();
			}
		});

		findViewById(R.id.noButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AreYouOKApp.cancelCountdownTimer();
				
				stopAlertSound();
				stopVibrator();
				
				new AlertDialog.Builder(AlarmActivity.this)
				.setMessage("Do you need help?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						sendSMS();
						Toast.makeText(AlarmActivity.this, "Message sent to friends and family", Toast.LENGTH_LONG).show();
						dialog.dismiss();
						finish();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
                        Prefs.setSentSMSCount(0);
						dialog.dismiss();
						finish();
					}
				}).show();
			}
		});
		
		mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		startVibrator();
		
		playAlertSound();
		
		// give user a countdown before sending out messages to friends
		AreYouOKApp.startCountdownTimer();
		
		new SetAlarmTask().execute();
	}
	
	private class SetAlarmTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// set/schedule next alarm
			setAlarm(AlarmActivity.this);
			return null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i("AYO", "Back pressed"); // doesn't fire due to not being a Launcher replacement
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onUserLeaveHint() {
		Log.i("AYO", "User left"); // user hit Home, same as OK
		super.onUserLeaveHint();
		finish(); // TODO: Appears to also fire if they reject an incoming call during the dialog 
	}
	
	private void sendSMS() {
		SMSSender.sendEmergencySMS(this);
	}
	
	private void playAlertSound() {
		AlarmSounds.play(R.raw.alarm, 10);
	}
	
	private void stopAlertSound() {
		AlarmSounds.stop();
	}
	
	private void startVibrator() {
		try {
			mVibrator.vibrate(new long[]{250l,1000l,250l,1000l,250l,1000l,250l,1000l,250l,1000l,250l,1000l,250l,1000l}, -1);
		} catch (Exception e) {
			Log.w("AYO", "Vibrator failed.");
		}
		
	}
	
	private void stopVibrator() {
		try {
			mVibrator.cancel();
		} catch (Exception e) {
			Log.w("AYO", "Vibrator failed (stopping).");
		}
	}
	
	/**
	 * To help understand how the alarm is set, keep in mind some example on and off times...
	 * e.g. alarm on at 6am, off at 10pm, running every 4 hours (10am, 2pm, 6pm, not inc 10pm)
	 * @param context
	 */
	public static void setAlarm(Context context) {
		Context app = context.getApplicationContext();
		
		AlarmManager am = (AlarmManager)app.getSystemService(ALARM_SERVICE);

		final Intent alarmIntent = new Intent(app, AlarmReceiver.class);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(app, 0, alarmIntent, 0);
		
		// cancel existing alarm
        am.cancel(pendingIntent);
        
        // check alarm is on
//        if(Prefs.getAlarmEnabled() == false) {
//        	Log.i("AYO", "Alarm disabled");
//        	return;
//        }
        
        Log.i("AYO", "Scheduling alarm...");
		Log.i("AYO", "On at " + Prefs.getAlarmOnAt());
		Log.i("AYO", "Off at " + Prefs.getAlarmOffAt());
        
        // Joda Time takes into account DST changes when adding time
        DateTime now = DateTime.now();
        Log.i("AYO", "Now " + now.toString());
        
        final int alarmFrequencyHours = Prefs.getAlarmFrequencyHours();

        // time the alarm goes on and off TODAY
        DateTime onAt = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), Prefs.getAlarmOnAt(), 0);
        DateTime offAt = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), Prefs.getAlarmOffAt(), 0);
        if(offAt.getHourOfDay()==0) {
        	// if off time is midnight tonight, make it midnight tomorrow so that it is in the future for comparisons
        	offAt = offAt.plusDays(1);
        }
        
        DateTime nextAlarm = null;
        
        int compareNowToOffTime = now.compareTo(offAt);
        int compareNowToOnTime = now.compareTo(onAt);
        if(compareNowToOffTime == -1) {
        	// we're before "off alarm" time (e.g. 10 pm)
        	
        	// check if we're before the "on time"
        	if(compareNowToOnTime == -1) {
        		// we're before the first alarm has gone off in the day, so just set it to that
        		nextAlarm = onAt.plusHours(alarmFrequencyHours);
        	} else {
        		// we're after the first alarm, but before the off time
        		// find the next alarm time that would occur before the alarm off time...
        		int nowHour = now.getHourOfDay();
        		int onAtHour = onAt.getHourOfDay();
        		int offAtHour = offAt.getHourOfDay();
        		if(offAtHour==0) offAtHour = 24; // let midnight be 24 not 0 for comparisons below
        		int nextAlarmHour = onAtHour;
        		while(nextAlarmHour <= nowHour) {
    				nextAlarmHour += alarmFrequencyHours;
        		}
        		if(nextAlarmHour < offAtHour) {
        			nextAlarm = onAt.plusHours(nextAlarmHour - onAtHour);
        		} else {
        			nextAlarm = onAt.plusDays(1).plusHours(alarmFrequencyHours);
        		}
        	}
        	
        } else if(compareNowToOffTime == 1 || compareNowToOffTime == 0){
        	// we're at or after the off time (e.g. 10pm), set alarm for the morning
    		nextAlarm = onAt.plusDays(1).plusHours(alarmFrequencyHours);
        } 
        
        if(nextAlarm != null) {
        	Log.i("AYO", "Date time " + nextAlarm.getMillis());
        	Log.i("AYO", "Next Alarm " + nextAlarm.toString());
//am.set(AlarmManager.RTC_WAKEUP, now.getMillis() + 1000*180, pendingIntent);
        	am.set(AlarmManager.RTC_WAKEUP, nextAlarm.getMillis(), pendingIntent);
        	Prefs.setAlarmEnabled(true);
        } else {
        	Toast.makeText(app, "Unable to set alarm, unknown problem occurred", Toast.LENGTH_LONG);
        	Log.e("AYO", "Couldn't find nextAlarm time");
        }
	}
	
	public static void disableAlarm(Context context) {
		AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);

		final Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
		
		// cancel alarm
        am.cancel(pendingIntent);
        
        Prefs.setAlarmEnabled(false);
	}
}
