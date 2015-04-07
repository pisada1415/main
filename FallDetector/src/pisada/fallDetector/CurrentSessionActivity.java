package pisada.fallDetector;

/*
 * problemi:
 * tempo storato nel cronometro raddoppia ogni volta che esci e entri
 * update: macelli vari col cronometro
 */
import fallDetectorException.DublicateNameSessionException;
import fallDetectorException.MoreThanOneOpenSessionException;
import pisada.database.AcquisitionDataSource;
import pisada.database.FallSqlHelper;
import pisada.database.SessionDataSource;
import pisada.recycler.CurrentSessionCardAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

public class CurrentSessionActivity extends ActionBarActivity{

	private static Intent serviceIntent;
	RecyclerView rView;
	CurrentSessionCardAdapter cardAdapter;
	SessionDataSource.Session currentSession; //ooOOOOooOooOOOOH!
	private static SessionDataSource sessionData;
	private static AcquisitionDataSource acquisitionData;
	LayoutManager mLayoutManager;
	String sessionName;
	String sessionNameDefault;
	private boolean startChronometerOnStartActivity = false;
	private long pauseTime = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_session);

		serviceIntent = new Intent(this, ForegroundService.class);
		sessionNameDefault = getResources().getString(R.string.defaultSessionName);
		sessionName = sessionNameDefault;
		//APRO CONNESSIONI AL DATABASE

		sessionData=new SessionDataSource(this);
	//	sessionData.open();
		acquisitionData=new AcquisitionDataSource(this);
	//	acquisitionData.open();


		//long timeSessionUp = 0;

		if(sessionData.existCurrentSession()){
			//timeSessionUp = sessionData.sessionDuration(sessionData.currentSession());
			sessionName = sessionData.currentSession().getName();
			startChronometerOnStartActivity = true;
			/*
			 * TODO QUI SE è IN PAUSA BISOGNA CHE IL SERVICE NON PARTA QUINDI QUESTE 6 RIGHE SOTTO VANNO MESSE SOLO SE NON è IN PAUSA e il service non sta già andando.
			 * STESSO TIPO DI CONTROLLO SI FA PER SETTARE IL TASTO IN STATO DI PAUSA O DI PLAY
			 * 
			 */
			if(!sessionData.currentSession().isOnPause() && !ForegroundService.isRunning()){
				serviceIntent = new Intent(this, ForegroundService.class);
				String activeServ = Utility.checkLocationServices(this, true);
				serviceIntent.putExtra("activeServices", activeServ);
				startService(serviceIntent);
			}
			else if(sessionData.currentSession().isOnPause())
			{
				pauseTime = sessionData.sessionDuration(sessionData.currentSession());
				setTitle(sessionData.currentSession().getName());
			}


		}

		rView=(RecyclerView) findViewById(R.id.currentsession_list_recycler);
		rView.setHasFixedSize(true);
		//ForegroundService.setSessionTime(sessionData.existCurrentSession() ? System.currentTimeMillis() - sessionData.sessionDuration(sessionData.currentSession()) : System.currentTimeMillis());
		/*
		 * se il service sta andando chiedo al metodo del service: ok
		 */
		
		cardAdapter = new CurrentSessionCardAdapter(this, ForegroundService.getSessionDuration(sessionData), startChronometerOnStartActivity, pauseTime);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);
		setTitle(sessionName);


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
	//	sessionData.open();
	//	acquisitionData.open();
	}

	public void playPauseService(View v){

		long time = System.currentTimeMillis();
		if(sessionName.equals(sessionNameDefault)) //cioè non è stato cambiato
			sessionName = "Session"+ time; //assegno nome default (altrimenti tengo quello cambiato

		
		if(!ForegroundService.isRunning()){
			//il service non sta andando
			if(!sessionData.existCurrentSession()){
				//non esiste sessione corrente: creane una nuova
				currentSession = null;
				currentSession = addSession(sessionName, "" + time, time, 0);
				setTitle(sessionName);
				cardAdapter.startChronometer();
				
				serviceIntent = new Intent(this, ForegroundService.class);
				String activeServ = Utility.checkLocationServices(this, true);
				serviceIntent.putExtra("activeServices", activeServ);
				startService(serviceIntent);
			}
			else
			{
				if(sessionData.currentSession().isOnPause()){
					currentSession = sessionData.currentSession();
					sessionData.resumeSession(currentSession);
					setTitle(currentSession.getName());
					cardAdapter.startChronometer();
					
					serviceIntent = new Intent(this, ForegroundService.class);
					String activeServ = Utility.checkLocationServices(this, true);
					serviceIntent.putExtra("activeServices", activeServ);
					startService(serviceIntent);
				}
				else{
					//pausa
					
					sessionData.setSessionOnPause(sessionData.currentSession());
					//	ForegroundService.storeDuration(sessionData);
					stopService(serviceIntent); //dovrebbe essere inutile
					cardAdapter.pauseChronometer();
				}
			}
			
		}
		else
		{
			//il service sta già andando
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


	public void stopService(View v) {
		cardAdapter.stopChronometer();
		
		if(sessionData.existCurrentSession())
			closeSession(sessionData.currentSession());
		if(serviceIntent!=null)
			stopService(serviceIntent);


		serviceIntent = null;
		currentSession = null;
		sessionName = sessionNameDefault;
		setTitle(sessionNameDefault);
		cardAdapter.clearGraphs();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MoreThanOneOpenSessionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DublicateNameSessionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else
		{
			Toast.makeText(this, "Can't add session with same name!aiusdiuashdiusa", Toast.LENGTH_LONG).show();
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
}
