package pisada.fallDetector;
/*
 * PROBLEMA DATABASE LOCKED: PIù THREAD CHE ACCEDONO A STESSO OGGETTO: USA SYNCHRONIZED
 * OPPURE: PIù HEPER NELLO STESSO FILE.
 * 
 * -->oggetti statici per il database, usati da tutti, sempre e solo con metodi synchronized
 */


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.recycler.CurrentSessionCardAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/*
 * 
 * utilizzo del service:
 * creare un nuovo intent e avviare il service tramite il comando startService(intent)
 * se si vuole ricevere update per cadute e acquisizioni:
 * connettere l'activity al service tramite il metodo ForegroundService.connect(ActivityDaConnettere)
 * e implementare l'interfaccia ServiceReceiver. Quando hai finito di ricevere dati
 * chiama: ForegroundService.disconnect(ActivityDaConnettere)
 * 
 * 
 * funzionamento del gps:
 * la location viene aggiornata ogni 5 minuti richiedendo la posizione ai provider gps e network.
 * le coordinate vengono poi passate al metodo dell'activity "connessa" quando avviene una caduta
 * 
 *
 *
 *TODO: 
 *prendere lista cadute e caricarla nella sessione quando apri ed era in pausa
 */

public class ForegroundService extends Service implements SensorEventListener {

	private final String GPSProvider = LocationManager.GPS_PROVIDER;
	private final String networkProvider = LocationManager.NETWORK_PROVIDER;

	private boolean stop = false; 
	private boolean running = false;
	private boolean updatesRemoved = false;
	private static boolean connected = false;
	private static boolean isRunning = false;
	private static boolean timeInitialized = false;
	private static Acquisition lastInserted;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static ArrayList<ServiceReceiver> connectedActs;
	private LocationListener locationListenerGPS, locationListenerNetwork;
	private LocationManager lm;
	private Double latitude = null;
	private Double longitude = null;
	private Calendar c;
	private int counterGPSUpdate = 50; //per attivare subito la ricerca della posizione
	private Handler uiHandler;
	private Criteria criteria;
	private String bestProvider;
	private String activeService;
	private static long totalTime = 0;
	private static long startTime = 0;
	private NotificationManager nm;
	private long lastFall = System.currentTimeMillis() - 2000;
	private  SessionDataSource sessionDataSource;
	private  FallDataSource fallDataSource;
	private ExpiringList acquisitionList;
	private static String position, link;
	private final int TIME_BETWEEN_FALLS = 2000;
	private BackgroundTask bgrTask;
	protected static final int MAX_SENSOR_UPDATE_RATE = 10; //ogni quanti millisecondi update
	@Override
	public void onStart(Intent intent, int startId) {

	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		/*
		 * this method is called when another component (activity) requests the service
		 * to start.
		 * Service needs then to be stopped when the job is done (when the stop button is pressed
		 * ) by calling stopSelf() or stopService()
		 */

		// handleCommand(intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.


		//APRO CONNESSIONI AL DATABASE
		/*	acquisitionData.open();*/
		if(sessionDataSource == null){
			initSessionData();;
		}

		//questo fa si che totalTime tenga il tempo per cui la sessione è aperta in totale
		if(!timeInitialized && existsCurrentSession()){

			totalTime = sessionDuration(currentSession());
			timeInitialized = true;
			startTime = System.currentTimeMillis();
		}

		isRunning = true;
		uiHandler = new Handler();
		criteria = new Criteria();

		if(intent != null){
			activeService = intent.getStringExtra("activeServices");
		}


		//========================================================================


		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


		locationListenerGPS = new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				stopLocationUpdates();
				Toast.makeText(getApplicationContext(), "ricevuta posizione gps", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}





		};


