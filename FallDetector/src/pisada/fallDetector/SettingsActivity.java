package pisada.fallDetector;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	private ActionBar actionBar;
	private Preference smsNotiPref, maxDurationSession, sampleRate, alarmTime;
	private static AlarmManager alarmManager;
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	@Override
	protected boolean isValidFragment(String fragmentName) {
		String miouno = SimplePreferenceFragment.class.getName();
		String miodue = GeneralPreferenceFragment.class.getName();
		  return (fragmentName.equals(SimplePreferenceFragment.class.getName()) ||
				  fragmentName.equals(GeneralPreferenceFragment.class.getName()));
		}
	
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.material_green_700)));
		
		
		actionBar.setDisplayHomeAsUpEnabled(true);
	//	setupSimplePreferencesScreen();
		
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	
	
	
	
	private void setupSimplePreferencesScreen() {



		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Display the fragment as the main content.
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction mFragmentTransaction = mFragmentManager
				.beginTransaction();
		SimplePreferenceFragment mPrefsFragment = new SimplePreferenceFragment();
		mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
		mFragmentTransaction.commit();

		/*
		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Add 'notifications' preferences, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_notifications);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_notification);
		smsNotiPref = (Preference) findPreference("sms_list");
		smsNotiPref.setOnPreferenceClickListener(clickListener);
		 */


	}
	
	public static class SimplePreferenceFragment extends PreferenceFragment {
		private Preference smsNotiPref, maxDurationSession, sampleRate, alarmTime;
		private static AlarmManager alarmManager;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			/*PreferenceCategory header2 = new PreferenceCategory(getActivity());
			header2.setTitle(R.string.pref_header_general);
			getPreferenceScreen().addPreference(header2);
			 */addPreferencesFromResource(R.xml.pref_general);
			 
			 
			 PreferenceCategory header1 = new PreferenceCategory(getActivity());
			 header1.setTitle(R.string.pref_header_notifications);
			 getPreferenceScreen().addPreference(header1);
			 addPreferencesFromResource(R.xml.pref_notification);
			 smsNotiPref = (Preference) findPreference("sms_list");
			 smsNotiPref.setOnPreferenceClickListener(clickListener);
			 sampleRate = (Preference) findPreference("sample_rate");
			 maxDurationSession = (Preference) findPreference("max_duration_session");
			 alarmTime = (Preference) findPreference("time");
			 bindPreferenceSummaryToValue(findPreference("sample_rate"));

				bindPreferenceSummaryToValue(findPreference("sms_list"));
				bindPreferenceSummaryToValue(findPreference("max_duration_session"));
				bindPreferenceSummaryToValue(findPreference("time"));
		}
		
		private Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), ContactsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta gi� runnando e non richiami oncreate
				startActivity(intent);


				return false;
			}


		};

		
	}
	

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	
	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		boolean returnedBool =  (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;

		return returnedBool;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			}  else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		private Preference smsNotiPref, maxDurationSession, sampleRate, alarmTime;
		private static AlarmManager alarmManager;
		
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			 PreferenceCategory header1 = new PreferenceCategory(getActivity());
			 header1.setTitle(R.string.pref_header_notifications);
			 getPreferenceScreen().addPreference(header1);
			 addPreferencesFromResource(R.xml.pref_notification);
			 smsNotiPref = (Preference) findPreference("sms_list");
			 smsNotiPref.setOnPreferenceClickListener(clickListener);
			 sampleRate = (Preference) findPreference("sample_rate");
			 maxDurationSession = (Preference) findPreference("max_duration_session");
			 alarmTime = (Preference) findPreference("time");
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("sample_rate"));
			bindPreferenceSummaryToValue(findPreference("max_duration_session"));
		}
		
		private Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), ContactsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta gi� runnando e non richiami oncreate
				startActivity(intent);


				return false;
			}


		};
	}


	/**
	 * This fragment shows notification preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends
			PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
		}
	}

	
	
	

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if(key.equals("sample_rate"))
		{
			String newValue = sharedPreferences.getString(key, "");
			ForegroundService.MAX_SENSOR_UPDATE_RATE = Integer.parseInt(newValue);
		}
		else if(key.equals("max_duration_session"))
		{
			String newValue = sharedPreferences.getString(key, "");
			String value = newValue;
			if(Integer.parseInt(value) < 1 || Integer.parseInt(value)>24)
			{
				Toast.makeText(this, "Insert a value between 1 and 24", Toast.LENGTH_SHORT).show();
			}
		}
		else if(key.equals("time"))
		{
			String newValue = sharedPreferences.getString(key, "");
			Context c = this;
			String value = newValue;
			List<String> splittedList = Arrays.asList(value.split(":"));
			int hour = Integer.parseInt(splittedList.get(0));
			int minute = Integer.parseInt(splittedList.get(1));
			boolean am = hour<12;
			if(!am)
			{
				hour -= 12;
				am = false;
			}

			Intent myIntent = new Intent(c,  NotificationReceiver.class/*StartSessionReminderActivity.class*/);     
			myIntent.setAction("pisada.NOTIFICATION");
			alarmManager = (AlarmManager)c.getSystemService(ALARM_SERVICE);
		
			
	
			/*
			 * cancello tutte allarmi
			 */
			PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, myIntent, 0);
			try {
		        alarmManager.cancel(pendingIntent);

		    } catch (Exception e) {
		    	
		    }
			
			
			//get time from database and initialise the variables.


			Calendar now = Calendar.getInstance();
			Calendar calendar = Calendar.getInstance();
		//	calendar.setTimeInMillis(System.currentTimeMillis());
			
			calendar.set(Calendar.HOUR, hour);  
			if(am)
				calendar.set(Calendar.AM_PM, Calendar.AM);
			else
				calendar.set(Calendar.AM_PM, Calendar.PM);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
			
			if(calendar.before(now))
				calendar.add(Calendar.DATE, 1); //se � passato non suonare
			
			/*setInexactRepeating*/
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
			Toast.makeText(c, getResources().getString(R.string.alarmsuccess), Toast.LENGTH_LONG).show();

		}
		
		
	}

	
	
	
	
	
	
	
}
