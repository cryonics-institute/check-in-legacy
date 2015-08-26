package com.cryonicsinstitute;

import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cryonicsinstitute.data.Contact;
import com.cryonicsinstitute.data.Extras;
import com.cryonicsinstitute.prefs.Prefs;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Main menu
 */
public class MenuActivity extends BaseActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    /**
     * Receiver which is configured to listen to pending SMS sending operation
     */
    final BroadcastReceiver mSMSSenderBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             boolean success = false;
             String error = "";
             int resultCode = getResultCode();
             switch (resultCode) {
             case Activity.RESULT_OK:
                 success = true;
                 break;
             case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                 error = "Check credit";
                 break;
             case SmsManager.RESULT_ERROR_NO_SERVICE:
                 error = "No service/signal";
                 break;
             case SmsManager.RESULT_ERROR_NULL_PDU:
                 error = "Null PDU";
                 break;
             case SmsManager.RESULT_ERROR_RADIO_OFF:
                 error = "Radio is turned off/in flight mode";
                 break;
             case 5: //SmsManager.RESULT_ERROR_LIMIT_EXCEEDED:
                 error = "Permission denied in app settings"; // they said no to "this app will charge you" permission
                 break;
             }

             if(success) {
                 String names = intent.getStringExtra("names");
                 AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                 builder.setMessage("SMS sent to " + names);
                 builder.setNegativeButton("OK", null);
                 builder.create().show();
             } else {
                 boolean isCallForHelp = intent.getBooleanExtra("isCallForHelp", false);
                 AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                 builder.setMessage("Unable to send text message! ("+error+")");
                 builder.setNegativeButton("OK", null);
                 if(isCallForHelp) {
                     builder.setPositiveButton("Dial Emergency Service", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             Uri number = Uri.parse("tel:112");
                             Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                             startActivity(callIntent);
                         }
                     });
                 }
                 builder.create().show();
             }

             if(mProgressDialog != null) {
                 mProgressDialog.dismiss();
             }

             unregisterReceiver(mSMSSenderBroadcastReceiver);
             mIsSMSSenderReceiverRegistered = false;
         }
     };

    private boolean mIsSMSSenderReceiverRegistered;
    private ProgressDialog mProgressDialog;
    private ImageView statusIcon;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boolean isFirstRun = Prefs.getIsFirstRun();
		
		if(isFirstRun) {
			Intent intent = new Intent(this, WizardActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return;
		}
		
		setContentView(R.layout.menu_activity);

        statusIcon = (ImageView) findViewById(R.id.statusIcon);
		
		findViewById(R.id.call_for_help_button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showCallForHelpAlert();
            }
        });

		findViewById(R.id.tell_friends_ok_button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showImOKAlert();
            }
        });

		findViewById(R.id.settings_button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
		findViewById(R.id.exit_button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        ((TextView)findViewById(R.id.version_textview)).setText("Version: " + BuildConfig.VERSION_NAME);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                // previously rejected permission
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this);
                builder.setMessage("SMS permission is required to function, click OK to accept the permissions request.");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        requestPermissions();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        MenuActivity.this.finish();
                    }
                });
                builder.create().show();

            } else {
                requestPermissions();
            }
        }
	}
	
	@Override
    protected void onResume() {
		super.onResume();
		
		boolean isAlarmOn = Prefs.getAlarmEnabled();
		TextView introText = (TextView)findViewById(R.id.introTextView);

		if(isAlarmOn) {
            String nextAlarmTimeStr = "Unknown time";
            long nextAlarmTime = Prefs.getNextAlarmTime();
            if(nextAlarmTime!=-1) {
                DateTime date = new DateTime(nextAlarmTime);
                DateTimeFormatter fmt = DateTimeFormat.forPattern("ha");
                nextAlarmTimeStr = date.toString(fmt);
            }

			introText.setText(getString(R.string.introAlarmOn, nextAlarmTimeStr));
			introText.setTextColor(0xff000000);
            statusIcon.setImageResource(R.drawable.ic_thumbs_up);
		} else {
			introText.setText(R.string.introAlarmOff);
			introText.setTextColor(0xffff0000);
            statusIcon.setImageResource(R.drawable.ic_warning);
		}
	}

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_CODE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsSMSSenderReceiverRegistered) {
            unregisterReceiver(mSMSSenderBroadcastReceiver);
            mIsSMSSenderReceiverRegistered = false;
        }
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private AlertDialog permissionsRejectedDialog;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    // permission was granted, woohoo!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    if(permissionsRejectedDialog == null) {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(this);
                        builder.setMessage("SMS permissions are required to function, please re-install or check your phone's Application Settings if you're still having trouble.");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                MenuActivity.this.finish();
                            }
                        });
                        permissionsRejectedDialog = builder.create();
                        permissionsRejectedDialog.show();
                    }
                }
            }
        }
    }

    private void showCallForHelpAlert() {
		
		ArrayList<Contact> contacts = Prefs.getContacts();
		if(contacts.isEmpty()) {
			new AlertDialog.Builder(MenuActivity.this)
			.setMessage(R.string.you_havent_chosen_friends_family_message)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					Intent intent = new Intent(MenuActivity.this, ChooseContactsActivity.class);
					intent.putExtra(Extras.PICK_CONTACT_AND_SEND_MESSAGE, Extras.SEND_FOR_HELP_MESSAGE);
					startActivity(intent);
				}
			}).show();
			
		} else {
			new AlertDialog.Builder(MenuActivity.this)
			.setMessage(R.string.send_for_help_dialog_message)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
                    mProgressDialog = ProgressDialog.show(MenuActivity.this, null, getString(R.string.sending_), true);
                    registerSMSSentReceiver();
					SMSSender.sendEmergencySMS(MenuActivity.this, lastKnownLocation);
				}
			}).setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			}).show();
		}
	}
	
	private void showImOKAlert() {
		ArrayList<Contact> contacts = Prefs.getContacts();
		if(contacts.isEmpty()) {
			new AlertDialog.Builder(MenuActivity.this)
			.setMessage(R.string.you_havent_chosen_friends_family_message)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					final Intent intent = new Intent(MenuActivity.this, ChooseContactsActivity.class);
					intent.putExtra(Extras.PICK_CONTACT_AND_SEND_MESSAGE, Extras.SEND_IM_OK_MESSAGE);
					startActivity(intent);
				}
			}).show();
			
		} else {
			new AlertDialog.Builder(MenuActivity.this)
		    .setMessage(R.string.let_designated_know_youre_ok_message)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	dialog.dismiss();
                    CryonicsCheckinApp.cancelCountdownToSMSTimer();
                    mProgressDialog = ProgressDialog.show(MenuActivity.this, null, "Sending...", true);
                    registerSMSSentReceiver();
		        	SMSSender.sendImOKSMS(MenuActivity.this);
		        }
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	dialog.dismiss();
		        }
		    }).show();
		}
	}

    private void registerSMSSentReceiver() {
        registerReceiver(mSMSSenderBroadcastReceiver, new IntentFilter(SMSSender.SMS_SENT_ACTION));
        mIsSMSSenderReceiverRegistered = true;
    }
}
