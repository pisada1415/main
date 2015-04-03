package pisada.fallDetector;


import pisada.database.AcquisitionDataSource;
import pisada.database.BoolNotBoolException;
import pisada.database.FallSqlHelper;
import pisada.database.SessionDataSource;
import pisada.recycler.CurrentSessionCardAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
/*
 * 
 * connect dovrà anche inizializzare il timer che manda indietro qui il tempo passato da quando
 * è stata inizializzata la session.
 *  
 * sistemare le fall che devono inizializzare schede se avvengono 
 * 
 * salvare la session quando viene premuto stop (per ora, poi ci sarà limite di tempo interno al service).
 * 
 *  GETTO LA DATA E ORA PARTENZA SESSION DAL DB, INIZIALIZZO TIMER NELLA UI CON QUEL VALORE, PLAY DEL CRONOMETRO QUANDO APRO ACTIVITY
 * 
 * NOME SESSION NELLA ACTIONBAR MODIFICABILE CON IL TASTO OPZIONI OVERFLOW
 */

public class CurrentSessionActivity extends ActionBarActivity{

	private static Intent serviceIntent;
	RecyclerView rView;
	CurrentSessionCardAdapter cardAdapter;
	Session currentSession; //ooOOOOooOooOOOOH!
	private static SessionDataSource sessionData;
	private static AcquisitionDataSource acquisitionData;
	LayoutManager mLayoutManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_session);
		

		//APRO CONNESSIONI AL DATABASE
				sessionData=new SessionDataSource(this);
				sessionData.open();
				acquisitionData=new AcquisitionDataSource(this);
				acquisitionData.open();

				
		//INIZIALIZZO RECYCLERVIEW
		long timeSessionUp = 0;
		if(sessionData.existCurrentSession())
			timeSessionUp = sessionData.time();
		rView=(RecyclerView) findViewById(R.id.currentsession_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter=new CurrentSessionCardAdapter(this, timeSessionUp);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);
		setTitle("Current session");
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
		acquisitionData.close();
		ForegroundService.disconnect(); //disconnette l'activity connessa
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(!ForegroundService.isConnected())
			ForegroundService.connect(cardAdapter);
		sessionData.open();
		acquisitionData.open();
	}

	public void playPauseService(View v){
		
		if(!ForegroundService.isRunning()){
			//play
			serviceIntent = new Intent(this, ForegroundService.class);
			String activeServ = Utility.checkLocationServices(this, true);
			serviceIntent.putExtra("activeServices", activeServ);
			startService(serviceIntent);
			long time = System.currentTimeMillis();
			String nomeDefault = "session"+ time;
			
			ForegroundService.initTime(System.currentTimeMillis());
			
			cardAdapter.startChronometer();
			
			if(sessionData.existCurrentSession() && currentSession == null)
			{
				currentSession = sessionData.currentSession();
				
			}
			if(currentSession == null)
			{
				try {
					currentSession = addSession(nomeDefault, "" + time, time, 0);
				} catch (BoolNotBoolException e) {
					
					e.printStackTrace();
				}
				setTitle(nomeDefault);
			}
			// if(/*vedere nel db se current session è CHIUSA*/){/*qui mettere la roba che inizializza dati nuova sess nel db*/}
			/*
			 * QUI VIENE INIZIALIZZATA LA SESSION. DATI TRA CUI
			 * ORA INIZIO
			 * MINIATURA
			 * NOME SESSION
			 * VENGONO INIZIALIZZATI QUI
			 */
		}
		else
		{
			//pausa
			stopService(serviceIntent);
			ForegroundService.storeDuration();
			cardAdapter.pauseChronometer();
		}
	}



	public void stopService(View v) {
		
		stopService(serviceIntent);
		serviceIntent = null;
		cardAdapter.clearGraphs();
		cardAdapter.stopChronometer();
		if(ForegroundService.isRunning())
			ForegroundService.storeDuration();
		closeSession(currentSession);
		currentSession = null;
		//aggiungere ora fine a session nel db
		//CHIUDERE current session nel db
		
		/*
		 *  * SESSION VIENE TERMINATA QUI. 
		 */
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
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
		            Editable value = input.getText(); 
		            setTitle(value);
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
	
	
	public Session addSession(String name, String pic, long timeStart, long timeEnd) throws BoolNotBoolException{

		Session session = null;
		if(!sessionData.existSession(name)){

			session=new Session(name, pic,timeStart,timeEnd,FallSqlHelper.OPEN,null);
			try{
				sessionData.insert(session);
			}
			catch(SQLiteConstraintException e){
				e.printStackTrace();
			}

		}
		
		/*
		 * TODO else... blahblah
		 */
		
		return session;
	}
	
	public void closeSession(Session s){
		sessionData.closeSession(s);
	}
	
	




}
