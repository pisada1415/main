package pisada.fallDetector;

/*
 * questa è la mainactivity. Vorrei farla in stile Google FIT (dateci un'occhiata) così rispettiamo
 * le richieste di mettere lo start stop (bottone in alto iniziale) nella stessa schermata
 * della lista di sessions..
 */
//Samuele gay
//
//Canaglia
import java.util.ArrayList;

import pisada.database.AcquisitionDataSource;
import pisada.database.FallSqlHelper;
import pisada.database.SessionDataSource;
import pisada.recycler.SessionListCardAdapter;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import pisada.database.BoolNotBoolException;





public class SessionsListActivity extends ActionBarActivity implements SensorEventListener {

	private RecyclerView rView;
	private static SessionListCardAdapter cardAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	public static int counter;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private static SessionDataSource sessionData;
	private static AcquisitionDataSource acquisitionData;
	//private String sessionName="prima";




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sessions_list);


		//APRO CONNESSIONI AL DATABASE
		sessionData=new SessionDataSource(this);
		sessionData.open();
		acquisitionData=new AcquisitionDataSource(this);
		acquisitionData.open();


		//INIZIALIZZO RECYCLERVIEW

		rView=(RecyclerView) findViewById(R.id.session_list_recycler);
		rView.setHasFixedSize(true);
		cardAdapter=new SessionListCardAdapter(this, rView);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);
		rView.setItemAnimator(new DefaultItemAnimator());

		//INIZIALIZZO SENSORE E MANAGER
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);





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
		/*if(add){
			try{
				lastTime=currentTime;
				acquisitionData.insert(currentTime, xValue, yValue, zValue, sessionName,bool );
				Acquisition a=acquisitionData.getAcquisition(currentTime, sessionName);
				cardAdapter.addItem(a);
				add=false;
			}
			catch(Exception e){
				e.printStackTrace();

			}
		}*/
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
	public void addSession(View v) throws BoolNotBoolException{
		EditText editName=(EditText) findViewById(R.id.type_session);
		String name=editName.getText().toString();

		if(!sessionData.existSession(name)){
			Session session=new Session(name,"NONE",System.currentTimeMillis(),0,FallSqlHelper.OPEN,null);
			try{
				sessionData.insert(session);
				cardAdapter.addNewSession(session);
			}
			catch(SQLiteConstraintException e){
				e.printStackTrace();
			}


		}



	}
	public void closeCurrentSession(View v){
	 cardAdapter.closeCurrentSession();
	}


}



