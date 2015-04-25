package pisada.fallDetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SessionDetailsActivity extends ActionBarActivity {

	ActionBar actionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.session, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		switch (id) {
		case android.R.id.home:
			Intent toDaniel = new Intent(this, SessionsListActivity.class);
			toDaniel.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			startActivity(toDaniel);
			this.finish(); 
			return true;
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			startActivity(intent);
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onBackPressed()
	{
		Intent toDaniel = new Intent(this, SessionsListActivity.class);
		toDaniel.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
		startActivity(toDaniel);
		this.finish(); 
	}
}
