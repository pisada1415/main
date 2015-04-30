package pisada.fallDetector;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.database.FallSqlHelper;
import pisada.database.SessionDataSource;
import pisada.recycler.CurrentSessionCardAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
	private RecyclerView rView;
	private LayoutManager mLayoutManager;
	private String sessionName, sessionNameDefault;
	private boolean startChronometerOnStartActivity = false;
	private long pauseTime = 0;

	private FallDataSource fallDataSource;
	private SessionDataSource.Session currentSession;

	private ActionBar actionBar;

	private Activity activity;

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
        inflater.inflate(R.menu.current_session, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.activity_current_session, container, false);  
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
		sessionNameDefault = getResources().getString(R.string.defaultSessionName);
		sessionName = sessionNameDefault;

		//INIZIALIZZO DATABASE

		sessionData=new SessionDataSource(activity);

		if(sessionData.existCurrentSession()){
			currentSession = sessionData.currentSession();
			sessionName = currentSession.getName();


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


			else if(sessionData.currentSession().isOnPause()) //SE INVECE LA CURRENT SESSION è IN PAUSA... 
			{
				//INIZIALIZZO IL TEMPO DA CUI IL CRONOMETRO DEVE RIPARTIRE
				pauseTime = sessionData.sessionDuration(currentSession);
			}

		}

		String title = activity.getIntent().getStringExtra("title");
		if(title != null && !title.equals(""))
			sessionName = title;

		//INIZIALIZZO LA RECYCLERVIEW
		rView=(RecyclerView) getView().findViewById(R.id.currentsession_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter = new CurrentSessionCardAdapter(activity, ForegroundService.getSessionDuration(sessionData), startChronometerOnStartActivity, pauseTime);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(activity);
		rView.setLayoutManager(mLayoutManager);
		activity.setTitle(sessionName);


		if(sessionData.existCurrentSession()) //SE INVECE LA CURRENT SESSION è IN PAUSA... 
		{
			if(fallDataSource == null)
				fallDataSource = new FallDataSource(activity);
			ArrayList<FallDataSource.Fall> cadute = fallDataSource.sessionFalls(sessionData.currentSession());
			if(cadute != null) //OCCHIO POTREBBE NASCONDERE PROBLEMI
				for(int i = cadute.size()-1; i >= 0; i--){
					cardAdapter.addFall(cadute.get(i), currentSession);
				}
		}

		if(!ForegroundService.isConnected(this)){
			ForegroundService.connect(this); 
		}

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
		if(!ForegroundService.isConnected(cardAdapter)){
			ForegroundService.connect(cardAdapter);
		}
		if(!ForegroundService.isConnected(this)){
			ForegroundService.connect(this);
		}
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

	//CHIAMATO QUANDO VIENE PREMUTO IL TASTO SOPRA (PLAY/PAUSA)
	@Override
	public void playPauseService(View v){

		long time = System.currentTimeMillis(); //MEMORIZZA IL MOMENTO IN CUI è STATO PREMUTO IL TASTO
		if(sessionName.equals(sessionNameDefault)) //cioè non è stato cambiato
			sessionName = "Session:"+ Utility.getStringTime(time); //assegno nome default UNICO (altrimenti tengo quello cambiato)


		if(!ForegroundService.isRunning()){
			//il service non sta andando
			if(!sessionData.existCurrentSession()){
				//non esiste sessione corrente: creane una nuova
				currentSession = null;
				currentSession = addSession(sessionName, "" + time, time, 0);
				activity.setTitle(sessionName);
				CurrentSessionCardAdapter.startChronometer();
				//FA PARTIRE IL SERVICE
				serviceIntent = new Intent(activity, ForegroundService.class);
				String activeServ = Utility.checkLocationServices(activity, true);
				serviceIntent.putExtra("activeServices", activeServ);
				activity.startService(serviceIntent);
			}
			else
			{
				//ESISTE SESSIONE CORRENTE
				if(sessionData.currentSession().isOnPause()){
					//è IN PAUSA
					currentSession = sessionData.currentSession();
					sessionData.resumeSession(currentSession); //LA FACCIO RIPARTIRE
					CurrentSessionCardAdapter.startChronometer();
					//FA PARTIRE IL SERVICE
					serviceIntent = new Intent(activity, ForegroundService.class);
					String activeServ = Utility.checkLocationServices(activity, true);
					serviceIntent.putExtra("activeServices", activeServ);
					activity.startService(serviceIntent);
				}
				else{
					//STA ANDANDO, QUINDI VA MESSA IN PAUSA
					
					sessionData.setSessionOnPause(sessionData.currentSession());
					activity.stopService(serviceIntent); //dovrebbe essere inutile
					cardAdapter.pauseChronometer();
				}
			}

		}
		else
		{
			//il service sta già andando (STESSA COSA DI PRIMA MA QUI NON FA PARTIRE IL SERVICE)
			if(!sessionData.existCurrentSession()){
				//non esiste sessione corrente: creane una nuova
				currentSession = null;
				currentSession = addSession(sessionName, "" + time, time, 0);
				activity.setTitle(sessionName);
				CurrentSessionCardAdapter.startChronometer();

			}
			else
			{
				if(sessionData.currentSession().isOnPause()){
					currentSession = sessionData.currentSession();
					sessionData.resumeSession(currentSession);
					activity.setTitle(currentSession.getName());
					CurrentSessionCardAdapter.startChronometer();

				}
				else{
					//pausa

					sessionData.setSessionOnPause(sessionData.currentSession());
					//	ForegroundService.storeDuration(sessionData);
					activity.stopService(serviceIntent); 
					cardAdapter.pauseChronometer();
				}
			}
		}
	}


	//METODO CHIAMATO DAL TASTO STOP NELLA PRIMA CARD
	@Override
	public void stopService(View v) {

		ForegroundService.killSessionOnDestroy();
		cardAdapter.stopChronometer();
		cardAdapter.clearFalls();
		String closedSessionName = null;
		if(sessionData.existCurrentSession())
			closedSessionName = sessionData.currentSession().getName();
		if(serviceIntent!=null)
			activity.stopService(serviceIntent);//altro metodo con stesso nome ma di Activity che semplicemente stoppa il service
		serviceIntent = null;
		currentSession = null;
		sessionName = sessionNameDefault;
		activity.setTitle(sessionNameDefault);
		cardAdapter.clearGraphs();

		if(closedSessionName != null){
			Intent toPiero = new Intent(activity, SessionDetailsFragment.class);
			toPiero.putExtra(Utility.SESSION_NAME_KEY, closedSessionName); 
			((FragmentCommunicator)activity).switchFragment(toPiero); 
		}
	}


	public SessionDataSource.Session addSession(String name, String pic, long timeStart, long timeEnd) {

		SessionDataSource.Session session = null;
		if(!sessionData.existSession(name)){

			try{
				sessionData.openNewSession(name, pic, timeStart, timeEnd);
			}
			catch(SQLiteConstraintException e){
				e.printStackTrace();
			} catch (fallDetectorException.BoolNotBoolException e) {
				e.printStackTrace();
			} catch (MoreThanOneOpenSessionException e) {
				e.printStackTrace();
			} catch (DublicateNameSessionException e) {
				e.printStackTrace();
			}

		}
		else
		{
			Toast.makeText(activity, "Can't add session with same name", Toast.LENGTH_LONG).show();
			if(ForegroundService.isRunning())
				activity.stopService(serviceIntent);
			cardAdapter.stopChronometer();
		}

		return session;
	}



	public CurrentSessionCardAdapter getAdapter()
	{
		return cardAdapter;
	}

	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//azioni da compiere quando avviene la rotazione (---ATTENZIONE---) non togliere questo metodo anche se vuoto.
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
	public void serviceUpdate(String fallPosition, String link,
			String timeLiteral, long time, boolean b, String sessionName) {
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
		sessionName = s;
	}

	@Override
	public boolean equalsClass(ServiceReceiver obj) {
		
		if(obj instanceof CurrentSessionFragment)
			return true;
		return false;
	}

	
	

}
