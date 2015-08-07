package com.cryonicsinstitute;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.cryonicsinstitute.prefs.Prefs;

public class AlarmSettingsActivity extends BaseActivity {
	
	private static int CHANGE_FREQUENCY_STATE	= 0;
	private static int SLEEP_HOURS_STATE		= 1;
	private int state = CHANGE_FREQUENCY_STATE;
	
	private TextView mHoursTextView;
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		populateUI();
	}
	
	private void populateUI() {
		if(state == CHANGE_FREQUENCY_STATE) {
			populateAlarmFrequencyUI();
		} else if(state == SLEEP_HOURS_STATE) {
			populateSleepHoursUI();
		}
	}
	
	private void populateAlarmFrequencyUI() {
		setContentView(R.layout.alarm_settings_activity_frequency);
		
		final Button minusButton = (Button)findViewById(R.id.minusButton);
		final Button plusButton = (Button)findViewById(R.id.plusButton);
		final Button nextButton = (Button)findViewById(R.id.nextButton);
		mHoursTextView = (TextView)findViewById(R.id.hoursTextView);
		
		mHoursTextView.setText(String.valueOf(Prefs.getAlarmFrequencyHours()));
		
		minusButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int freq = Prefs.getAlarmFrequencyHours();
				if(freq-1 > 0) {
					setAlarmFrequency(freq-1);
				}
			}
		});
		plusButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int freq = Prefs.getAlarmFrequencyHours();
				if(freq+1 < getResources().getInteger(R.integer.max_alarm_frequency)) {
					setAlarmFrequency(freq+1);
				}
			}
		});
		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				state = SLEEP_HOURS_STATE;
				populateSleepHoursUI();
			}
		});
		
	}
	
	private void populateSleepHoursUI() {
		setContentView(R.layout.alarm_settings_activity_sleep);
		
		final Spinner fromSpinner = (Spinner)findViewById(R.id.fromSpinner);
		final Spinner toSpinner = (Spinner)findViewById(R.id.toSpinner);
		
		fromSpinner.setAdapter(new HoursAdapter(this, new Integer[]{13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0}, HoursAdapter.EVENING));
		toSpinner.setAdapter(new HoursAdapter(this, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, HoursAdapter.MORNING));
		
		int offAt = Prefs.getAlarmOffAt();
		fromSpinner.setSelection( (offAt==0) ? 11 : offAt-13); // select last entry for 0, else 24hr - 12 - 1 (the -1 is cos array 0-based)
		toSpinner.setSelection(Prefs.getAlarmOnAt()-1);
		
		final Button nextButton = (Button)findViewById(R.id.nextButton);
		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int onHour = (Integer)toSpinner.getSelectedItem();
				
				int offHour = (Integer)fromSpinner.getSelectedItem();
				setAlarmOnOffHours(onHour, offHour);
				mProgressDialog = ProgressDialog.show(AlarmSettingsActivity.this, null, "Setting alarm...", true);
				new Handler().post(new Runnable() {
					public void run() {
						new SetAlarmTask().execute();		
					}
				});
				nextButton.setEnabled(false);
			}
		});
	}
	
	private class SetAlarmTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// set/schedule next alarm
            Prefs.setAlarmEnabled(true);
			AlarmActivity.setNextAlarm(AlarmSettingsActivity.this);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setResult(RESULT_OK);
			mProgressDialog.dismiss();
//			Toast t = Toast.makeText(AlarmSettingsActivity.this, "Alarm Set", Toast.LENGTH_LONG);
//            t.setGravity(CENTER, 0, 0);
//            t.show();
			finish();
		}
	}
	
	private void setAlarmFrequency(int freq) {
		Prefs.setAlarmFrequencyHours(freq);
		mHoursTextView.setText(String.valueOf(freq));
	}
	
	private void setAlarmOnOffHours(int onHour, int offHour) {
		Prefs.setAlarmOnAt(onHour);
		Prefs.setAlarmOffAt(offHour);
		Prefs.setIsFirstRun(false);
	}
	
	private static class HoursAdapter extends BaseAdapter {
		public static int MORNING = 0;
		public static int EVENING = 1;
		
		private Integer[] mTimeValues;
		private Context mContext;
		private int mTimePeriod;
		
		public HoursAdapter(Context context, Integer[] timeValues, int timePeriod) {
			mContext = context;
			mTimeValues = timeValues;
			mTimePeriod = timePeriod;
		}
		
		public int getCount() {
			return mTimeValues.length;
		}
		
		public Object getItem(int position) {
			return mTimeValues[position];
		}

		public long getItemId(int position) {
			return mTimeValues[position];
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view;
			if(convertView != null) {
				view = (TextView)convertView;
			} else {
				view = (TextView)LayoutInflater.from(mContext)
						.inflate(R.layout.sleep_spinner_list_row, parent, false);
			}
			int time = (Integer)getItem(position);
			String amPM;
			if(mTimePeriod==MORNING) {
				if(time==12) amPM = " midday";
				else amPM = " am";
			} else {
				// format for reading
				if(time==0) {
					time = 12;
					amPM = " midnight";
				} else {
					time = time - 12;
					amPM = " pm";
				}
			}
			view.setText(String.valueOf(time) + amPM);
			return view;
		}
	}
}
