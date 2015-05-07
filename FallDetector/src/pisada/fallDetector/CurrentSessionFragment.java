package pisada.fallDetector;

/*
 * riflessioni riguardo i metodi settati in xml:
 * TODO
 * potrei:
 * 1)settarli programmaticamente come onclicklistener, stando attento a non dimenticare niente che eventualmente veniva modificato qui
 * --non c'è altro modo direi--
 */
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.database.FallDataSource.Fall;
import pisada.database.SessionDataSource;
import pisada.recycler.CurrentSessionCardAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import fallDetectorException.DublicateNameSessionException;
import fallDetectorException.MoreThanOneOpenSessionException;

public class CurrentSessionFragment extends FallDetectorFragment implements ServiceReceiver {
	private static Intent serviceIntent;
	private static SessionDataSource sessionData;
	private static CurrentSessionCardAdapter cardAdapter;
	private final int TYPE = 0;
	//private RecyclerView rView;
	private LayoutManager mLayoutManager;
	private String sessionName, sessionNameDefault;
	private boolean startChronometerOnStartActivity = false;
	private long pauseTime = 0;
	private long infoTime = -1;
	private FallDataSource fallDataSource;
	private SessionDataSource.Session currentSession;

	private ActionBar actionBar;

	private Activity activity;
	private Drawable pause, play;

	public int getType()
	{
		return this.TYPE;
	}

	public CurrentSessionFragment()
	{
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.current_session, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_current_session, container, false);  
	}

	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		activity = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstance)
	{
		super.onActivityCreated(savedInstance);
		if(activity == null)
			activity = getActivity(); //ma dovrebbe essere già settata in onAttach()

		serviceIntent = new Intent(activity, ForegroundService.class);


		//INIZIALIZZO DATABASE

		sessionData=new SessionDataSource(activity);

		if(sessionData.existCurrentSession()){

			currentSession = sessionData.currentSession();

			sessionName = currentSession.getName();

			infoTime = currentSession.getStartTime();

			if(!currentSession.isOnPause())
				startChronometerOnStartActivity = true; //FA SI CHE PARTA IL CRONOMETRO AL LANCIO DELL'ACTIVITY


			//SE CURRENTSESSION NON è IN PAUSA E NON C'è IL SERVICE ATTIVO... FAI PARTIRE IL SERVICE (C'è STATA UNA CHIUSURA INASPETTATA)
			if(!currentSession.isOnPause() && !ForegroundService.isRunning()){
				serviceIntent = new Intent(activity, ForegroundService.class);
				String activeServ = Utility.checkLocationServices(activity, true);
				serviceIntent.putExtra("activeServices", activeServ);
				activity.startService(serviceIntent);
				pauseTime = 0;
			}


			else if(currentSession.isOnPause()) //SE invece LA CURRENT SESSION è IN PAUSA... 
			{
				//INIZIALIZZO IL TEMPO DA CUI IL CRONOMETRO DEVE RIPARTIRE
				pauseTime = sessionData.sessionDuration(currentSession);
			}

		}



		//INIZIALIZZO LA RECYCLERVIEW
		rView=(RecyclerView) getView().findViewById(R.id.currentsession_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter = new CurrentSessionCardAdapter(this.getView(), activity, ForegroundService.getSessionDuration(sessionData), startChronometerOnStartActivity, pauseTime, MainActivity.isPortrait);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(activity);
		rView.setLayoutManager(mLayoutManager);
		if(sessionName != null)
			activity.setTitle(sessionName);

		if(infoTime != -1) //quindi esiste anche currentsession
			cardAdapter.setCurrentSessionValues(infoTime, currentSession, -1);

		if(sessionData.existCurrentSession()) //SE INVECE LA CURRENT SESSION è IN PAUSA... 
		{
			if(fallDataSource == null)
				fallDataSource = new FallDataSource(activity);
			ArrayList<FallDataSource.Fall> cadute = fallDataSource.sessionFalls(sessionData.currentSession());
			if(cadute != null) //OCCHIO POTREBBE NASCONDERE PROBLEMI
				for(int i = cadute.size()-1; i >= 0; i--){
					cardAdapter.addFall(cadute.get(i), currentSession);
				}
			cardAdapter.updateSessionName(sessionData.currentSession().getName());
		}

		ForegroundService.connect(this); 

		pause =getResources().getDrawable(R.drawable.button_selector_pause);
		play =getResources().getDrawable(R.drawable.button_selector_play);
		this.scroll(MainActivity.currentSessionFragmentLastIndex);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		ForegroundService.disconnect(this);
		ForegroundService.disconnect(cardAdapter);//disconnette l'activity connessa
	}

	@Override
	public void onResume()
	{
		super.onResume();
		ForegroundService.connect(cardAdapter);

		ForegroundService.connect(this);

		try{
			sessionData.open();
		}
		catch(Exception e)
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			Toast.makeText(activity, errors.toString(), Toast.LENGTH_LONG).show();
		}
	}



	public CurrentSessionCardAdapter getAdapter()
	{
		return cardAdapter;
	}




	@Override
	public void sessionTimeOut() {
		if(serviceIntent != null)
			activity.stopService(serviceIntent);
	}

	@Override
	public void serviceUpdate(float x, float y, float z, long time) {
		//inutile qui
	}

	@Override
	public void serviceUpdate(Fall f, String sessionName) {
		rView.getAdapter().notifyItemChanged(rView.getAdapter().getItemCount()-1);
		rView.scrollToPosition(rView.getAdapter().getItemCount()-1);

	}
	@Override
	public String getSessionName()
	{
		return this.sessionName;
	}
	@Override
	public void setSessionName(String s)
	{
		if(sessionData.existCurrentSession())
			currentSession = sessionData.currentSession();
		sessionName = s;
		cardAdapter.updateSessionName(s);
	}

	@Override
	public boolean equalsClass(ServiceReceiver obj) {

		if(obj instanceof CurrentSessionFragment)
			return true;
		return false;
	}




}
