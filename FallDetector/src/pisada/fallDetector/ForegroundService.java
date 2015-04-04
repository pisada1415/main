package pisada.fallDetector;



import java.util.Calendar;

import pisada.database.AcquisitionDataSource;
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
import android.widget.Toast;

/*
 * funzionamento del gps:
 * la location viene aggiornata ogni 5 minuti richiedendo la posizione ai provider gps e network.
 * le coordinate vengono poi passate al metodo che invia la notifica della caduta dando la precedenza ai dati in arrivo dal gps.
 * todo: 
 * -usare play service SOLO se disponibili nel dispositivo in uso per avere una location più accurata
 * -geolocator per dire nome del paese in cui si trova oltre alle coordinate (sempre se play services disponibili)
 * 
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
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static ServiceReceiver connectedAc;
	private LocationListener locationListenerGPS, locationListenerNetwork;
	private LocationManager lm;
	private Double latitude = null;
	private Double longitude = null;
	private Calendar c;
	private int counterGPSUpdate = 5; //per attivare subito la ricerca della posizione
	private Handler uiHandler;
	private Criteria criteria;
	private String bestProvider;
	private String activeService;
	private long startTime = System.currentTimeMillis();
	private NotificationManager nm;

	private AcquisitionDataSource acquisitionData;
	private SessionDataSource sessionDataSource;

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
		if(acquisitionData == null){
			acquisitionData=new AcquisitionDataSource(this);
			acquisitionData.open();}
		if(sessionDataSource == null){
			sessionDataSource = new SessionDataSource(this);
			sessionDataSource.open();}

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
		if(acquisitionData == null){
			acquisitionData=new AcquisitionDataSource(this);
			acquisitionData.open();}
		if(sessionDataSource == null){
			sessionDataSource = new SessionDataSource(this);
			sessionDataSource.open();}
		new Thread(new Runnable(){
			@Override
			public void run()
			{
				while(!stop){
					
						storeDuration();
					try {
						Thread.sleep(1000); //precisione di un secondo +-
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
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
		sessionDataSource.close();
		acquisitionData.close();
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

			if(connected) //&& connectedAc == null
			{
				long time = c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND);
				connectedAc.serviceUpdate(x, y, z, time);
			}
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
			/*
			 * if(fall){
			 * Intent launchImOK = new Intent(getBaseContext(), ImOK.class);
			 * launchImOK.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * launchImOK.putExtras("latitude", latitude);
			 * launchImOK.putExtras("longitude", longitude); //se sono null va scritto location non disponibile
			 * getApplication().startActivity(launchImOK);
			 * (sarebbe bello fosse un dialog più che un'activity)
			 */
			//TODO storetoDB(time, x, y, z); per salvare i dati nel DataBase

		}
	}



	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//not interesting for this purpose
	}

	public static void connect(ServiceReceiver connectedActivity)
	{
		connectedAc = connectedActivity;
		connected = true;
	}

	public static void disconnect()
	{
		connectedAc = null;
		connected = false;
	}

	public static boolean isConnected()
	{
		return connectedAc != null;
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
		sessionData.updateSessionDuration(sessionData.currentSession(), System.currentTimeMillis() - startTime);
	}*/
	private void storeDuration()
	{
		if(sessionDataSource.existCurrentSession())
			sessionDataSource.updateSessionDuration(sessionDataSource.currentSession(), System.currentTimeMillis() - startTime);
	}

}