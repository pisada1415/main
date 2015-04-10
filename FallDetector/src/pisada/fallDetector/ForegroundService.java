package pisada.fallDetector;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
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

	private SessionDataSource sessionDataSource;
	private FallDataSource fallDataSource;
	private ExpiringList acquisitionList;

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
			sessionDataSource = new SessionDataSource(this);
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
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

		HandlerThread thread = new HandlerThread("",
				android.os.Process.THREAD_PRIORITY_FOREGROUND); //almost unkillable
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		if(sessionDataSource == null){
			sessionDataSource = new SessionDataSource(this);
		}

	}

	@Override
	public void onDestroy() {
		/*
		 * clean everything up
		 */

		/*deve mandare in pausa la session
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



	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float[] values = event.values;

			float x = values[0];
			float y = values[1];
			float z = values[2];

			c = Calendar.getInstance();
			long time = c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND);

			if(connectedActs != null && connectedActs.size() > 0){
				for(ServiceReceiver sr : connectedActs)
					sr.serviceUpdate(x, y, z, time); //update dell'activity connessa (questo avviene se hai implementato l'interfaccia e fatto connect)
			}



			//TODO RIEMPIRE LA FIGA DI LISTA CON I VALORI DI UN SECONDO DI ACQUISIZ

			if(acquisitionList == null)
				acquisitionList = new ExpiringList();
			acquisitionList.add(new Acquisition(time, x, y, z));

			/*
			 * qui prendi i dati dell'accelerometro e li passi in danielAlgorithm 
			 * sotto forma di "roba"
			 */
			boolean fall = DetectorAlgorithm.danielAlgorithm(x, y, z); //può restituire bool per identificare una caduta, in questo caso qui di seguito lanciamo la classe che notifica e manda email

			if(fall)
			{
				Location locationGPS = lm.getLastKnownLocation(GPSProvider);
				Location locationNetwork = lm.getLastKnownLocation(networkProvider);

				latitude = locationGPS != null ? locationGPS.getLatitude() : locationNetwork.getLatitude();
				longitude = locationGPS != null ? locationGPS.getLongitude() : locationNetwork.getLongitude();
				//qui usare latitude e longitude e mandarle al metodo che manderà l'email. considerare che saranno null in caso non ci siano i servizi attivi.
				//
				//
				//
				//
			}

			//il controllo va fatto sul dato di mezzo secondo fa' (metà arraylist)

			int mid = acquisitionList.size() >>> 1; //operazione per avere sempre l'elemento a metà evitando overflow vari.
			Acquisition middle = acquisitionList.get(mid);

			
			//TEMPORANEOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
			//MA TUTTA STA ROBA è OK. SOLO L'IF è TEMPORANEO. va fatto se avviene la fall.
			if(Math.sqrt(middle.getXaxis()*middle.getXaxis() + middle.getYaxis()*middle.getYaxis() + middle.getZaxis()*middle.getZaxis()) > 20){ //provvisorio, sarà sostituito da danielalgorithm
				Location locationGPS = lm.getLastKnownLocation(GPSProvider);
				Location locationNetwork = lm.getLastKnownLocation(networkProvider);

				if(locationNetwork != null || locationGPS != null){
					latitude = locationGPS != null ? locationGPS.getLatitude() : locationNetwork.getLatitude();
					longitude = locationGPS != null ? locationGPS.getLongitude() : locationNetwork.getLongitude();
				}
				if(connectedActs.size() > 0){

					String position;
					String link = null;
					long fallTime = System.currentTimeMillis();

					if(latitude != null && longitude != null){
						position = "" + latitude + ", " + longitude;
						link = "https://www.google.com/maps/@" + latitude+","+longitude + ",13z";

					}
					else
						position = "Not available";


					//TODO STORE fallTime e position per la caduta

					//=====================store nel database=================

					acquisitionList.add(new Acquisition(time, x,y,z));
					if(fallDataSource == null)
						fallDataSource = new FallDataSource(this);
					fallDataSource.insertFall(sessionDataSource.currentSession(), acquisitionList.getList());
					//=================store nel database (end)===============


					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy_hh:mm:ss");

					// milliseconds to date 
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(fallTime);
					Date date = calendar.getTime();
					String formattedTime = formatter.format(date);


					for(ServiceReceiver sr : connectedActs)
						sr.serviceUpdate(position, link, formattedTime, fallTime);
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
		uiHandler.post(runnable);
	}


	/*
	 * viene chiusa la sessione e poi viene comunque chiamato ondestroy sulla sessione
	 * già chiusa che risulta null nel database. perché ondestroy contiene storeduration.
	 * soluzioni:
	 * try - catch (poco elegante)
	 */
	/*protected static void storeDuration(SessionDataSource sessionData)
	{
		sessionData.updateSessionDuration(sessionData.currentSession(), System.currentTimeMillis() - totalTime);
	}*/
	private void storeDuration()
	{
		if(sessionDataSource.existCurrentSession())
			sessionDataSource.updateSessionDuration(sessionDataSource.currentSession(), System.currentTimeMillis() - startTime);
		//totalTime = System.currentTimeMillis();
	}

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

	private void resetTime()
	{
		timeInitialized = false;
	}

}