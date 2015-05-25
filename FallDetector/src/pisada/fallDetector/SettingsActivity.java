package pisada.fallDetector;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;


public class SettingsActivity extends AppCompatActivity {
	
	private ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
		
		
	}

	

	private void setupSimplePreferencesScreen() {



		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Display the fragment as the main content.
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction mFragmentTransaction = mFragmentManager
				.beginTransaction();
		GeneralPreferenceFragment mPrefsFragment = new GeneralPreferenceFragment();
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




	public static class GeneralPreferenceFragment extends PreferenceFragment {
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
			 sampleRate.setOnPreferenceChangeListener(sampleRateChangeListener);
			 maxDurationSession = (Preference) findPreference("max_duration_session");
			 maxDurationSession.setOnPreferenceChangeListener(sessionDurationChangeListener);
			 alarmTime = (Preference) findPreference("time");
			 alarmTime.setOnPreferenceChangeListener(alarmTimeChangeListener);
		}

		private Preference.OnPreferenceChangeListener sampleRateChangeListener = new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ForegroundService.MAX_SENSOR_UPDATE_RATE = Integer.parseInt((String)newValue);
				return true;
			}
		};


		private Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), ContactsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
				startActivity(intent);


				return false;
			}


		};

		private Preference.OnPreferenceChangeListener sessionDurationChangeListener = new Preference.OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				String value = newValue.toString();
				if(Integer.parseInt(value) < 1 || Integer.parseInt(value)>24)
				{
					Toast.makeText(getActivity(), "Insert a value between 1 and 24", Toast.LENGTH_SHORT).show();
					//settings.edit().putString("max_duration_session",lastValidDurationValue);
					return false;
				}
				ForegroundService.TIMEOUT_SESSION = Long.parseLong(value) *  3600000;
				return true;
			}


		};

		private Preference.OnPreferenceChangeListener alarmTimeChangeListener = new Preference.OnPreferenceChangeListener(){



			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Context c = getActivity();
				String value = newValue.toString();
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
					calendar.add(Calendar.DATE, 1); //se è passato non suonare
				
				/*setInexactRepeating*/
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
				Toast.makeText(c, getResources().getString(R.string.alarmsuccess), Toast.LENGTH_LONG).show();
				
				return true;
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


	/*
	Intent myIntent = new Intent(this , NotifyService.class);     
	AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
	pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);

	//get time from database and initialise the variables.
	int minute;
	int hour;

	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.SECOND, 0);
	calendar.set(Calendar.MINUTE, minute);
	calendar.set(Calendar.HOUR, hour);
	calendar.set(Calendar.AM_PM, Calendar.AM);    //set accordingly
	calendar.add(Calendar.DAY_OF_YEAR, 0);

	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
	 */

}
