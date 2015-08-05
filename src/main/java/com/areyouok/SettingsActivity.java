package com.areyouok;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.areyouok.prefs.Prefs;

public class SettingsActivity extends ActionBarActivity {

    private static final int REQUEST_SET_ALARM = 0;
    private static final int REQUEST_CANCEL_ALARM = 1;

	Button mChangeAlarmButton;
	Button mCancelAlarmButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.settings_activity);
		
		mChangeAlarmButton = (Button)findViewById(R.id.change_alarm_button);
		mChangeAlarmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, AlarmSettingsActivity.class);
				startActivityForResult(intent, REQUEST_SET_ALARM);
			}
		});
		
		findViewById(R.id.change_friends_family_button).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, ChooseContactsActivity.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.back_button).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		mCancelAlarmButton = (Button)findViewById(R.id.cancel_alarm_button);
		mCancelAlarmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(SettingsActivity.this)
			    .setMessage("Are you sure you want to cancel the alarm completely?")
			    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	AlarmActivity.disableAlarm(SettingsActivity.this);
			        	Toast.makeText(SettingsActivity.this, "Alarm cancelled", Toast.LENGTH_LONG).show();
			        	dialog.dismiss();
			        	updateButtons();
                        finish();
			        }
			    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	dialog.dismiss();
			        	updateButtons();
			        }
			    }).show();
			}
		});
		findViewById(R.id.about_button).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(SettingsActivity.this)
						.setTitle("About")
						.setMessage("AreYouOK was developed by Richard Leggett, Valis Interactive Ltd.")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						}).setNegativeButton("Visit site", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.valisinteractive.com"));
						startActivity(viewIntent);
						dialog.dismiss();
					}
				}).show();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		updateButtons();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_SET_ALARM) {
            // back out to main menu after setting alarm
            finish();
        }
    }

    private void updateButtons() {
		boolean isAlarmOn = Prefs.getAlarmEnabled();
		
		mChangeAlarmButton.setText(isAlarmOn ? R.string.change_alarm : R.string.set_alarm);
		mCancelAlarmButton.setVisibility(isAlarmOn ? View.VISIBLE : View.GONE);
	}
}
