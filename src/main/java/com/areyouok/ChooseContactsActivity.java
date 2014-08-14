package com.areyouok;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.areyouok.data.Contact;
import com.areyouok.data.Extras;
import com.areyouok.prefs.Prefs;
import com.areyouok.util.Util;

/**
 * Allow user to pick a contact from phonebook, or add a number manually
 */
public class ChooseContactsActivity extends ActionBarActivity {

    static int PICK_CONTACT_REQUEST = 0;
	
	private ArrayList<Contact> mContacts = new ArrayList<Contact>();
	private final ArrayList<Contact> mJustPickedContacts = new ArrayList<Contact>();
	
	private Button mDoneButton;
	private int mSendMessageAfterPickingID = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.choose_contacts_activity);
		
		findViewById(R.id.phonebookButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
				contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
			    startActivityForResult(contactPickerIntent, PICK_CONTACT_REQUEST); 
			}
		});
		findViewById(R.id.manualNumberButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				promptForManualNumber();
			}
		});
		mDoneButton = (Button)findViewById(R.id.doneButton);
        mDoneButton.setEnabled(false);
		mDoneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mContacts.size()>0) {
					setResult(1);
				} else {
					setResult(0);
				}
				finish();
			}
		});
		
		mContacts = Prefs.getContacts();
		updateContactsList();
		
		// if we've arrived on this screen because the user has sent for help
		// but had no contacts, we will send a message as soon as they pick someone
		if(getIntent().hasExtra(Extras.PICK_CONTACT_AND_SEND_MESSAGE)) {
			mSendMessageAfterPickingID = getIntent().getExtras().getInt(Extras.PICK_CONTACT_AND_SEND_MESSAGE);
		}
	}
	
	private void updateContactsList() {
		ListView contactsList = (ListView)findViewById(R.id.contactsList);
		contactsList.setAdapter(new ContactsListAdapter(this));
		mDoneButton.setEnabled(mContacts.size()>0);
	}
	
	private void promptForManualNumber() {
		final LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        int padding = Util.dpToPx(8, this);
        params.setMargins(padding, padding, padding, padding);

        final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_PHONE);
		input.setHint("e.g. 07123456789");
        input.setLayoutParams(params);
        input.requestFocus();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // counter the dark text style used with our EditText styles
            input.setTextColor(0xffdddddd);
        }

        final LinearLayout view = new LinearLayout(this);
        view.addView(input);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle("Please enter phone number")
            .setView(view)
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    final String number = value.toString();
                    if (number.length() > 0) {
                        promptForManualName(number);
                    }
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Do nothing.
		        }
		    }).create();

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();


	}
	
	private void promptForManualName(final String number) {
        final LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        int padding = Util.dpToPx(8, this);
        params.setMargins(padding, padding, padding, padding);

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		input.setHint("e.g. John");
        input.setLayoutParams(params);
        input.requestFocus();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // counter the dark text style used with our EditText styles
            input.setTextColor(0xffdddddd);
        }

        final LinearLayout view = new LinearLayout(this);
        view.addView(input);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle("Please enter person's name")
            .setView(view)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    String name = value.toString();
                    addContact(new Contact(name, number));
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
			Uri contactData = data.getData();
			Cursor c = managedQuery(contactData, null, null, null, null);
			if (c.moveToFirst()) {
//				String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
				String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String number = c.getString(c.getColumnIndex("data1"));
				
				addContact(new Contact(name, number));
			}
			// disabled old-style which invoked a context menu which listed all numbers for a contact
//				String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
//				String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//				String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//				
//				if (hasPhone.equalsIgnoreCase("1")) {
//					Cursor phones = getContentResolver().query(
//							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//							null,
//							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
//									+ " = " + id, null, null);
//					phones.moveToFirst();
//					mJustPickedContacts.clear();
//					do {
//						String number = phones.getString(phones.getColumnIndex("data1"));
//						mJustPickedContacts.add(new Contact(name, number));
//						
//					} while(phones.moveToNext());
//					
//					// show ContextMenu to ask user to pick mobile (contacts may have multiple numbers)
//					Button phoneBookButton = (Button)findViewById(R.id.phonebookButton); 
//					registerForContextMenu(phoneBookButton);
//					openContextMenu(phoneBookButton);
//					unregisterForContextMenu(phoneBookButton);
//				}
//			}
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
	    menu.setHeaderTitle("Please pick number to add");
	    int i = 0;
	    for (Contact contact : mJustPickedContacts) {
	    	menu.add(0, i, i, contact.number);
	    	i++;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		addContact(mJustPickedContacts.get(id));
		return super.onContextItemSelected(item);
	}
	
	private void addContact(Contact contact) {
		mContacts.add(contact);
		Prefs.setContacts(mContacts);
		
		updateContactsList();
		
		if(mSendMessageAfterPickingID == Extras.SEND_FOR_HELP_MESSAGE) {
			SMSSender.sendEmergencySMS(this);
		} else if(mSendMessageAfterPickingID == Extras.SEND_IM_OK_MESSAGE) {
			SMSSender.sendImOKSMS(this);
		}
	}
	
	private OnClickListener mOnDeleteClickListener = new OnClickListener() {
		public void onClick(View v) {
			Contact contact = (Contact)v.getTag();
			mContacts.remove(contact);
			Prefs.setContacts(mContacts);
			updateContactsList();
		}
	};

    class ContactsListAdapter extends BaseAdapter {
		Context mContext;
		
		public ContactsListAdapter(Context context) {
			mContext = context;
		}
		
		public int getCount() {
			return mContacts.size();
		}

		public Object getItem(int position) {
			return mContacts.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
		
		public boolean isEnabled(int position) {
			return false;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			final Contact contact = mContacts.get(position);
			
			if(convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(mContext).inflate(R.layout.contact_list_row, null);
			}
			
			int len = Math.min(contact.name.length(), 20);
			((TextView)view.findViewById(R.id.label)).setText(contact.name.substring(0,len) + "\n("+ contact.number + ")");
			
			ImageButton deleteButton = (ImageButton)view.findViewById(R.id.deleteButton);
			deleteButton.setOnClickListener(mOnDeleteClickListener);
			deleteButton.setTag(contact);
			
			return view;
		}
	}	
}
