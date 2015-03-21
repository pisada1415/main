package pisada.fallDetector;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

public class ForegroundService extends Service implements SensorEventListener {

	boolean stop = false; 
	boolean running = false;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		//	    handleCommand(intent);
	}

	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * this method is called when another component (activity) requests the service
		 * to start.
		 * Service needs then to be stopped when the job is done (when the stop button is pressed
		 * ) by calling stopSelf() or stopService()
		 */

		//	handleCommand(intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.


		Notification notification = new Notification(R.drawable.notificationicon, "FallDetector detecting...",
				System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, SessionsListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(this, "FallDetector",
				"Detecting...", pendingIntent);


		if(running)
		{
			Toast.makeText(this, "Already running", Toast.LENGTH_LONG).show();
			return Service.START_STICKY;
		}
		else
		{
			startForeground(717232, notification);

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
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
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
					 * here we check the accelerometer data and execute the algorithm
					 */
					
					/*
					 * qui prendi i dati dell'accelerometro e li passi in danielAlgorithm 
					 * sotto forma di "roba"
					 */
					DetectorAlgorithm.danielAlgorithm(/*roba*/); 
					
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(stop)
						break;

				}
			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			running = false;
			stopSelf(msg.arg1);
		}
	}



	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float[] values = event.values;
		    // Movement
		    float x = values[0];
		    float y = values[1];
		    float z = values[2];
		    System.out.println("values:" + x + " ," + y + " , " + z );
		}
	}



	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
