package pisada.fallDetector;

import java.util.Calendar;

import pisada.database.BoolNotBoolException;
import pisada.database.FallSqlHelper;
import pisada.plotmaker.Data;
import pisada.plotmaker.Plot2d;
import pisada.recycler.CurrentSessionCardAdapter;
import pisada.recycler.SessionListCardAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
/*
 * impostare il layout con tastone grosso sopra ecc e nelle cardview ci vanno le cose varie.
 * 
 * connect dovrà anche inizializzare il timer che manda indietro qui il tempo passato da quando
 * è stato inizializzato il service.
 * 
 * inoltre la lista fall deve essere a sua volta riempita dal service quindi serve altro metodo
 * ancora che funziona come il mandadati dei grafici adesso.
 * 
 * 
 */

public class CurrentSessionActivity extends ActionBarActivity{

	private Plot2d plotx, ploty, plotz;
	private static Intent serviceIntent;
	private Calendar c;
	private int secondsStartGraph;
	RecyclerView rView;
	RecyclerView.Adapter<RecyclerView.ViewHolder> cardAdapter;
	LayoutManager mLayoutManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_session);
	/*	View view; 
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		view = inflater.inflate(R.layout.activity_current_session, null);
		LinearLayout rl = (LinearLayout) view.findViewById(R.id.graphcontainer);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 

		LinearLayout graphXLayout = (LinearLayout) rl.findViewById(R.id.graphx);
		LinearLayout graphYLayout = (LinearLayout) rl.findViewById(R.id.graphy);
		LinearLayout graphZLayout = (LinearLayout) rl.findViewById(R.id.graphz);

		c = Calendar.getInstance();

		secondsStartGraph = c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND);

		plotx = new Plot2d(this, new Data(c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND) - secondsStartGraph,0));
		ploty = new Plot2d(this, new Data(c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND) - secondsStartGraph,0));
		plotz = new Plot2d(this, new Data(c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND) - secondsStartGraph,0));

		graphXLayout.addView(plotx, lp);
		graphYLayout.addView(ploty, lp);
		graphZLayout.addView(plotz, lp);
		setContentView(rl);
*/
		





		//INIZIALIZZO RECYCLERVIEW

		rView=(RecyclerView) findViewById(R.id.currentsession_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter=new CurrentSessionCardAdapter(this);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);

		
		//BESTIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
		/*AOSDJOIAJDOIAJSMDOIASMDAOIMDOIAMDOIAJMODIAMDOIASJMDAOIM
				asdasd
				asd
				as
				das
				da
				sd
				asd
				as
				d
				a
				sdsa
				a*/

		//RIEMPIO RECYCLERVIEW CON TUTTE LE SESSIONI


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
		ForegroundService.disconnect();
	}

	@Override
	public void onResume()
	{
		super.onResume();
	//	ForegroundService.connect(this);
	}

	public void playPauseService(View v){
		//TODO

		//=======================BLOCCO DA SPOSTARE NEL TASTO START=======================
				if(serviceIntent == null){
				serviceIntent = new Intent(this, ForegroundService.class);
				String activeServ = Utility.checkLocationServices(this, true);
				serviceIntent.putExtra("activeServices", activeServ);
				startService(serviceIntent);
				
				}
				//====================BLOCCO DA SPOSTARE NEL TASTO START (FINE)===================
				else
				{
					
					serviceIntent = null;
				}
	}



	public void stopService(View v) {
		//TODO
		stopService(serviceIntent);

	}



}
