package pisada.fallDetector;

/*
 * questa è la mainactivity. Vorrei farla in stile Google FIT (dateci un'occhiata) così rispettiamo
 * le richieste di mettere lo start stop (bottone in alto iniziale) nella stessa schermata
 * della lista di sessions..
 */
//Samuele gay

import java.util.ArrayList;
import java.util.List;

import pisada.database.AcquisitionDataSource;
import pisada.database.SessionDataSource;
import pisada.recycler.CardAdapter;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;





public class SessionsListActivity extends ActionBarActivity implements SensorEventListener {
	
	private RecyclerView rView;
	private CardAdapter cardAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private ArrayList<Acquisition> acquisitions=new ArrayList<Acquisition>();
	public static int counter;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private static SessionDataSource sessionData;
	private static AcquisitionDataSource acquisitionData;
	private boolean fall=false;
	private long lastTime=0;
	private long lastFall=0;
	private String sessionName="prima";



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sessions_list);
		
		//INIZIALIZZO RECYCLERVIEW
	
		
		rView=(RecyclerView) findViewById(R.id.my_recycler_view);
		rView.setHasFixedSize(true);
		cardAdapter=new CardAdapter(acquisitions, this);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);
	
		//APRO CONNESSIONI AL DATABASE
		sessionData=new SessionDataSource(this);
		sessionData.open();
		acquisitionData=new AcquisitionDataSource(this);
		acquisitionData.open();
		
		//PROVO A FARE INSERT DELL'UNICA SESSIONE
		try{sessionData.storeNewSession(sessionName, "noFoto",System.currentTimeMillis(), 0);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		//INIZIALIZZO SENSORE E MANAGER
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		//RIEMPIO RECYCLERVIEW CON TUTTE LE ACQUISIZIONI
		acquisitions=acquisitionData.acquisitions(sessionName);
		for(Acquisition a: acquisitions){
			cardAdapter.addItem(a);
		}
		
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
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sessionData.open();
		acquisitionData.open();
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
		
		
	}
	@Override
	protected void onPause() {

		super.onPause();
		mSensorManager.unregisterListener(this);
		sessionData.close();
		acquisitionData.close();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		long currentTime=System.currentTimeMillis();
	/*	TextView xText=(TextView) findViewById(R.id.xAxis);
		TextView yText=(TextView) findViewById(R.id.yAxis);
		TextView zText=(TextView) findViewById(R.id.zAxis);*/
		float xValue=event.values[0];
		float yValue=event.values[1];
		float zValue=event.values[2];
		float absG=(float) Math.sqrt(xValue*xValue+yValue*yValue+zValue*zValue);
		int bool=0;
		if(absG>30)bool=1;
		//STORE NEL DATABASE
		if((currentTime-lastTime)/1000>1){
			try{
				lastTime=currentTime;
				acquisitionData.insert(currentTime, xValue, yValue, zValue, sessionName,bool );
				Acquisition a=acquisitionData.getAcquisition(currentTime, sessionName);
				cardAdapter.addItem(a);
			}
			catch(Exception e){
				e.printStackTrace();

			}
		}
	
		//SCRiVE NELLE TEXT VIEW 
	/*	if((currentTime-lastFall)/1000>4) fall=false;
		if(!fall){
			if(absG<30){
				xText.setText(Float.toString(xValue));
				yText.setText(Float.toString(yValue));
				zText.setText(Float.toString(zValue));
			}
			else{
				xText.setText("FAAAAALL! reached over "+(int)absG+" qualcosa");
				yText.setText("FAAAAALL! reached over "+(int)absG+" qualcosa");
				zText.setText("FAAAAALL! reached over "+(int)absG+" qualcosa");
				fall=true;
				lastFall=currentTime;
			}
		}*/
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}



