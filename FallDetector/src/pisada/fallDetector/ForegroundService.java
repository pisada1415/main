package pisada.fallDetector;



import java.util.Calendar;

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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;

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
 * ora
 */

public class ForegroundService extends Service implements SensorEventListener,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

	private final String GPSProvider = LocationManager.GPS_PROVIDER;
	private final String networkProvider = LocationManager.NETWORK_PROVIDER;

	private boolean stop = false; 
	private boolean running = false;
	private boolean playServices = false;
	private static boolean connected = false;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static ServiceReceiver connectedAc;
	private LocationListener locationListenerGPS, locationListenerNetwork;
	private LocationManager lm;
	private Double latitude = null;
	private Double longitude = null;
	private Location locationPlayServices;
	private String activeService;
	private Calendar c;
	private int counterGPSUpdate = 5; //per attivare subito la ricerca della posizione
	private Handler uiHandler;
	private Criteria criteria;
	private String bestProvider;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;

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

		uiHandler = new Handler();
		criteria = new Criteria();
		buildGoogleApiClient();
		activeService = intent.getExtras().getString("activeServ");
		playServices = intent.getBooleanExtra("playServicesAvailable", false);
		
		
		//========================================================================


		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


		locationListenerGPS = new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				System.out.println("AGGIORNATA POSIZIONE SENZA PLAY SERVICES GPS");
				stopLocationUpdates();
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
				System.out.println("AGGIORNATA POSIZIONE SENZA PLAY SERVICES NETWORK");
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

		if(activeService != null){ //chiamato sul thread UI
			if(!playServices){
				
				bestProvider = lm.getBestProvider(criteria, true); 
				lm.requestLocationUpdates(bestProvider, 5000, 0/*50*/, locationListenerGPS); //if gps is available
				lm.requestLocationUpdates(networkProvider, 5000, 0/*50*/, locationListenerNetwork); //always updates location with network: it's faster
				System.out.println("inizializzato senza PLAY SERVICES inizio. LATLNG nulle penso = " + latitude + " " + longitude);
			}
			else{
				createLocationRequest();
				bestProvider = lm.getBestProvider(criteria, true); 
				if(locationPlayServices == null)
					locationPlayServices = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
				// Get latitude of the current location 
				if(locationPlayServices != null){
					latitude = locationPlayServices.getLatitude(); 
					// Get longitude of the current location 
					longitude = locationPlayServices.getLongitude();
				}
				System.out.println("POSIZIONE AGGIORNATA PLAY SERVICES inizio. LATLNG = " + latitude + " " + longitude);
			}
		}

		//=========================NOTIFICATION(START)============
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, CurrentSessionActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				717232, notificationIntent, 0);

		NotificationManager nm = (NotificationManager) context
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

	}

	@Override
	public void onDestroy() {
		/*
		 * clean everything up
		 */
		stop = true;
		mSensorManager.unregisterListener(this);
		stopLocationUpdates();
	}

	protected void stopLocationUpdates() {
		lm.removeUpdates(locationListenerGPS);
		lm.removeUpdates(locationListenerNetwork);
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
					if(activeService != null && counterGPSUpdate++ >= 5) //richiesto update posizione ogni 5 minuti per risparmiare batteria
					{
						counterGPSUpdate = 0;
						//lm.requestLocationUpdates(activeService, 2000, 10, locationListener);

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if(!playServices){
									lm.requestLocationUpdates(GPSProvider, 5000, 0/*50*/, locationListenerGPS);
									lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0/*50*/, locationListenerNetwork);
									System.out.println("POSIZIONE AGGIORNATA senza!! PLAY SERVICES. LATLNG = " + latitude + " " + longitude);
								}
								else{
									bestProvider = lm.getBestProvider(criteria, true); 
									locationPlayServices = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);//lm.getLastKnownLocation(bestProvider);
									
									if(locationPlayServices == null) locationPlayServices = lm.getLastKnownLocation(networkProvider);
									else System.out.println("FUNZIONANOOOOOOOOOOOOOOO");
									// Get latitude of the current location 
									latitude = locationPlayServices.getLatitude(); 
									// Get longitude of the current location 
									longitude = locationPlayServices.getLongitude(); 
									System.out.println("POSIZIONE AGGIORNATA PLAY SERVICES. LATLNG = " + latitude + " " + longitude);
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

	private void runOnUiThread(Runnable runnable) {
		uiHandler.post(runnable);
	}



	protected synchronized void buildGoogleApiClient() {
		   mGoogleApiClient = new GoogleApiClient.Builder(this)
		        .addConnectionCallbacks(this)
		        .addOnConnectionFailedListener(this)
		        .addApi(LocationServices.API)
		        .build();
		   mGoogleApiClient.connect();
		}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, "connection to google play services failed", Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(this, "connected to google play services", Toast.LENGTH_SHORT).show();
		
		startLocationUpdates();
	
	}
	
	protected void startLocationUpdates() {
	    LocationServices.FusedLocationApi.requestLocationUpdates(
	            mGoogleApiClient, mLocationRequest, this);
	}
	
	

	@Override
	public void onConnectionSuspended(int arg0) {
		Toast.makeText(this, "connection to google play services suspended", Toast.LENGTH_SHORT).show();
		
	}
	
	protected void createLocationRequest() {
	    mLocationRequest = new LocationRequest();
	    mLocationRequest.setInterval(10000); //da cambiare
	    mLocationRequest.setFastestInterval(5000); //da cambiare
	    mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
	}



	@Override
	public void onLocationChanged(Location location) {
		locationPlayServices = location;
		System.out.println("LOCATION CAMBIATA PLAYYYYYYYYYYY");
	}




}