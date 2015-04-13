package pisada.fallDetector;


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

/*
 *
 * 
 * 
 * TODO:
 * 
 * 
 * TEMPO LIMITE POI LA SESSION SI CHIUDE DA SOLA.
 * 
 */


public class CurrentSessionActivity extends ActionBarActivity{

	private static Intent serviceIntent;
	RecyclerView rView;
	private static CurrentSessionCardAdapter cardAdapter;
	SessionDataSource.Session currentSession; //ooOOOOooOooOOOOH!
	private static SessionDataSource sessionData;
	LayoutManager mLayoutManager;
	String sessionName;
	String sessionNameDefault;
	private boolean startChronometerOnStartActivity = false;
	private long pauseTime = 0;
	private FallDataSource fallDataSource;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_session);

		serviceIntent = new Intent(this, ForegroundService.class);
		sessionNameDefault = getResources().getString(R.string.defaultSessionName);
		sessionName = sessionNameDefault;
		
		
		
		
		
				
				
				
				
		//INIZIALIZZO DATABASE

		sessionData=new SessionDataSource(this);
	
		if(sessionData.existCurrentSession()){
			SessionDataSource.Session currentSession = sessionData.currentSession();
			sessionName = currentSession.getName();
			
			
			//TODO CARICARE LE CADUTE RELATIVE ALLA CURRENTSESSION dal database NELL'ADAPTER.
			
			
			if(!currentSession.isOnPause())
				startChronometerOnStartActivity = true; //FA SI CHE PARTA IL CRONOMETRO AL LANCIO DELL'ACTIVITY
			
			
			//SE CURRENTSESSION NON � IN PAUSA E NON C'� IL SERVICE ATTIVO... FAI PARTIRE IL SERVICE (C'� STATA UNA CHIUSURA INASPETTATA)
			if(!currentSession.isOnPause() && !ForegroundService.isRunning()){
				serviceIntent = new Intent(this, ForegroundService.class);
				String activeServ = Utility.checkLocationServices(this, true);
				serviceIntent.putExtra("activeServices", activeServ);
				startService(serviceIntent);
				pauseTime = 0;
			}
			

			else if(sessionData.currentSession().isOnPause()) //SE INVECE LA CURRENT SESSION � IN PAUSA... 
			{
				//INIZIALIZZO IL TEMPO DA CUI IL CRONOMETRO DEVE RIPARTIRE
				pauseTime = sessionData.sessionDuration(currentSession);
				
				
				//TODO SETTARE ICONA ADATTA NELL'ADAPTER
			}

		}

		
		//INIZIALIZZO LA RECYCLERVIEW
		rView=(RecyclerView) findViewById(R.id.currentsession_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter = new CurrentSessionCardAdapter(this, ForegroundService.getSessionDuration(sessionData), startChronometerOnStartActivity, pauseTime);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);
		setTitle(sessionName);
		
		
		if(sessionData.existCurrentSession() && sessionData.currentSession().isOnPause()) //SE INVECE LA CURRENT SESSION � IN PAUSA... 
		{
			if(fallDataSource == null)
				fallDataSource = new FallDataSource(CurrentSessionActivity.this);
			ArrayList<FallDataSource.Fall> cadute = fallDataSource.sessionFalls(sessionData.currentSession());
			for(FallDataSource.Fall f : cadute){
				cardAdapter.addFall(f, currentSession);
			}
		}


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.current_session, menu);
		return true;
	}



	@Override
	public void onPause()
	{
		super.onPause();
		sessionData.close();
		ForegroundService.disconnect(cardAdapter); //disconnette l'activity connessa
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(!ForegroundService.isConnected(cardAdapter))
			ForegroundService.connect(cardAdapter);
		sessionData.open();
	}

	//CHIAMATO QUANDO VIENE PREMUTO IL TASTO SOPRA (PLAY/PAUSA)
	public void playPauseService(View v){

		long time = System.currentTimeMillis(); //MEMORIZZA IL MOMENTO IN CUI � STATO PREMUTO IL TASTO
		if(sessionName.equals(sessionNameDefault)) //cio� non � stato cambiato
			sessionName = "Session"+ time; //assegno nome default UNICO (altrimenti tengo quello cambiato)

		
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
					//� IN PAUSA
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
			//il service sta gi� andando (STESSA COSA DI PRIMA MA QUI NON FA PARTIRE IL SERVICE)
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
		
		if(sessionData.existCurrentSession())
			closeSession(sessionData.currentSession());
		if(serviceIntent!=null)
			stopService(serviceIntent);//altro metodo con stesso nome ma di Activity che semplicemente stoppa il service


		serviceIntent = null;
		currentSession = null;
		sessionName = sessionNameDefault;
		setTitle(sessionNameDefault);
		cardAdapter.clearGraphs();
		/*
		 *  * SESSION VIENE TERMINATA QUI. (dal service in ondestroy) 
		 */
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		

		int id = item.getItemId();
		if (id == R.id.rename_session) {
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
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	public void closeSession(SessionDataSource.Session s){
		if(sessionData.existCurrentSession())
			sessionData.closeSession(s);
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
}
