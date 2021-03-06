package com.cryonicsinstitute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.cryonicsinstitute.prefs.Prefs;

public class SMSAlertReceiver extends BroadcastReceiver {
    public static final int MAX_EMERGENCY_SMS_TO_SEND = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        // user did not respond within X mins
        Log.i("AYO", "SMSAlertReceiver invoked.");

        // limit help messages to a max
        if(Prefs.getSentSMSCount() < MAX_EMERGENCY_SMS_TO_SEND) {

            // dismiss AlarmActivity
            AlarmActivity.dismiss(CryonicsCheckinApp.app);

            // send emergency SMS
            Location location = intent.getParcelableExtra("location");
            SMSSender.sendEmergencySMS(CryonicsCheckinApp.app, location);

            // schedule next SMS
            // NOTE: removed, they only want one SMS to be sent no matter how long we wait
            Prefs.setSentSMSCount(0);
//            CryonicsCheckinApp.startCountdownToSMSTimer();
        } else {
            // cancelled or SMS count >= MAX_EMERGENCY_SMS_TO_SEND
            Prefs.setSentSMSCount(0);
        }
    }
}
