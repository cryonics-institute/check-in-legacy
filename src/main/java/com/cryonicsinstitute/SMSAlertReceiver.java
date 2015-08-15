package com.cryonicsinstitute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cryonicsinstitute.prefs.Prefs;

public class SMSAlertReceiver extends BroadcastReceiver {
    public static final int MAX_EMERGENCY_SMS_TO_SEND = 5;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AYO", "SMSAlertReceiver invoked.");
        // limit help messages to a max
        if(Prefs.getSentSMSCount() < MAX_EMERGENCY_SMS_TO_SEND) {

            // dismiss AlarmActivity
            AlarmActivity.dismiss(CryonicsCheckinApp.app);

            // send a/another emergency SMS
            SMSSender.sendEmergencySMS(CryonicsCheckinApp.app);

            // schedule next SMS
            CryonicsCheckinApp.startCountdownTimer();
        } else {
            // cancelled or SMS count >= MAX_EMERGENCY_SMS_TO_SEND
            Prefs.setSentSMSCount(0);
        }
    }
}
