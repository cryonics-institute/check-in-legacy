package com.cryonicsinstitute;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.cryonicsinstitute.prefs.Prefs;

public class AlarmActivity extends Activity {
    private static final String TAG = "AlarmActivity";
    private static final String CANCEL_ALERT_EXTRA = "cancelAlert";

    private Vibrator mVibrator;
    private Handler mHandler;

    private static final int ALARM_SOUND_AND_VIBRATE_REPEAT_LIMIT = 20;
    private int mAlarmSoundAndVibrateRepeatCount = 0;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // check for cancellation (see AreYouOKApp.java)
        final Intent intent = getIntent();
        if(intent.hasExtra(CANCEL_ALERT_EXTRA)) {
            finish();
            return;
        }
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		
		setContentView(R.layout.alarm_activity);
		
		findViewById(R.id.yesButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CryonicsCheckinApp.cancelCountdownToSMSTimer();
                stopAlertSoundAndVibration();
				finish();
			}
		});

		findViewById(R.id.noButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CryonicsCheckinApp.cancelCountdownToSMSTimer();
                stopAlertSoundAndVibration();

				new AlertDialog.Builder(AlarmActivity.this)
				.setMessage("Do you need help?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
                        CryonicsCheckinApp.cancelCountdownToSMSTimer();
						sendEmergencySMS();

						final Toast toast = Toast.makeText(AlarmActivity.this, "Message sent to friends and family", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();

						dialog.dismiss();
						finish();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
                        CryonicsCheckinApp.cancelCountdownToSMSTimer();
						dialog.dismiss();
						finish();
					}
				}).show();
			}
		});

        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        mAlarmSoundAndVibrateRepeatCount = 0;

        // post the command to start the alert sound, as onPause() is called on 2nd run which
        // cancels the mAlarmSoundAndVibrateRunnable - this circumvents that
        mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mHandler.removeCallbacks(mAlarmSoundAndVibrateRunnable);
                mHandler.postDelayed(mAlarmSoundAndVibrateRunnable, 1000);
            }
        });


		// give user a countdown before sending out messages to friends
        // i.e. wait 20 mins for user to reach phone
        CryonicsCheckinApp.cancelCountdownToSMSTimer();
		CryonicsCheckinApp.startCountdownToSMSTimer();

        // Setup the next alarm
		new SetAlarmTask().execute();
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra(CANCEL_ALERT_EXTRA)) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAlertSoundAndVibration();
    }

    /**
     * Runs every X seconds to resound the alarm and vibrate the phone
     */
    private Runnable mAlarmSoundAndVibrateRunnable = new Runnable() {
        @Override
        public void run() {
            playAlertSound();
            startVibrator();
            if(++mAlarmSoundAndVibrateRepeatCount < ALARM_SOUND_AND_VIBRATE_REPEAT_LIMIT) {
                mHandler.postDelayed(mAlarmSoundAndVibrateRunnable, 5000);
            }
        }
    };

    private class SetAlarmTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// set/schedule next alarm
			setNextAlarm(AlarmActivity.this);
			return null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i(TAG, "Back pressed"); // doesn't fire due to not being a Launcher replacement
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onUserLeaveHint() {
		Log.i(TAG, "User left"); // user hit Home, treat it the same as I'm OK
		super.onUserLeaveHint();
        CryonicsCheckinApp.cancelCountdownToSMSTimer();
		finish();
	}
	
	private void sendEmergencySMS() {
		SMSSender.sendEmergencySMS(this);
	}
	
	private void playAlertSound() {
		AlarmSounds.play(R.raw.alarm);
	}

    private void stopAlertSoundAndVibration() {
        stopVibrator();
        stopAlertSound();
        mAlarmSoundAndVibrateRepeatCount = 0;
        if(mHandler != null) {
            mHandler.removeCallbacks(mAlarmSoundAndVibrateRunnable);
        }
    }
	
	private void stopAlertSound() {
		AlarmSounds.stop();
	}
	
	private void startVibrator() {
		try {
			mVibrator.vibrate(new long[]{250l, 1000l, 250l, 1000l, 250l, 1000l}, -1);
		} catch (Exception e) {
			Log.w(TAG, "Vibrator failed.");
		}
		
	}
	
	private void stopVibrator() {
		try {
			mVibrator.cancel();
		} catch (Exception e) {
			Log.w(TAG, "Vibrator failed (stopping).");
		}
	}
	
	/**
	 * To help understand how the alarm is set, keep in mind some example on and off times...
	 * e.g. alarm on at 6am, off at 10pm, running every 4 hours (10am, 2pm, 6pm, not inc 10pm)
	 * @param context Context
	 */
	public static void setNextAlarm(Context context) {
		final Context app = context.getApplicationContext();
		final AlarmManager am = (AlarmManager)app.getSystemService(ALARM_SERVICE);
		final Intent alarmIntent = new Intent(app, AlarmReceiver.class);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(app, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		// cancel existing alarm
        am.cancel(pendingIntent);
        
        // check alarm is on
        if(!Prefs.getAlarmEnabled()) {
        	Log.i(TAG, "Alarm disabled");
        	return;
        }
        
        Log.i(TAG, "Scheduling alarm...");
		Log.i(TAG, "On at " + Prefs.getAlarmOnAt());
		Log.i(TAG, "Off at " + Prefs.getAlarmOffAt());
        
        // Note: Joda DateTime takes into account DST changes when adding time
        final DateTime now = DateTime.now();
        Log.i(TAG, "Now " + now.toString());
        
        final int alarmFrequencyHours = Prefs.getAlarmFrequencyHours();

        // time the alarm goes on and off TODAY
        DateTime onAt = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), Prefs.getAlarmOnAt(), 0);
        DateTime offAt = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), Prefs.getAlarmOffAt(), 0);
        if(offAt.getHourOfDay()==0) {
        	// if off time is midnight tonight, make it midnight tomorrow so that it is in the future for comparisons
        	offAt = offAt.plusDays(1);
        }
        
        final DateTime nextAlarm;
        
        int compareNowToOffTime = now.compareTo(offAt);
        if(compareNowToOffTime == -1) {
        	// we're before "off alarm" time (e.g. 10 pm)

        	// check if we're before the "on time"
        	int compareNowToOnTime = now.compareTo(onAt);
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
        	
        } else {
        	// we're at or after the off time (e.g. 10pm), set alarm for the morning
    		nextAlarm = onAt.plusDays(1).plusHours(alarmFrequencyHours);
        } 

		long nextAlarmTime = nextAlarm.getMillis();

		Log.i(TAG, "Next Alarm " + nextAlarm.toString());
//this next line can be used to test the alarm more frequently for dev purposes (3min)
//nextAlarmTime = now.getMillis() + 2000*60;

		// cancel previous
		am.cancel(pendingIntent);

		// set new alarm
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
			am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent);
		} else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
			am.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent);
		} else {
			am.set(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent);
		}

		Prefs.setAlarmEnabled(true);
		Prefs.setNextAlarmTime(nextAlarm.getMillis());
	}
	
	public static void disableAlarm(Context context) {
		AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);

		final Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		// cancel alarm
        am.cancel(pendingIntent);
        
        Prefs.setAlarmEnabled(false);
	}

    public static void dismiss(Context context) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        alarmIntent.putExtra(CANCEL_ALERT_EXTRA, true);
        context.startActivity(alarmIntent);
    }
}
