package pisada.fallDetector;

/*
 * questa è la mainactivity. Vorrei farla in stile Google FIT (dateci un'occhiata) così rispettiamo
 * le richieste di mettere lo start stop (bottone in alto iniziale) nella stessa schermata
 * della lista di sessions..
 */
//Samuele gay
//
//Canaglia
import pisada.database.SessionDataSource;
import pisada.recycler.SessionListCardAdapter;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import fallDetectorException.BoolNotBoolException;



public class SessionsListFragment extends FallDetectorFragment {

	private RecyclerView rView;
	private static SessionListCardAdapter cardAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	public static int counter;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private static SessionDataSource sessionData;
	Intent serviceIntent;
	Activity activity;


	public SessionsListFragment()
	{
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(android.view.LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_sessions_list, container, false);
	};


	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		activity = a;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		//APRO CONNESSIONI AL DATABASE
		sessionData=new SessionDataSource(activity);

		//INIZIALIZZO RECYCLERVIEW

		rView=(RecyclerView) getView().findViewById(R.id.session_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter=new SessionListCardAdapter(activity, rView);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(activity);
		rView.setLayoutManager(mLayoutManager);
		rView.setItemAnimator(new DefaultItemAnimator());

	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.sessions_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	public void onResume(){
		super.onResume();
		sessionData.open();
		cardAdapter.check();
	}


	@Override
	public void addSession(View v) throws BoolNotBoolException{

		Intent toSamu = new Intent(activity, CurrentSessionFragment.class);
		((FragmentCommunicator)activity).switchFragment(toSamu);

	}
 @Override
	public void currentSessionDetails(View v){

		this.addSession(v);
	}


}



