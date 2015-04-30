package pisada.fallDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pisada.database.FallSqlHelper;
import pisada.database.SessionDataSource;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements FragmentCommunicator{
	private List<NavDrawerItem> listItems;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private FallDetectorFragment fragment;
	private int currentUIIndex = 0;
	private SessionDataSource sessionData;
	private FragmentManager fm;
	private final int SESSION_DETAILS_ID = -1;
	private final int FALL_DETAILS_ID = -2;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fm = getSupportFragmentManager();
		sessionData = new SessionDataSource(this); 
		setContentView(R.layout.activity_navigation_drawer);
		String[] arr = (getResources().getStringArray(R.array.navigation_items));
		listItems = new ArrayList<NavDrawerItem>();
		listItems.add(new NavDrawerItem(arr[0], R.drawable.ic_launcher)); //TODO icone adatte
		listItems.add(new NavDrawerItem(arr[1], R.drawable.ic_launcher));
		listItems.add(new NavDrawerItem(arr[2], R.drawable.ic_launcher));
		listItems.add(new NavDrawerItem(arr[3], R.drawable.ic_launcher));
		listItems.add(new NavDrawerItem(arr[4], R.drawable.ic_launcher));

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		mDrawerList.setAdapter(new NavDrawListAdapter(this,  listItems));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, android.R.drawable.ic_lock_idle_alarm, R.string.app_name) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				if(currentUIIndex > -1)
					mDrawerList.setItemChecked(currentUIIndex, true);

			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				if(currentUIIndex > -1)
					mDrawerList.setItemChecked(currentUIIndex, true);

			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		/*
		 * di default mettiamo la currentsessionactivity
		 */
		fragment = new CurrentSessionFragment();
		// Insert the fragment by replacing any existing fragment
		fm.beginTransaction()
		.replace(R.id.content_frame, (Fragment)fragment)
		.commit();
	}

	/*
	 * metodi view che rimandano ai fragment ma vengono automaticamente chiamati qui
	 */
	public void playPauseService(View v){
		fragment.playPauseService(v);
	}

	public void stopService(View v)
	{
		fragment.stopService(v);
	}

	public void addSession(View v)
	{
		fragment.addSession(v);
	}

	public void currentSessionDetails(View v)
	{
		fragment.currentSessionDetails(v);
	}


	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		/*
		 * chiamato quando viene selezionato un elemento dal navigation drawer
		 */
		currentUIIndex = position;
		switch(position)
		{
		case 0:
			fragment = new CurrentSessionFragment();
			break;
		case 1:
			fragment = new SessionsListFragment();
			break;
		case 2:
			fragment = new ArchiveFragment();
			break;
		case 3:
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta gi� runnando e non richiami oncreate
			startActivity(intent);
			SettingsActivity.setActivity(this);
			Intent intent2 = new Intent(MainActivity.this, CurrentSessionFragment.class);
			currentUIIndex = 0; //sto rimandando la schermata a currentsession
			switchFragment(intent2);

			break;
		case 4:
			fragment = new InfoFragment();
			break;
		default:

			break;
		}


		fm.beginTransaction()
		.replace(R.id.content_frame, (Fragment)fragment)
		.commit();

		/*Bundle args = new Bundle();
        args.putInt(CurrentSessionFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);*/

		// Insert the fragment by replacing any existing fragment

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(listItems.get(position).getTitle());
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}


	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		try{
			menu.findItem(R.id.rename_session).setVisible(!drawerOpen);
		}
		catch(NullPointerException e)
		{
			/*
			 * non faccio niente, significa che la view non c'� ancora
			 */
		}
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}




	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		int id = item.getItemId();

		switch (id) {
		case R.id.rename_session:
		{

			if(currentUIIndex == 0){ 
				final EditText input = new EditText(this);

				new AlertDialog.Builder(this)
				.setTitle("Rename")
				.setMessage("Insert name")
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString(); 
						String tmp = fragment.getSessionName();

						if(!sessionData.existSession(value) && sessionData.existCurrentSession()){
							sessionData.renameSession(sessionData.currentSession(), value);
							setTitle(value);
							fragment.setSessionName(value);
						}
						else if(sessionData.existSession(value))
						{
							Toast.makeText(MainActivity.this, "Can't add session with same name!", Toast.LENGTH_LONG).show();
							fragment.setSessionName(tmp);
							setTitle(fragment.getSessionName());
						}

					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
				return true;
			}
			else if(currentUIIndex == this.SESSION_DETAILS_ID)//altrimenti passo il lavoro al fragment che sarebbe il sessionDetails
				return false;
		}
		return true;
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta gi� runnando e non richiami oncreate
			startActivity(intent);
			SettingsActivity.setActivity(this);
			Intent intent2 = new Intent(this, CurrentSessionFragment.class);
			this.switchFragment(intent2);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}



	@Override
	public void onBackPressed()
	{
		int count = this.getSupportFragmentManager().getBackStackEntryCount();
		if(currentUIIndex == 0)
			finish();
		else if(currentUIIndex < 0)
		{
			if (fm.getBackStackEntryCount() > 0) {
				fm.popBackStackImmediate();
				List<Fragment> list = fm.getFragments();
				FallDetectorFragment frag = (FallDetectorFragment)fm.getFragments().get(count>0?count-1:count);
				currentUIIndex = getFragmentIndex(frag);
			} else {
				super.onBackPressed();  
			}
		}
		else
		{
			Intent toSamu = new Intent(this, CurrentSessionFragment.class);
			this.switchFragment(toSamu);
		}

		FallDetectorFragment frag = (FallDetectorFragment)fm.getFragments().get(count>0?count-1:count);
		
		fragment = frag;
		
		invalidateOptionsMenu();

	}

	private int getFragmentIndex(FallDetectorFragment frag) {
		// TODO Auto-generated method stub
		return frag.getType();
	}

	@Override
	public void switchFragment(Intent i) {

		if (i.getComponent().getClassName().contains("CurrentSessionFragment")){
			currentUIIndex = 0;
			/*svuoto back stack*/
			for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
				fm.popBackStack();
			}
			fragment = new CurrentSessionFragment();
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();
		}
		else if (i.getComponent().getClassName().contains("SessionsListFragment")){
			currentUIIndex = 1;
			/*svuoto back stack*/
			for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
				fm.popBackStack();
			}
			fragment = new SessionsListFragment();
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();
		}
		else if (i.getComponent().getClassName().contains("SessionDetailsFragment")){
			currentUIIndex = this.SESSION_DETAILS_ID;// non appare nel nav draw
			unselectAllLines();
			fragment = new SessionDetailsFragment();
			Bundle args = new Bundle();
			args.putString(Utility.SESSION_NAME_KEY, i.getStringExtra(Utility.SESSION_NAME_KEY));
			fragment.setArguments(args);
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment).addToBackStack("sessiondetails")
			.commit();
		}
		else if (i.getComponent().getClassName().contains("FallDetailsFragment")){
			currentUIIndex = this.FALL_DETAILS_ID;// non appare nel nav draw
			unselectAllLines();
			fragment = new FallDetailsFragment();
			Bundle args = new Bundle();
			args.putString(Utility.SESSION_NAME_KEY, i.getStringExtra(Utility.SESSION_NAME_KEY));
			args.putLong(Utility.FALL_TIME_KEY, i.getLongExtra(Utility.FALL_TIME_KEY, -1));
			fragment.setArguments(args);
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment).addToBackStack("falldetails")
			.commit();
		}
		else if(i.getComponent().getClassName().contains("ArchiveFragment")){
			currentUIIndex = 2;
			/*svuoto back stack*/
			for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
				fm.popBackStack();
			}
			fragment = new ArchiveFragment();
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();
		}
		else if(i.getComponent().getClassName().contains("InfoFragment")){
			currentUIIndex = 4;
			/*svuoto back stack*/
			for(int j = 0; j < fm.getBackStackEntryCount(); ++j) {    
				fm.popBackStack();
			}
			fragment = new InfoFragment();
			fm.beginTransaction()
			.replace(R.id.content_frame, (Fragment)fragment)
			.commit();
		}
		if(currentUIIndex > -1)
			mDrawerList.setItemChecked(currentUIIndex, true);
		invalidateOptionsMenu();
	}

	private void unselectAllLines()
	{
		for(int i = 0; i < listItems.size(); i++){
			mDrawerList.setItemChecked(i, false);
		}
	}


}
