package pisada.fallDetector;

import java.util.Calendar;


import pisada.plotmaker.Plot2d;
import pisada.recycler.CurrentSessionCardAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
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
	LayoutManager mLayoutManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_session);
		

		//INIZIALIZZO RECYCLERVIEW

		rView=(RecyclerView) findViewById(R.id.currentsession_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter=new CurrentSessionCardAdapter(this);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);


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
		
		ForegroundService.disconnect(); //disconnette l'activity connessa
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(!ForegroundService.isConnected())
			ForegroundService.connect(cardAdapter);
	}

	public void playPauseService(View v){
		
		if(!ForegroundService.isRunning()){
			//play
			serviceIntent = new Intent(this, ForegroundService.class);
			String activeServ = Utility.checkLocationServices(this, true);
			serviceIntent.putExtra("activeServices", activeServ);
			startService(serviceIntent);
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
			

		}
	}



	public void stopService(View v) {
		//TODO
		stopService(serviceIntent);
		serviceIntent = null;
		cardAdapter.clearGraphs();
		//aggiungere ora fine a session nel db
		//CHIUDERE current session nel db
		
		/*
		 *  * SESSION VIENE TERMINATA QUI. 
		 */
	}



}
