package pisada.fallDetector;
/*
 * PROBLEMA DATABASE LOCKED: PIù THREAD CHE ACCEDONO A STESSO OGGETTO: USA SYNCHRONIZED
 * OPPURE: PIù HEPER NELLO STESSO FILE.
 * 
 * -->oggetti statici per il database, usati da tutti, sempre e solo con metodi synchronized
 */


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import pisada.database.Acquisition;
import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.fallDetector.smSender.SMSender;
import pisada.recycler.CurrentSessionCardAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
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
 * MAX_SENSOR_UPDATE_RATE permette di risparmiare su calcoli computazionali, memoria e batteria
 * 
 * funzionamento del gps:
 * la location viene aggiornata ogni 5 minuti richiedendo la posizione ai provider gps e network.
 * le coordinate vengono poi passate al metodo dell'activity "connessa" quando avviene una caduta
 * 
 *
 *
 *TODO: 
 *MANDARE MESSAGGIO A CONTATTI PRESTABILITI QUANDO AVVIENE CADUTA
 */

public class ForegroundService extends Service implements SensorEventListener {

	protected static int MAX_SENSOR_UPDATE_RATE = 10; //ogni quanti millisecondi update
	private final int TIME_BETWEEN_FALLS = 2000, CYCLES_FOR_LOCATION_REQUESTS = 50, SERVICE_SLEEP_TIME = 5000, MIN_TIME_LOCATION_UPDATES = 5000, MIN_DISTANCE_LOCATION_UPDATES = 500; 
	private long TIMEOUT_SESSION = 86400000; //24h
	private final static String CONTACTS_KEY = "contacts";
	
	private final String GPSProvider = LocationManager.GPS_PROVIDER;
	private final String networkProvider = LocationManager.NETWORK_PROVIDER;


	private boolean stop = false, running = false, locationUpdatesRemoved = false; 
	private static boolean isRunning = false, timeInitialized = false; //riguarda il tempo per sapere da quanto è aperta la session;
	private static String position, link;
	private static long totalTime = 0, startTime = 0;
	public static ArrayList<ServiceReceiver> connectedActs;

	private int counterGPSUpdate = 50; //per attivare subito la ricerca della posizione
	private String bestProvider;
	private String activeService;
	private long lastFallTime = System.currentTimeMillis() - 2000, lastSensorChanged = System.currentTimeMillis();
	private Double latitude = -1d, longitude = -1d;

	private Acquisition lastInserted;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private LocationListener locationListenerGPS, locationListenerNetwork;
	private LocationManager lm;
	private Calendar c;
	private Handler uiHandler;
	private Criteria criteria;
	private NotificationManager nm;

	private SessionDataSource sessionDataSource;
	private FallDataSource fallDataSource;
	private ExpiringList acquisitionList;
	private BackgroundTask bgrTask;
	private static SharedPreferences sp;
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
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		if(sessionDataSource == null){
			sessionDataSource = new SessionDataSource(ForegroundService.this);
		}

