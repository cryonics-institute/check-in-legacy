Certificate password: keepmealive

In detail:

When the app is installed, it asks the user to select 1 or more friends and family from their contacts list, 
and also the frequency of the prompts during the day and at again for at night. Options vary from not at all, to every 12 hours.

The app signals the user every X hours by bleeping loudly and vibrating. 
They do not need to unlock the phone to answer the prompt.

Big text and buttons allow them to easily silence the phone by pressing YES or NO. If they click yes, the alarm resets. 
If they click NO or cannot get to the phone in a specified amount of time, a message is sent to the friends and family asking 
them to get in touch. 

This compliments existing emergency button systems found in many peoples' homes which signal emergency services when pressed. 
In cases where someone is too weak to get to the button, this free system provides an early warning, allowing friends and family to 
check in on them, potentially avoiding a life threatening scenario. 
 
Flow:

Launch app from icon/launcher -> WelcomeActivity on first run, otherwise SetupActivity

WelcomeActivity: Welcome message, "Let's get started..." button -> PickContactActivity

SetupActivity: Buttons "Pick friends and family", "Set up alarm".

PickContactActivity: Displays a list of contacts "Richard Mobile", "Richard Home" and so on.

ContactPickedActivity: Let's user pick another contact (redirect to PickContactActivity), or say "That's enough for now" -> SetupAlarmActivity.

SetupAlarmActivity: 2 screens, During the day from: "9am" to "6pm", set alarm off every: "1, 2, 3 hours, no alarm during day". Night (same). "Next" button 

SetupCompleteActivity: "Thank you message", "Done button"

AlarmActivity: "Are you OK", "YES", "NO".

Accessibility:

http://developer.android.com/guide/practices/design/accessibility.html
http://developer.android.com/reference/android/view/accessibility/package-summary.html
http://developer.vodafone.com/smartaccess2011/edf/
