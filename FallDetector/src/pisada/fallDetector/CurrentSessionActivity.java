package pisada.fallDetector;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.recycler.CurrentSessionCardAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import fallDetectorException.DublicateNameSessionException;
import fallDetectorException.MoreThanOneOpenSessionException;




public class CurrentSessionActivity extends ActionBarActivity implements ServiceReceiver{

	private static Intent serviceIntent;
	private static SessionDataSource sessionData;
	private static CurrentSessionCardAdapter cardAdapter;

	private RecyclerView rView;
	private LayoutManager mLayoutManager;
	private String sessionName, sessionNameDefault;
	private boolean startChronometerOnStartActivity = false;
	private long pauseTime = 0;

	private FallDataSource fallDataSource;
	private SessionDataSource.Session currentSession; 
	private RecyclerView recycler;
	
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_session);

		serviceIntent = new Intent(this, ForegroundService.class);
		sessionNameDefault = getResources().getString(R.string.defaultSessionName);
		sessionName = sessionNameDefault;
		recycler = (RecyclerView) findViewById(R.id.currentsession_list_recycler);

		//INIZIALIZZO DATABASE

		sessionData=new SessionDataSource(this);

		if(sessionData.existCurrentSession()){
			SessionDataSource.Session currentSession = sessionData.currentSession();
			sessionName = currentSession.getName();


			if(!currentSession.isOnPause())
				startChronometerOnStartActivity = true; //FA SI CHE PARTA IL CRONOMETRO AL LANCIO DELL'ACTIVITY


			//SE CURRENTSESSION NON è IN PAUSA E NON C'è IL SERVICE ATTIVO... FAI PARTIRE IL SERVICE (C'è STATA UNA CHIUSURA INASPETTATA)
			if(!currentSession.isOnPause() && !ForegroundService.isRunning()){
				serviceIntent = new Intent(this, ForegroundService.class);
				String activeServ = Utility.checkLocationServices(this, true);
				serviceIntent.putExtra("activeServices", activeServ);
				startService(serviceIntent);
				pauseTime = 0;
			}


			else if(sessionData.currentSession().isOnPause()) //SE INVECE LA CURRENT SESSION è IN PAUSA... 
			{
				//INIZIALIZZO IL TEMPO DA CUI IL CRONOMETRO DEVE RIPARTIRE
				pauseTime = sessionData.sessionDuration(currentSession);


				//TODO SETTARE ICONA ADATTA NELL'ADAPTER
			}

		}

		String title = getIntent().getStringExtra("title");
		if(title != null && !title.equals(""))
			sessionName = title;

		//INIZIALIZZO LA RECYCLERVIEW
		rView=(RecyclerView) findViewById(R.id.currentsession_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter = new CurrentSessionCardAdapter(this, ForegroundService.getSessionDuration(sessionData), startChronometerOnStartActivity, pauseTime);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);
		setTitle(sessionName);


		if(sessionData.existCurrentSession()) //SE INVECE LA CURRENT SESSION è IN PAUSA... 
		{
			if(fallDataSource == null)
				fallDataSource = new FallDataSource(CurrentSessionActivity.this);
			ArrayList<FallDataSource.Fall> cadute = fallDataSource.sessionFalls(sessionData.currentSession());
			if(cadute != null) //OCCHIO POTREBBE NASCONDERE PROBLEMI
				for(int i = cadute.size()-1; i >= 0; i--){
					cardAdapter.addFall(cadute.get(i), currentSession/*TODO prendi valore true false da caduta*/);
				}
		}

		if(!ForegroundService.isConnected(this)){
			ForegroundService.connect(this); //TODO
		}

		actionBar = getSupportActionBar();
		//actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//aggiunge elementi all'action bar se presenti
		getMenuInflater().inflate(R.menu.current_session, menu);
		return true;
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

			Toast.makeText(this, errors.toString(), Toast.LENGTH_LONG).show();
		}
	}

	//CHIAMATO QUANDO VIENE PREMUTO IL TASTO SOPRA (PLAY/PAUSA)
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
				setTitle(sessionName);
				cardAdapter.startChronometer();
				//FA PARTIRE IL SERVICE
				serviceIntent = new Intent(this, ForegroundService.class);
				String activeServ = Utility.checkLocationServices(this, true);
				serviceIntent.putExtra("activeServices", activeServ);
				startService(serviceIntent);
			}
			else
			{
				//ESISTE SESSIONE CORRENTE
				if(sessionData.currentSession().isOnPause()){
					//è IN PAUSA
					currentSession = sessionData.currentSession();
					sessionData.resumeSession(currentSession); //LA FACCIO RIPARTIRE
					cardAdapter.startChronometer();
					//FA PARTIRE IL SERVICE
					serviceIntent = new Intent(this, ForegroundService.class);
					String activeServ = Utility.checkLocationServices(this, true);
					serviceIntent.putExtra("activeServices", activeServ);
					startService(serviceIntent);
				}
				else{
					//STA ANDANDO, QUINDI VA MESSA IN PAUSA
					sessionData.setSessionOnPause(sessionData.currentSession());
					stopService(serviceIntent); //dovrebbe essere inutile
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
				setTitle(sessionName);
				cardAdapter.startChronometer();

			}
			else
			{
				if(sessionData.currentSession().isOnPause()){
					currentSession = sessionData.currentSession();
					sessionData.resumeSession(currentSession);
					setTitle(currentSession.getName());
					cardAdapter.startChronometer();

				}
				else{
					//pausa

					sessionData.setSessionOnPause(sessionData.currentSession());
					//	ForegroundService.storeDuration(sessionData);
					stopService(serviceIntent); 
					cardAdapter.pauseChronometer();
				}
			}
		}
	}

	//METODO CHIAMATO DAL TASTO STOP NELLA PRIMA CARD
	public void stopService(View v) {
		cardAdapter.stopChronometer();
		cardAdapter.clearFalls();
		String closedSessionName = null;
		if(sessionData.existCurrentSession()){
			closedSessionName = sessionData.currentSession().getName();
			sessionData.closeSession(sessionData.currentSession());
		}
		if(serviceIntent!=null)
			stopService(serviceIntent);//altro metodo con stesso nome ma di Activity che semplicemente stoppa il service


		serviceIntent = null;
		currentSession = null;
		sessionName = sessionNameDefault;
		setTitle(sessionNameDefault);
		cardAdapter.clearGraphs();
		
		if(closedSessionName != null){
			Intent toPiero = new Intent(this, SessionDetailsActivity.class);
			toPiero.putExtra("name", closedSessionName); // TODO nome da dire a piero per extras quando viene premuto stop
			toPiero.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(toPiero);
			//TODO rimpiazzare queste ultime 2 righe con finish
			ForegroundService.disconnect(this);
			ForegroundService.disconnect(cardAdapter);
			///this.finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		int id = item.getItemId();

		switch (id) {
		case android.R.id.home:
			// app icon in action bar clicked; goto parent activity.
			Intent toDaniel = new Intent(this, SessionsListActivity.class);
			toDaniel.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			startActivity(toDaniel);
		/*	ForegroundService.disconnect(this);
			ForegroundService.disconnect(cardAdapter);*/
			this.finish();
			return true;
		case R.id.rename_session:
		{
			// Set an EditText view to get user input 
			final EditText input = new EditText(this);

			new AlertDialog.Builder(CurrentSessionActivity.this)
			.setTitle("Rename")
			.setMessage("Insert name")
			.setView(input)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString(); 
					String tmp = sessionName;

					if(sessionData.existCurrentSession() && !sessionData.existSession(value)){
						sessionData.renameSession(sessionData.currentSession(), value);
						setTitle(value);
						sessionName = value;
					}
					else if(!sessionData.existSession(value)){
						sessionName = value; setTitle(value);
					}
					else
					{
						Toast.makeText(CurrentSessionActivity.this, "Can't add session with same name!", Toast.LENGTH_LONG).show();
						sessionName = tmp;
						setTitle(sessionName);


					}

				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			}).show();

		}
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
			Toast.makeText(this, "Can't add session with same name!!!!", Toast.LENGTH_LONG).show();
			if(ForegroundService.isRunning())
				stopService(serviceIntent);
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
		//Do stuff here

	}

	@Override
	public void sessionTimeOut() {
		if(serviceIntent != null)
			stopService(serviceIntent);

	}

	@Override
	public void serviceUpdate(float x, float y, float z, long time) {

	}

	@Override
	public void serviceUpdate(String fallPosition, String link, String time,
			boolean b) {
		// non serve qui
		recycler.scrollToPosition(recycler.getAdapter().getItemCount()-1);
	}
}
