package test;


import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import pisada.database.Acquisition;
import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.fallDetector.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import fallDetectorException.DublicateNameSessionException;
import fallDetectorException.MoreThanOneOpenSessionException;



public class TestActivity extends Activity{


	private FallDataSource fallDataSource;
	private SessionDataSource.Session currentSession; 
	private SessionDataSource sessionData;
	private SessionDataSource.Session session = null;
	private ConcurrentLinkedQueue<Acquisition> q,q1,q2, q3, q4;
	private int n = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		sessionData = new SessionDataSource(this);
		fallDataSource = new FallDataSource(this);
		Random r = new Random();
		try {
			if(sessionData.existCurrentSession())
				sessionData.closeSession(sessionData.currentSession());
			session = sessionData.openNewSession("giannanono" + r.nextDouble(), "giajnna"+r.nextDouble(), System.currentTimeMillis());
		} catch (MoreThanOneOpenSessionException e) {
			e.printStackTrace();
		} catch (DublicateNameSessionException e) {
			e.printStackTrace();
		}
		
		q = new ConcurrentLinkedQueue<Acquisition>();
		q1 = new ConcurrentLinkedQueue<Acquisition>();
		q2 = new ConcurrentLinkedQueue<Acquisition>();
		q3 = new ConcurrentLinkedQueue<Acquisition>();
		q4 = new ConcurrentLinkedQueue<Acquisition>();

		init();
		Toast.makeText(this, "ready", Toast.LENGTH_LONG).show();;

		Button button1 = (Button)findViewById(R.id.button1);

		button1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(){
					@Override
					public void run()
					{
						long time = System.currentTimeMillis();
						System.out.println("inizio a salvare thread 0");

						fallDataSource.insertFall(session, q, 20 , 20);
						long timeElapsed = System.currentTimeMillis() - time;
						System.out.println("ci ho messo " + timeElapsed + " a salvare una coda di " + n + " elementi, che significa in media " + ((float)(timeElapsed))/((float)n) + " millisecondi per store");

					}
				}.start();
			/*	new Thread(){
					@Override
					public void run()
					{
						long time = System.currentTimeMillis();
						System.out.println("inizio a salvare thread 1");

						fallDataSource.insertFall(session, q1, 20 , 20);
						long timeElapsed = System.currentTimeMillis() - time;
						System.out.println("ci ho messo " + timeElapsed + " a salvare una coda di " + n + " elementi, che significa in media " + ((float)(timeElapsed))/((float)n) + " millisecondi per store");

					}
				}.start();
				new Thread(){
					@Override
					public void run()
					{
						long time = System.currentTimeMillis();
						System.out.println("inizio a salvare thread 2");

						fallDataSource.insertFall(session, q2, 20 , 20);
						long timeElapsed = System.currentTimeMillis() - time;
						System.out.println("ci ho messo " + timeElapsed + " a salvare una coda di " + n + " elementi, che significa in media " + ((float)(timeElapsed))/((float)n) + " millisecondi per store");

					}
				}.start();
				new Thread(){
					@Override
					public void run()
					{
						long time = System.currentTimeMillis();
						System.out.println("inizio a salvare thread 3");

						fallDataSource.insertFall(session, q3, 20 , 20);
						long timeElapsed = System.currentTimeMillis() - time;
						System.out.println("ci ho messo " + timeElapsed + " a salvare una coda di " + n + " elementi, che significa in media " + ((float)(timeElapsed))/((float)n) + " millisecondi per store");

					}
				}.start();
				new Thread(){
					@Override
					public void run()
					{
						long time = System.currentTimeMillis();
						System.out.println("inizio a salvare thread 4");

						fallDataSource.insertFall(session, q4, 20 , 20);
						long timeElapsed = System.currentTimeMillis() - time;
						System.out.println("ci ho messo " + timeElapsed + " a salvare una coda di " + n + " elementi, che significa in media " + ((float)(timeElapsed))/((float)n) + " millisecondi per store");

					}
				}.start();*/

			}
		});
		//	}

	}


	public void init()
	{
		int i = 0;
		long timePrev = System.currentTimeMillis();
		long timetmp = System.currentTimeMillis();
		while(i<n){
			timetmp = System.currentTimeMillis();
			if(timetmp != timePrev){
				timePrev = timetmp;
				q.add(new Acquisition(timetmp, 1, 1, 1));
				i++;
			}
		}
		i = 0;
		while(i<n){
			timetmp = System.currentTimeMillis();
			if(timetmp != timePrev){
				timePrev = timetmp;
				q1.add(new Acquisition(timetmp, 1, 1, 1));
				i++;
			}
		}
		i = 0;
		while(i<n){
			timetmp = System.currentTimeMillis();
			if(timetmp != timePrev){
				timePrev = timetmp;
				q2.add(new Acquisition(timetmp, 1, 1, 1));
				i++;
			}
		}
		i = 0;
		while(i<n){
			timetmp = System.currentTimeMillis();
			if(timetmp != timePrev){
				timePrev = timetmp;
				q3.add(new Acquisition(timetmp, 1, 1, 1));
				i++;
			}
		}
		i = 0;
		while(i<n){
			timetmp = System.currentTimeMillis();
			if(timetmp != timePrev){
				timePrev = timetmp;
				q4.add(new Acquisition(timetmp, 1, 1, 1));
				i++;
			}
		}

	}



	@Override
	public void onPause()
	{
		super.onPause();
		
	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		
		init();
		Toast.makeText(this, "ready", Toast.LENGTH_LONG).show();;
	}




}