		locationListenerNetwork = new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				stopLocationUpdates();
				Toast.makeText(getApplicationContext(), "ricevuta posizione network", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}



		};

		if(activeService != null && !updatesRemoved){ //chiamato sul thread UI


			bestProvider = lm.getBestProvider(criteria, true); 
			lm.requestLocationUpdates(bestProvider, 5000, 0/*50*/, locationListenerGPS); //if gps is available
			lm.requestLocationUpdates(networkProvider, 5000, 0/*50*/, locationListenerNetwork); //always updates location with network: it's faster

		}

		//=========================NOTIFICATION(START)============
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, CurrentSessionActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				717232, notificationIntent, 0);

		nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Resources res = context.getResources();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		builder.setContentIntent(contentIntent)
		.setSmallIcon(R.drawable.notificationicon)
		.setContentTitle(res.getString(R.string.detecting));
		Notification n = builder.build();

		nm.notify(717232, n);
		//=========================NOTIFICATION(END)==============


		if(running)
		{
			Toast.makeText(this, "Already running", Toast.LENGTH_LONG).show();
			return Service.START_STICKY;
		}
		else
		{
			startForeground(717232, n);

			Message msg = mServiceHandler.obtainMessage();
			msg.arg1 = startId;
			mServiceHandler.sendMessage(msg);
			stop = false;
			return START_STICKY;
		}
	}




	@Override
	public IBinder onBind(Intent intent) {
		/*
		 * here the communication between the service and another component is managed
		 * another component can call bindService() to connect to the service. here an IBinder must 
		 * be returned
		 */
		return null;
	}

	@Override
	public void onCreate() {

		/*
		 * this is meant to perform one-time setup procedures
		 * when the service is first created, before onStartCommand or onBind are called
		 * 
		 */


		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


		HandlerThread thread = new HandlerThread("",
				android.os.Process.THREAD_PRIORITY_FOREGROUND); //almost unkillable
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI/*, mServiceHandler*/);

		if(sessionDataSource == null){
			initSessionData();
		}

	}

	@Override
	public void onDestroy() {
		

		/*TODO deve mandare in pausa la session
		 * 
		 */
		if(sessionDataSource.existCurrentSession())
			storeDuration();
		resetTime();
		sessionDataSource.close();
		if(fallDataSource != null)
			fallDataSource.close();
		stop = true;
		mSensorManager.unregisterListener(this);
		stopLocationUpdates();
		isRunning = false;
		nm.cancel(717232);
	}

	protected void stopLocationUpdates() {
		lm.removeUpdates(locationListenerGPS);
		lm.removeUpdates(locationListenerNetwork);
		updatesRemoved = true;
	}

	protected static boolean isRunning(){
		return isRunning;
	}



	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			//hard job is done here
			if(!running)
				while (true) {

					running = true;
					/*
					 * the service keeps running as long as this statement is cycling 
					 * the check for the service to stop occurs every 5 seconds (to save battery)
					 */

					if(activeService == null){
						activeService = Utility.checkLocationServices(getApplicationContext(), false);		
					}
					if(activeService != null && counterGPSUpdate++ >= 50) //richiesto update posizione ogni 2.5 minuti per risparmiare batteria
					{
						counterGPSUpdate = 0;
						//lm.requestLocationUpdates(activeService, 2000, 10, locationListener);

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if(!updatesRemoved){
									lm.requestLocationUpdates(GPSProvider, 5000, 0/*50*/, locationListenerGPS);
									lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0/*50*/, locationListenerNetwork);
								}
							}
						});
					}



					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
					if(stop)
						break;

				}

			running = false;
			stopSelf(msg.arg1);
		}
	}


	long lastSensorChanged = System.currentTimeMillis();
	//boolean verifyingSensorData;

	/*
	 * BISOGNA COMUNQUE FERMARE LA LISTA MENTRE SI CONTROLLA E INVIA PER VERIFICA CADUTA ALTRIMENTI ARRIVA DIVERSA! 
	 * 
	 * */
	@SuppressLint("NewApi")
	@Override
	public synchronized void onSensorChanged(SensorEvent event) {
		/*if(!verifyingSensorData){
			verifyingSensorData = true;*/
		
		if(System.currentTimeMillis() - lastSensorChanged >= MAX_SENSOR_UPDATE_RATE){ //non più di un update ogni 10 millisecondi
			lastSensorChanged = System.currentTimeMillis();
			
			
			if(acquisitionList == null)
				acquisitionList = new ExpiringList();
			
			if(bgrTask == null)
				bgrTask = new BackgroundTask();
			if(acquisitionList.size()>=10){
				//initializeBGThread(lastInserted).start();
			
				if(bgrTask.getStatus()!= AsyncTask.Status.RUNNING){
					
					bgrTask.execute(acquisitionList);
				}
			}
			
			//update accettato nella prima riga, sveglio l'asynctask:
			boolean pause = bgrTask.getPause();
			System.out.println("pause is " + pause);
			if(pause)
				bgrTask.wakeUp();
			
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				float[] values = event.values;

				final float x = values[0];
				final float y = values[1];
				final float z = values[2];

				c = Calendar.getInstance();
				final long time = c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND);

				if(connectedActs != null && connectedActs.size() > 0){

					for(final ServiceReceiver sr : connectedActs){

						Runnable r = new Runnable(){@Override public void run() { if(sr != null) sr.serviceUpdate(x, y, z, time);}};

						if(sr instanceof CurrentSessionCardAdapter)
							((CurrentSessionCardAdapter)sr).runOnUiThread(r);
						else if(sr instanceof Activity)
							((Activity)sr).runOnUiThread(r);
					}
				}

				long timeNow = System.currentTimeMillis();
				
				if(lastInserted == null || timeNow > lastInserted.getTime()){
					lastInserted = new Acquisition(timeNow, x, y, z);
					acquisitionList.enqueue(lastInserted); //RIEMPIMENTO LISTA

				}
			}

			/*
			 * if(fall){
			 * Intent launchImOK = new Intent(getBaseContext(), ImOK.class);
			 * launchImOK.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * launchImOK.putExtras("latitude", latitude);
			 * launchImOK.putExtras("longitude", longitude); //se sono null va scritto location non disponibile
			 * getApplication().startActivity(launchImOK);
			 * (sarebbe bello fosse un dialog più che un'activity)
			 */

		}
	}






	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//not interesting for this purpose
	}

	public static void connect(ServiceReceiver connectedActivity)
	{
		if(connectedActs == null)
			connectedActs = new ArrayList<ServiceReceiver>();
		connectedActs.add(connectedActivity);

	}

	public static void disconnect(ServiceReceiver sr)
	{
		connectedActs.remove(sr);
	}

	public static boolean isConnected(ServiceReceiver sr)
	{
		for(ServiceReceiver s : connectedActs)
			if(s.equals(sr))
				return true;
		return false;
	}



	private void runOnUiThread(Runnable runnable) {
		if(uiHandler == null)
			uiHandler = new Handler();
		uiHandler.post(runnable);
	}


	
	private void resetTime()
	{
		timeInitialized = false;
	}


	private class BackgroundTask extends AsyncTask<ExpiringList, Void, String> {

		private String INTERRUPTOR = "tatanka";
		private boolean pause = true;
		
		
	    public void pauseMyTask() {
	    	System.out.println("messo in pausa qui e pausa messo true");
	      pause = true;
	    }
		
	    
	    public void wakeUp() {
	      synchronized (INTERRUPTOR){
		    System.out.println("comando notify lanciato");
	        INTERRUPTOR.notify();
	      }
	    }
	    
	    
	    public boolean getPause() {
	      return pause;
	    }
	    
		@SuppressLint("NewApi")
		@Override
		protected String doInBackground(ExpiringList... params) {




			ExpiringList list = params[0]; //VERIFICARE CHE NON SIA UNA COPIA



			while(true){

				if (pause) {
					synchronized (INTERRUPTOR) {
						try {

							// --- sleep tile wake-up method will be called --
							System.out.println("pausa qui");
							INTERRUPTOR.wait();
							System.out.println("ripartito qui");

						} catch (InterruptedException e) {e.printStackTrace();}
						pause = false;
						System.out.println("pause messo false");
					}
				}

				/*
				 * PROVARE SENZA WAIT QUI VEDERE COSA SUCCEDE QUANDO VA TUTTO VELOCE.
				 * IN TEORIA ANCHE CON LISTA CORTA DOVREBBERO ESSERE TUTTE ACQUISIZIONI NUOVE
				 */
				//WAITINGACQUISITION
				
				
				
				
				
				if(System.currentTimeMillis() - lastFall > TIME_BETWEEN_FALLS)
				{
					/*
					Acquisition lastInserted = acquisitionList.peek(); /////=list.peek()
					*/
					if(lastInserted != null){
						float objectX = lastInserted.getXaxis(); final float objectY = lastInserted.getYaxis(); final float objectZ = lastInserted.getZaxis();
						if(Math.sqrt(objectX*objectX + objectY*objectY + objectZ*objectZ) > 17){ //CONTROLLO PRIMO IMPULSO CADUTA PASSANDO SOLO VAL CENTRALE

							//SE PRIMA PARTE CADUTA CONFERMATA QUI PASSO IL RESTO COME COPIA. SE CONTINUA A ESSERE CADUTA, CONTINUIAMO (AGGIUNGERE IF)

							if(DetectorAlgorithm.danielAlgorithm(acquisitionList)){
								if(System.currentTimeMillis() - lastFall > TIME_BETWEEN_FALLS){
									lastFall = System.currentTimeMillis();
									////=====================ASPETTARE 0.5 SECONDI mentre continui a storare nella coda==========================================

									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									////=========================================================================================================================


									//================================PARTE GPS=====================================================

									Location locationGPS = lm.getLastKnownLocation(GPSProvider);
									Location locationNetwork = lm.getLastKnownLocation(networkProvider);

									if(locationNetwork != null || locationGPS != null){
										latitude = locationGPS != null ? locationGPS.getLatitude() : locationNetwork.getLatitude();
										longitude = locationGPS != null ? locationGPS.getLongitude() : locationNetwork.getLongitude();
									}
									//===========================FINE PARTE GPS=====================================================


									//=====================store nel database=================

									if(fallDataSource == null)
										fallDataSource = new FallDataSource(ForegroundService.this);


									fallDataSource.insertFall(sessionDataSource.currentSession(), acquisitionList.getQueue(), 1010,1010);



									
									acquisitionList = new ExpiringList(); //REINIZIALIZZO
									//==============================INVIO ALLE ACTIVITY CONNESSE I DATI=================================
									if(connectedActs != null && connectedActs.size() > 0){

										link = null;
										final long fallTime = System.currentTimeMillis();

										if(latitude != null && longitude != null){
											position = "" + latitude + ", " + longitude;
											link = Utility.getMapsLink(latitude, longitude);

										}
										else
											position = "Not available";

										

										final String formattedTime = Utility.getStringTime(fallTime);

										for(final ServiceReceiver sr : connectedActs){
											
											Runnable r = new Runnable(){@Override public void run() { sr.serviceUpdate(position, link, formattedTime, fallTime);}};

											if(sr instanceof CurrentSessionCardAdapter)
												((CurrentSessionCardAdapter)sr).runOnUiThread(r);
											else if(sr instanceof Activity)
												((Activity)sr).runOnUiThread(r);
										}


									}
									//==============================INVIO ALLE ACTIVITY CONNESSE I DATI (FINE)=================================
								}
							}
						}
					}
				}








				if(stop==true)
					break;

				//sempre e comunque, lo metto in sleep
				pauseMyTask();



			}






			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {

		}

		@Override
		protected void onPreExecute() {}

		@Override
		protected void onProgressUpdate(Void... values) {}
	}




	private static void databaseSaver(final FallDataSource fds, final SessionDataSource.Session s, final ConcurrentLinkedQueue<Acquisition> al, final double lat,final double lng)
	{
		new Thread(new Runnable(){
			@Override
			public void run(){
				fds.insertFall(s, al,lat,lng);
			}
		}).start();
	}


/*
	
	private void initFallData()
	{
		synchronized(ForegroundService.fallDataSource){
			fallDataSource = new FallDataSource(ForegroundService.this);
		}
	}
	private void openFallData()
	{
		synchronized(ForegroundService.fallDataSource){
			fallDataSource.open();
		}
	}
	private void closeFallData()
	{
		synchronized(ForegroundService.fallDataSource){
			fallDataSource.close();
		}
	}
	private void addFallToFallData(final FallDataSource fds, final SessionDataSource.Session s, final ArrayList<Acquisition> al)
	{//TODO
		synchronized(ForegroundService.fallDataSource){
			databaseSaver(fds,s, al);
		}
	}
	
	private void initSessionData()
	{
		synchronized(ForegroundService.sessionDataSource){
			sessionDataSource = new SessionDataSource(ForegroundService.this);
		}
	}
	private void openSessionData()
	{
		synchronized(ForegroundService.sessionDataSource){
			sessionDataSource.open();
		}
	}
	private void closeSessionData()
	{
		synchronized(ForegroundService.sessionDataSource){
			sessionDataSource.close();
		}
	}
	public static long getSessionDataSessionDuration(SessionDataSource db)
	{//TODO
		synchronized(ForegroundService.sessionDataSource){
			if(timeInitialized)
				return System.currentTimeMillis() - startTime + totalTime;
			else
			{

				if(db.existCurrentSession())
					return db.sessionDuration(db.currentSession());
				else
					return 0;
			}
		}
	}
	
	private void storeDuration()
	{
		synchronized(ForegroundService.sessionDataSource){
		if(sessionDataSource.existCurrentSession())
			sessionDataSource.updateSessionDuration(sessionDataSource.currentSession(), System.currentTimeMillis() - startTime);
		//totalTime = System.currentTimeMillis();
		}
	}
	
	private boolean existsCurrentSession()
	{
		synchronized(ForegroundService.sessionDataSource)
		{
			return sessionDataSource.existCurrentSession();
		}
	}
	
	private long sessionDuration(SessionDataSource.Session s){
		synchronized(ForegroundService.sessionDataSource){
			return sessionDataSource.sessionDuration(s);
		}
	}
	
	private SessionDataSource.Session currentSession()
	{
		synchronized(ForegroundService.sessionDataSource){
			return sessionDataSource.currentSession();
		}
	}
	
	/*
	 * CONTINUARE A SCAMBIARE LE CHIAMATE AI METODI DI SESSIONDATASOURCE CON QUELLI SYNC
	 */
	
	
	public static long getSessionDuration(SessionDataSource db)
	{//TODO
			if(timeInitialized)
				return System.currentTimeMillis() - startTime + totalTime;
			else
			{

				if(db.existCurrentSession())
					return db.sessionDuration(db.currentSession());
				else
					return 0;
			}
		
	}
	
	
	
	private void initSessionData()
	{
			sessionDataSource = new SessionDataSource(ForegroundService.this);
		
	}
	
	private void storeDuration()
	{
		
		if(sessionDataSource.existCurrentSession())
			sessionDataSource.updateSessionDuration(sessionDataSource.currentSession(), System.currentTimeMillis() - startTime);
		//totalTime = System.currentTimeMillis();
		
	}
	
	private boolean existsCurrentSession()
	{
		
			return sessionDataSource.existCurrentSession();
		
	}
	
	private long sessionDuration(SessionDataSource.Session s){
			return sessionDataSource.sessionDuration(s);
		
	}
	
	private SessionDataSource.Session currentSession()
	{
			return sessionDataSource.currentSession();
		
	}
	

}
