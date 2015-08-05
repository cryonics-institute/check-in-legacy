package com.areyouok;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.areyouok.prefs.Prefs;

/**
 * WizardActivity is displayed on first-run
 */
public class WizardActivity extends ActionBarActivity {
	
	public static String STATE_EXTRA		=	"state";
	
	public static int WELCOME_STATE			=	0;
	public static int CHOOSE_CONTACTS_STATE	=	1;
	public static int SET_ALARM_STATE		=	2;
	public static int WIZARD_DONE_STATE		=	3;
	private int state =	WELCOME_STATE;
	
	static int CHOOSE_CONTACTS_REQUEST	=	0;
	static int SET_ALARM_REQUEST		=	1;

    private String mTitle;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getTitle().toString();

        if(getIntent().hasExtra(STATE_EXTRA)) {
        	state = getIntent().getExtras().getInt(STATE_EXTRA);
        } 
        populateUI();
    }

    private void populateUI() {
    	if(state == WELCOME_STATE) {
    		setContentView(R.layout.wizard_activity_1);
    		
    		findViewById(R.id.startButton).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					state = CHOOSE_CONTACTS_STATE;
					populateUI();
				}
			});
            setTitle(mTitle);
    		
    	} else if(state == CHOOSE_CONTACTS_STATE) {
    		setContentView(R.layout.wizard_activity_2);

    		findViewById(R.id.startButton).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(WizardActivity.this, ChooseContactsActivity.class);
					startActivityForResult(intent, CHOOSE_CONTACTS_REQUEST);
				}
			});
            setTitle(mTitle);

    	} else if(state == SET_ALARM_STATE) {
    		setContentView(R.layout.wizard_activity_3);
    		
    		((Button)findViewById(R.id.continueButton)).setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				// Alarm is actually registered here
    				Intent intent = new Intent(WizardActivity.this, AlarmSettingsActivity.class);
    				startActivityForResult(intent, SET_ALARM_REQUEST);
    			}
    		});
            setTitle(mTitle);

	    } else if(state == WIZARD_DONE_STATE) {
	    	setContentView(R.layout.wizard_activity_4);
	    	
	    	int onTime = Prefs.getAlarmOnAt();
	    	int offTime = Prefs.getAlarmOffAt();
	    	
	    	// format for easy reading
	    	String onAMPM;
			if(onTime==12) onAMPM = " midday";
			else onAMPM = " am";
				
			String offAMPM;
			if(offTime==0) {
				offTime = 12;
				offAMPM = " midnight";
			} else {
				offTime = offTime - 12;
				offAMPM = " pm";
			}

            int alarmFreqHours = Prefs.getAlarmFrequencyHours();
	    	TextView textView = (TextView)findViewById(R.id.textView);
	    	textView.setText(String.format(textView.getText().toString(), 
	    						String.valueOf(alarmFreqHours),
                                alarmFreqHours==1 ? getString(R.string.hour) : getString(R.string.hours),
	    						String.valueOf(onTime) + onAMPM, 
	    						String.valueOf(offTime) + offAMPM
	    						)
	    					);
	    	
	    	findViewById(R.id.continueButton).setOnClickListener(new OnClickListener() {
	    		public void onClick(View v) {
                    Intent intent = new Intent(WizardActivity.this, MenuActivity.class);
                    startActivity(intent);
	    			finish();
	    		}
	    	});

            setTitle("All Done");
	    }
    }
 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == CHOOSE_CONTACTS_REQUEST) {
    		// back from picking friends and family
    		if(resultCode == RESULT_OK) {
                state = SET_ALARM_STATE;
    			populateUI();
    		}
    	} else if(requestCode == SET_ALARM_REQUEST) {
    		// back from setting up alarm
    		if(resultCode == RESULT_OK) {
    			state = WIZARD_DONE_STATE;
    			populateUI();
    		}
    	}
    }
}