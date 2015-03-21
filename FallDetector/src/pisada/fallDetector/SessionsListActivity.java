package pisada.fallDetector;

/*
 * questa è la mainactivity. Vorrei farla in stile Google FIT (dateci un'occhiata) così rispettiamo
 * le richieste di mettere lo start stop (bottone in alto iniziale) nella stessa schermata
 * della lista di sessions..
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SessionsListActivity extends ActionBarActivity {

	Intent serviceIntent;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sessions_list);
		
		//===============================================
		//service debug start
		//===============================================
		
		
		serviceIntent = new Intent(this, ForegroundService.class);
		startService(serviceIntent);
		//===============================================
		//service debug end
		//===============================================
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sessions_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			if(serviceIntent!=null)
				stopService(serviceIntent);
			return true;
		}
		if(id == R.id.action_settings_2)
		{
			if(serviceIntent!=null)
				startService(serviceIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
