package com.areyouok.prefs;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.areyouok.data.Contact;

/**
 * Easy storage and retrieval of user preferences.
 * Remember you can also use the DataBackup agent to back these up to the cloud.
 */
public class Prefs {
	
	private static String NAME = "shared_prefs";
	private static int MODE = Context.MODE_PRIVATE;
	
	private static Context mContext;
	private static SharedPreferences mPrefs;
	private static Editor mEditor;
	
	// Preference names/consts
	
	private static final String IS_FIRST_RUN = "IS_FIRST_RUN";
	private static final String ALARM_ENABLED = "ALARM_ENABLED";
	private static final String ALARM_OFF_AT = "ALARM_OFF_AT";
	private static final String ALARM_ON_AT = "ALARM_ON_AT";
	private static final String ALARM_FREQUENCY = "ALARM_FREQUENCY";
    private static final String NEXT_ALARM_TIME = "NEXT_ALARM_TIME";
	private static final String SENT_SMS_COUNT = "SENT_SMS_COUNT";
	private static final String RESPOND_TIME = "RESPOND_TIME";
	private static final String CONTACTS = "CONTACTS";

	
	public static void init(Context context) {
		if(context instanceof Application) {
			mContext = context;
		} else {
			mContext = context.getApplicationContext();
		}
		mPrefs = mContext.getSharedPreferences(NAME, MODE);
		mEditor = mPrefs.edit();
	}
	
	// Preference accessors
	
	public static boolean getIsFirstRun() {
		return mPrefs.getBoolean(IS_FIRST_RUN, true);
	}
	public static void setIsFirstRun(boolean flag) {
		mEditor.putBoolean(IS_FIRST_RUN, flag);
		mEditor.commit();
	}
	
	public static boolean getAlarmEnabled() {
		return mPrefs.getBoolean(ALARM_ENABLED, true);
	}
	public static void setAlarmEnabled(boolean flag) {
		mEditor.putBoolean(ALARM_ENABLED, flag);
		mEditor.commit();
	}
	
	/**
	 * 24 hr off time (0-23)
	 * @return
	 */
	public static int getAlarmOffAt() {
		return mPrefs.getInt(ALARM_OFF_AT, 21);
	}
	public static void setAlarmOffAt(int hour) {
		mEditor.putInt(ALARM_OFF_AT, hour);
		mEditor.commit();
	}

	/**
	 * 24 hr on time (0-23)
	 * @return
	 */
	public static int getAlarmOnAt() {
		return mPrefs.getInt(ALARM_ON_AT, 7);
	}
	public static void setAlarmOnAt(int hour) {
		mEditor.putInt(ALARM_ON_AT, hour);
		mEditor.commit();
	}

    /**
     * Next scheduled alarm time
     * @return -1 if not set else Date as milliseconds value (Date.getTime())
     */
    public static long getNextAlarmTime() {
        return mPrefs.getLong(NEXT_ALARM_TIME, -1);
    }
    public static void setNextAlarmTime(long time) {
        mEditor.putLong(NEXT_ALARM_TIME, time);
        mEditor.commit();
    }

	public static int getAlarmFrequencyHours() {
		return mPrefs.getInt(ALARM_FREQUENCY, 4); // hrs
	}
	public static void setAlarmFrequencyHours(int freq) {
		mEditor.putInt(ALARM_FREQUENCY, freq);
		mEditor.commit();
	}
	
	public static int getSentSMSCount() {
		return mPrefs.getInt(SENT_SMS_COUNT, 0);
	}
	public static void setSentSMSCount(int num) {
		mEditor.putInt(SENT_SMS_COUNT, num);
		mEditor.commit();
	}

    /**
     * Time user has to respond to alert before sending SMS
     * (default is 20 mins)
     */
	public static int getRespondTime() {
		return mPrefs.getInt(RESPOND_TIME, 1000*60*20); // 20 mins
//		return mPrefs.getInt(RESPOND_TIME, 1000*60*1); // 1 min
	}
	public static void setRespondTime(int msecs) {
		mEditor.putInt(RESPOND_TIME, msecs);
		mEditor.commit();
	}
	
	public static ArrayList<Contact> getContacts() {
		// contacts stored as name,number,name,number...
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		String contactsCSV = mPrefs.getString(CONTACTS, "");
		String[] contactsArr = contactsCSV.split("\\%");
		if(contactsCSV.length()>0 && contactsArr.length>0) {
			int len = contactsArr.length;
			for(int i=0; i<len; i++) {
				String[] nameNumber = contactsArr[i].split("\\|");
				String name = nameNumber[0];
				String number = nameNumber[1];
				contacts.add(new Contact(name, number));
			}
		}
		return contacts;
	}
	public static void setContacts(ArrayList<Contact> contacts) {
		String contactsCSV = "";
		for (Contact contact : contacts) {
			contactsCSV += contact.name + "|" + contact.number + "%";
		}
		mEditor.putString(CONTACTS, contactsCSV);
		mEditor.commit();
	}
}
