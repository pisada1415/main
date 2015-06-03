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


public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	private ActionBar actionBar;
	private static AlarmManager alarmManager;


	@Override
	protected boolean isValidFragment(String fragmentName) {
		  return (fragmentName.equals(SimplePreferenceFragment.class.getName()) ||
				  fragmentName.equals(GeneralPreferenceFragment.class.getName())) || 
				  fragmentName.equals(NotificationPreferenceFragment.class.getName());
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

	
	
	
	@SuppressWarnings("unused")
	private void setupSimplePreferencesScreen() {


		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction mFragmentTransaction = mFragmentManager
				.beginTransaction();
		SimplePreferenceFragment mPrefsFragment = new SimplePreferenceFragment();
		mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
		mFragmentTransaction.commit();


	}
	
	public static class SimplePreferenceFragment extends PreferenceFragment {
		private Preference smsNotiPref;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			 addPreferencesFromResource(R.xml.pref_general);
			 
			 
			 PreferenceCategory fakeHeader = new PreferenceCategory(getActivity());
			 fakeHeader.setTitle(R.string.pref_header_notifications);
			 getPreferenceScreen().addPreference(fakeHeader);
			 addPreferencesFromResource(R.xml.pref_notification);
			 smsNotiPref = (Preference) findPreference("sms_list");
			 smsNotiPref.setOnPreferenceClickListener(clickListener);
			 bindPreferenceSummaryToValue(findPreference("sample_rate"));

				bindPreferenceSummaryToValue(findPreference("sms_list"));
				bindPreferenceSummaryToValue(findPreference("max_duration_session"));
				bindPreferenceSummaryToValue(findPreference("time"));
		}
		
		private Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), ContactsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
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

	
	private static boolean isXLargeTablet(Context context) {
		boolean returnedBool =  (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;

		return returnedBool;
	}

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
				
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			}  else {
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	
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

	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			 
			
			bindPreferenceSummaryToValue(findPreference("sample_rate"));
			bindPreferenceSummaryToValue(findPreference("max_duration_session"));
		}
		
		
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends
			PreferenceFragment {
		Preference smsNotiPref;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);
			smsNotiPref = (Preference) findPreference("sms_list");
			smsNotiPref.setOnPreferenceClickListener(clickListener);
			
			bindPreferenceSummaryToValue(findPreference("time"));
		}
		private Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), ContactsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
				startActivity(intent);


				return false;
			}


		};

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
			
			

			Calendar now = Calendar.getInstance();
			Calendar calendar = Calendar.getInstance();
			
			calendar.set(Calendar.HOUR, hour);  
			if(am)
				calendar.set(Calendar.AM_PM, Calendar.AM);
			else
				calendar.set(Calendar.AM_PM, Calendar.PM);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
			
			if(calendar.before(now))
				calendar.add(Calendar.DATE, 1); //se è passato non suonare
			
			/*setInexactRepeating*/
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
			Toast.makeText(c, getResources().getString(R.string.alarmsuccess), Toast.LENGTH_LONG).show();

		}
		
		
	}

	
	
	
	
	
	
	
}
