package com.cryonicsinstitute;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.cryonicsinstitute.data.Contact;
import com.cryonicsinstitute.prefs.Prefs;

public class SMSSender {
    public static String SMS_SENT_ACTION = "SMS_SENT";

	public static void sendEmergencySMS(Context context) {
		Log.i("AYO", "Sending Emergency SMS!");
		ArrayList<Contact> contacts = Prefs.getContacts();
		
		for (Contact contact : contacts) {
			sendSMS(context, contact.number,
                    context.getString(R.string.help_message),
                    true);
		}
		
		Prefs.init(context.getApplicationContext());
		Prefs.setSentSMSCount(Prefs.getSentSMSCount() + 1);
		
		Log.i("AYO", "SMS sent in a row: " + Prefs.getSentSMSCount());
	}
	
	public static void sendImOKSMS(Context context) {
		Log.i("AYO", "Sending I'm OK SMS!");
		
		ArrayList<Contact> contacts = Prefs.getContacts();
		
		for (Contact contact : contacts) {
			sendSMS(context, contact.number,
                    context.getString(R.string.im_ok_message),
                    false);
		}
		
		Prefs.init(context.getApplicationContext());
		Prefs.setSentSMSCount(0);

		Log.i("AYO", "SMS sent in a row: " + Prefs.getSentSMSCount());
	}
	
	private static void sendSMS(final Context context, String phoneNumber, String message, boolean isCallForHelp) {
        ArrayList<Contact> contacts = Prefs.getContacts();
        String[] names = new String[contacts.size()];
        int i = 0;
        for (Contact contact : contacts) {
            names[i++] = contact.name;
        }
        final Intent sentIntent = new Intent(SMS_SENT_ACTION);
        sentIntent.putExtra("names", Arrays.toString(names).replace("[", "").replace("]", ""));
        if(isCallForHelp) {
			sentIntent.putExtra("isCallForHelp", true);
		}
		PendingIntent sentPendingIntent =
                PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		final SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPendingIntent, null);
	}
}
