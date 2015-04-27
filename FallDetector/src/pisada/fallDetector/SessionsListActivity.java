package pisada.fallDetector;


import fallDetectorException.BoolNotBoolException;
import pisada.database.SessionDataSource;
import pisada.recycler.SessionListCardAdapter;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class SessionsListActivity extends ActionBarActivity {

	private RecyclerView rView;
	private static SessionListCardAdapter cardAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	public static int counter;
	private static SessionDataSource sessionData;
	Intent serviceIntent;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sessions_list);
		//APRO CONNESSIONI AL DATABASE
		sessionData=new SessionDataSource(this);

		//INIZIALIZZO RECYCLERVIEW

		rView=(RecyclerView) findViewById(R.id.session_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter=new SessionListCardAdapter(this, rView);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);
		rView.setItemAnimator(new DefaultItemAnimator());

	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sessions_list, menu);
		return true;
	}


	@Override
	public void onResume(){
		super.onResume();
		sessionData.open();
		cardAdapter.check();
		cardAdapter.notifyDataSetChanged();
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);


	}
	@Override
	protected void onPause() {

		super.onPause();


	}

	
	public void addSession(View v) throws BoolNotBoolException{

		
		Intent toSamu = new Intent(this, CurrentSessionFragment.class);
		((FragmentCommunicator)activity).switchFragment(toSamu);
	}

	public void currentSessionDetails(View v){
		this.addSession(v);
	}
	
	


}