		//questo fa si che totalTime tenga il tempo per cui la sessione è aperta in totale
		if(!timeInitialized && sessionDataSource.existCurrentSession()){

			totalTime = sessionDataSource.sessionDuration(sessionDataSource.currentSession());
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
			public void onStatusChanged(String provider, int status,Bundle extras) {

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

		if(activeService != null && !locationUpdatesRemoved){ //chiamato sul thread UI


			bestProvider = lm.getBestProvider(criteria, true); 
			lm.requestLocationUpdates(bestProvider, MIN_TIME_LOCATION_UPDATES, MIN_DISTANCE_LOCATION_UPDATES, locationListenerGPS); //if gps is available
			lm.requestLocationUpdates(networkProvider, MIN_TIME_LOCATION_UPDATES, MIN_DISTANCE_LOCATION_UPDATES, locationListenerNetwork); //always updates location with network: it's faster

		}

		//=========================NOTIFICATION(START)============
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(context, CurrentSessionActivity.class);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				717232, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

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

	@SuppressLint("NewApi")
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

		HandlerThread mHandlerThread = new HandlerThread("sensorThread");
		mHandlerThread.start();
		Handler sensorHandler = new Handler(mHandlerThread.getLooper());


		if(Build.VERSION.SDK_INT>=19)
			mSensorManager.registerListener(this, mAccelerometer, MAX_SENSOR_UPDATE_RATE * 1000, 1000, sensorHandler); //fa risparmiare un po' di batteria se sei fortunato e hai android KK+
		else
			mSensorManager.registerListener(this, mAccelerometer, MAX_SENSOR_UPDATE_RATE * 1000, sensorHandler);

		if(sessionDataSource == null){
			sessionDataSource = new SessionDataSource(ForegroundService.this);
		}

	}

	@Override
	public void onDestroy() {

		if(sessionDataSource.existCurrentSession()){ //cioè il service viene chiuso dal sistema per poca memoria
			storeDuration();
			sessionDataSource.setSessionOnPause(sessionDataSource.currentSession());
		}
		resetTime();

		stop = true;
		mSensorManager.unregisterListener(this);
		stopLocationUpdates();
		isRunning = false;
		nm.cancel(717232); //toglie la notifica
	}

	protected void stopLocationUpdates() {
		lm.removeUpdates(locationListenerGPS);
		lm.removeUpdates(locationListenerNetwork);
		locationUpdatesRemoved = true;
	}


	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {

			/*
			 * qui si trova il ciclo che svolge tutto il lavoro che deve fare il service
			 * cioè controlla se i servizi gps sono attivi, se lo sono richiede updates
			 * in modo che se vengono attivati in runtime sono sempre richiesti updates,
			 * questo avviene ogni CYCLES_FOR_LOCATION_REQUESTS iterazioni del service. il service dorme per SERVICE_SLEEP_TIME millisecondi dopodiché controlla
			 * se è stato chiuso. La richiesta di update della location avviene ogni CYCLES_FOR_LOCATION_REQUESTS * SERVICE_SLEEP_TIME millisecondi
			 */
			if(!running)
				while (true) {
					running = true;
					/*
					 * the service keeps running as long as this statement is cycling 
					 * the check for the service to stop occurs every SERVICE_SLEEP_TIME/1000 seconds (to save battery)
					 */
					
					/*
					 * TODO qui aggiorniamo tutte le variabili che possono variare in base alle preferenze (es: TIMEOUT_SESSION)
					 */
					
					MAX_SENSOR_UPDATE_RATE = Integer.parseInt(sp.getString("sample_rate", "10"));
					TIMEOUT_SESSION = Integer.parseInt(sp.getString("max_duration_session", "24"))*3600000;
					
					if(activeService == null){
						activeService = Utility.checkLocationServices(getApplicationContext(), false);		
					}
					if(activeService != null && counterGPSUpdate++ >= CYCLES_FOR_LOCATION_REQUESTS) //richiesto update posizione ogni 2.5 minuti per risparmiare batteria
					{
						counterGPSUpdate = 0;

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if(!locationUpdatesRemoved){
									lm.requestLocationUpdates(GPSProvider, MIN_TIME_LOCATION_UPDATES, MIN_DISTANCE_LOCATION_UPDATES, locationListenerGPS);
									lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_LOCATION_UPDATES,  MIN_DISTANCE_LOCATION_UPDATES, locationListenerNetwork);
								}
							}
						});
					}


					/*
					 *qui check se la sessione rispetta la durata massima, se la sfora, chiuderla
					 */
					if((sessionDataSource != null && getSessionDuration(sessionDataSource) > TIMEOUT_SESSION) && (connectedActs != null && connectedActs.size() > 0)){

						for(final ServiceReceiver sr : connectedActs){
							if(sr instanceof Activity){
								Runnable r = new Runnable(){@Override public void run() { if(sr != null) sr.sessionTimeOut();}};
								((Activity)sr).runOnUiThread(r);
							}
						}
						if(sessionDataSource.existCurrentSession())
							sessionDataSource.closeSession(sessionDataSource.currentSession());
						
					}
					

					try {
						Thread.sleep(SERVICE_SLEEP_TIME);
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




	long lastUpdate = System.currentTimeMillis();
	@SuppressLint("NewApi")
	@Override
	public synchronized void onSensorChanged(SensorEvent event) {


		if(System.currentTimeMillis() - lastSensorChanged >= MAX_SENSOR_UPDATE_RATE){ //non più di un update ogni MAX_SENSOR_UPDATE_RATE millisecondi
			lastSensorChanged = System.currentTimeMillis();

			if(acquisitionList == null)
				acquisitionList = new ExpiringList();

			if(bgrTask == null)
				bgrTask = new BackgroundTask();
			/*
			 * richiedo almeno 10 elementi nella lista per far partire tutto
			 */
			if(acquisitionList.size()>=1){
				if(bgrTask.getStatus()!= AsyncTask.Status.RUNNING){
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						bgrTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
					else
						bgrTask.execute((Void[])null);

				}
			}

			if(bgrTask.getPause())
				bgrTask.wakeUp(); //sveglio ad ogni acquisizione l'asynctask

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				float[] values = event.values;

				final float x = values[0];
				final float y = values[1];
				final float z = values[2];

				c = Calendar.getInstance();
				final long time = c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND); //tempo che va sul grafico

				/*
				 * invio alle activity connesse dati relativi all'acquisizione in tempo reale (grafici)
				 */
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
				if(lastInserted == null || timeNow >= lastInserted.getTime()){
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
		//non ci interessa
	}

	/*
	 * connette l'activity al service
	 */
	public static void connect(ServiceReceiver connectedActivity)
	{
		if(connectedActs == null)
			connectedActs = new ArrayList<ServiceReceiver>();
		connectedActs.add(connectedActivity);
	}

	/*
	 * disconnette l'activity dal service
	 */
	public static void disconnect(ServiceReceiver sr)
	{
		if(connectedActs != null)
			connectedActs.remove(sr);
	}

	/*
	 * flag : connesso / non connesso
	 */
	public static boolean isConnected(ServiceReceiver sr)
	{
		if(connectedActs != null){
			for(ServiceReceiver s : connectedActs)
				if(s.equals(sr))
					return true;
		}
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

	/*
	 * task asincrono che esegue tutto il lavoro di controllo delle acquisizioni in background e salvataggio nel database delle cadute
	 */
	private class BackgroundTask extends AsyncTask<Void, Void, String> {

		private Object INTERRUPTOR = new Object();
		private boolean pause = true;


		public void pauseMyTask() {

			pause = true;
		}


		public void wakeUp() {
			synchronized (INTERRUPTOR){

				INTERRUPTOR.notify();
			}
		}


		public boolean getPause() {
			return pause;
		}


		@Override
		protected String doInBackground(Void... params) {
			//params contiene la espiringlist

			while(true){
				if (pause) {
					synchronized (INTERRUPTOR) {
						try {

							// aspetta risveglio tramite notify su oggetto INTERRUPTOR

							INTERRUPTOR.wait();


						} catch (InterruptedException e) {e.printStackTrace();}
						pause = false;

					}
				}


				if(System.currentTimeMillis() - lastFallTime > TIME_BETWEEN_FALLS)
				{

					if(lastInserted != null){
						float objectX = lastInserted.getXaxis(); final float objectY = lastInserted.getYaxis(); final float objectZ = lastInserted.getZaxis();
						if(Math.sqrt(objectX*objectX + objectY*objectY + objectZ*objectZ) > 17){ //CONTROLLO PRIMO IMPULSO CADUTA PASSANDO SOLO VAL CENTRALE


							//SE PRIMA PARTE CADUTA CONFERMATA QUI PASSO IL RESTO COME COPIA. SE CONTINUA A ESSERE CADUTA, CONTINUIAMO (AGGIUNGERE IF)

							if(DetectorAlgorithm.danielAlgorithm(acquisitionList)){
								lastFallTime = System.currentTimeMillis();

								//=====================ASPETTO 0.5 SECONDI mentre continuo a storare nella coda==========================================
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									Toast.makeText(getApplicationContext(), "INTERRUZIONE INTERRUPTEDEXCEPTION", Toast.LENGTH_LONG).show();
									Thread.currentThread().interrupt();

									break;
									/*
									 * eccezione che avvisa che il thread sta per essere chiuso da un altro thread.
									 * una volta lanciata l'eccezione la flag "interrupted" viene resettata:
									 * se ci sono cicli annidati questo causerà problemi nei cicli esterni, è bene quindi interrompere
									 * il thread corrente di nuovo se questa eccezione viene lanciata
									 */
								}
								//=====================ASPETTO 0.5 SECONDI mentre continuo a storare nella coda(fine)====================================


								//============================================RICEVO DATI GPS(INIZIO=========================================

								Location locationGPS = lm.getLastKnownLocation(GPSProvider);
								Location locationNetwork = lm.getLastKnownLocation(networkProvider);

								if(locationNetwork != null || locationGPS != null){
									latitude = locationGPS != null ? locationGPS.getLatitude() : locationNetwork.getLatitude();
									longitude = locationGPS != null ? locationGPS.getLongitude() : locationNetwork.getLongitude();
								}
								//=============================================RICEVO DATI GPS(FINE)=========================================


								//=====================STORE NEL DATABASE (INIZIO)=================
								if(fallDataSource == null)
									fallDataSource = new FallDataSource(ForegroundService.this);
								//=====================STORE NEL DATABASE(FINE)====================
								
								databaseFallSaverAndFallOccurredManager(ForegroundService.this, fallDataSource, sessionDataSource.currentSession(), acquisitionList.getQueue(), latitude, longitude);
								

								acquisitionList = new ExpiringList(); 

								
								if(latitude != -1 && longitude != -1){
									position = "" + latitude + ", " + longitude;
									link = Utility.getMapsLink(latitude, longitude);
								}
								else
									position = "Not available";
								

								
								
								
							}
						}
					}
				}

				//assicura la chiusura dell'asynctask quando il service viene distrutto
				if(stop==true)
					break;

				//sempre e comunque, metto in sleep l'asynctask alla fine dell'esecuzione del codice che effettua il controllo sulla singola acquisizione
				pauseMyTask();



			}
			return "done";
		}


	}




	/*
	 * restituisce all'esterno la durata esatta della sessione aggiornata con timer interno
	 */
	public static long getSessionDuration(SessionDataSource db)
	{
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




	/*
	 * salva la durata della sessione in modo che sia accessibile per inizializzare il cronometro
	 */
	private void storeDuration()
	{

		if(sessionDataSource.existCurrentSession())
			sessionDataSource.updateSessionDuration(sessionDataSource.currentSession(), System.currentTimeMillis() - startTime);

	}

	/*
	 * usa un thread separato per salvare una caduta nel database
	 * e far apparire tutto nell'adapter
	 */
	private void databaseFallSaverAndFallOccurredManager(final Context ctx, final FallDataSource fds, final SessionDataSource.Session s, final ConcurrentLinkedQueue<Acquisition> al, final double lat, final double lng)
	{
		
		new Thread(new Runnable(){
			@Override
			public void run(){
				FallDataSource.Fall fall = fds.insertFall(s, al, lat, lng);
				
				
				
				//==============================INVIO ALLE ACTIVITY CONNESSE I DATI(INIZIO)=================================
				if(connectedActs != null && connectedActs.size() > 0){
					//TODO C'ERA UN LINK = NULL
					final long fallTime = fall.getTime();
					final String formattedTime = Utility.getStringTime(fallTime);
					for(final ServiceReceiver sr : connectedActs){ 
						Runnable r = new Runnable(){@Override public void run() { sr.serviceUpdate(position, link, formattedTime, false);}};
						if(sr instanceof CurrentSessionCardAdapter)
							((CurrentSessionCardAdapter)sr).runOnUiThread(r);
						else if(sr instanceof Activity)
							((Activity)sr).runOnUiThread(r);
					}
				}
				//==============================INVIO ALLE ACTIVITY CONNESSE I DATI (FINE)=================================

				
				manageFallOccured(position, ctx, fall); //fa quello che c'è da fare quando avviene una fall!
			}
		}).start();
	}

	/*
	 * dice all'esterno se il service sta girando
	 */
	protected static boolean isRunning(){
		return isRunning;
	}
	
	private void manageFallOccured(String position, Context ctx, FallDataSource.Fall fall)
	{
		/*
		 * TODO: stobene - activity
		 */
		
		

		/*
		 * creo nuovo oggetto broadcastreceiver inizializzato con i valori sopra ^^^^^
		 * 
		 * no
		 * passo tutti i parametri in sendsms e salvo in database solo quando è stata mandata. oppure salvo subito e modifico tanto ho l'oggetto li dentro
		 * 
		 * 
		 * devo anche passare riferimento alla lista adapter per poter modificare il campo della fall dall'interno di smsender per quanto riguarda la notifica mandata o no
		 * poi ogni 5 secondi possiam chiamare notifydatasetchanged chi cazzo se ne frega
		 *
		 */
		
		Scanner scan;
		
		String message = getResources().getString(R.string.message);
		message += position;
		if(fall.getLat() != -1 && fall.getLng() != -1)
		{
			message += "\n" + Utility.getMapsLink(fall.getLat(), fall.getLng());
		}
		Set<String> numbers = sp.getStringSet(CONTACTS_KEY, null);
		ArrayList<String> contacts = numbers != null ? new ArrayList<String>(numbers) : new ArrayList<String>();
		ArrayList<String> numbersList = new ArrayList<String>();
		for(int i = 0; i < contacts.size(); i++)
		{
			String fullContact = contacts.get(i);
			scan = new Scanner(fullContact); scan.nextLine();
			String number = scan.nextLine();
			
			numbersList.add(number);
		}
		new SMSender().sendSMSToList(numbersList, ctx, message, fall);
	}
}
