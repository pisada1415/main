package pisada.recycler;


import java.util.ArrayList;
import java.util.Calendar;
import fallDetectorException.DublicateNameSessionException;
import fallDetectorException.MoreThanOneOpenSessionException;
import pisada.database.FallDataSource;
import pisada.database.FallDataSource.Fall;
import pisada.database.SessionDataSource;
import pisada.fallDetector.FallDetailsDialogFragment;
import pisada.fallDetector.ForegroundService;
import pisada.fallDetector.FragmentCommunicator;
import pisada.fallDetector.R;
import pisada.fallDetector.ServiceReceiver;
import pisada.fallDetector.SessionDetailsFragment;
import pisada.fallDetector.Utility;
import pisada.plotmaker.Data;
import pisada.plotmaker.Plot;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
public class CurrentSessionCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ServiceReceiver {

	private static ArrayList<FallDataSource.Fall> cardContentList;
	private Activity activity;


	private double last;
	private static Plot graph;
	private Calendar c;
	private long millisecStartGraph;
	private static Chronometer duration; 
	private static long timeSessionUp;
	private static long timeWhenPaused = 0;
	private String infoText,  sessionNameDefault;
	private SharedPreferences sp;
	private Intent serviceIntent;
	private TextView info;
	private static boolean startChronometerOnStart = false;
	private final String CONTACTS_KEY = "contacts";
	private static String currentSessionName;
	private SessionDataSource sds;
	private ProgressBar pb;
	private ImageView fallThumbnail;
	private ImageView thumbNailCurrent;
	private SessionDataSource.Session session;
	private Drawable pause, play;
	
	/*
	 * 
	 * first_new_currentsession_card
	 */
	public class FirstCardHolder extends RecyclerView.ViewHolder {

		

		private Button playPause;
		private Button stop;
		
		@SuppressWarnings("deprecation")
		public FirstCardHolder(View v) {
			super(v);
			playPause = (Button) v.findViewById(R.id.start_pause_button);
			stop = (Button) v.findViewById(R.id.stop_button);
			duration = (Chronometer) v.findViewById(R.id.chronometer);
			thumbNailCurrent = (ImageView)v.findViewById(R.id.thumbnail);
			info =  (TextView) v.findViewById(R.id.info);
			pb = (ProgressBar)v.findViewById(R.id.progressBarFirstCard);
			pb.setVisibility(View.VISIBLE);
			if(infoText != null)
				info.setText(infoText);
			else
				info.setVisibility(View.GONE);
			if(session != null)
				BitmapManager.loadBitmap(session.getID(), thumbNailCurrent, activity);
			else
				thumbNailCurrent.setVisibility(View.GONE);

			if(startChronometerOnStart)
				startChronometer();
			if(timeWhenPaused != 0)
			{
				duration.setBase(SystemClock.elapsedRealtime() - timeWhenPaused);
				duration.start();
				duration.stop();
				timeWhenPaused = 0;
			}

			if(sds.existCurrentSession())
			{
				if(sds.currentSession().isOnPause())
					playPause.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.button_selector_play));
				else
					playPause.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.button_selector_pause));
				//devo usare il deprecato perché per setBackground serve API Level 16
			}
			stop.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					stopChronometer();
					clearFalls();
					String closedSessionName = null;
					if(serviceIntent == null)
						serviceIntent = new Intent(activity, ForegroundService.class);
					if(sds.existCurrentSession())
						closedSessionName = sds.currentSession().getName();
					if(serviceIntent!=null && ForegroundService.isRunning()){
						ForegroundService.killSessionOnDestroy();
						activity.stopService(serviceIntent);//altro metodo con stesso nome ma di Activity che semplicemente stoppa il service
					}
					else if(sds.existCurrentSession())
						sds.closeSession(sds.currentSession());
					serviceIntent = null;
					session = null;
					currentSessionName = sessionNameDefault;
					activity.setTitle(sessionNameDefault);
					clearGraphs();

					if(closedSessionName != null){
						Intent toPiero = new Intent(activity, SessionDetailsFragment.class);
						toPiero.putExtra(Utility.SESSION_NAME_KEY, closedSessionName); 
						((FragmentCommunicator)activity).switchFragment(toPiero); 
					}
				}
			});
			playPause.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(final View v) {
					
					final Drawable selection;
					v.setClickable(false);
					String info = null;
					
					if(sds.existCurrentSession()){
						session = sds.currentSession();
						if(session.isOnPause())
							selection = pause;
						else
							selection = play;
					}
					else
						selection = pause;
					
					v.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.nonclickable));
					/*
					 * qui per due secondi setto un background blu con caricamento in mezzo. poi cambia. per quei due secondi è anche inclickabile
					 */

					new Thread(){
						@Override
						public void run(){

							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							activity.runOnUiThread(new Runnable() {


								@SuppressLint("NewApi")
								@Override
								public void run() {
									v.setClickable(true);
									int sdk = android.os.Build.VERSION.SDK_INT;
										if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
											v.setBackgroundDrawable(selection);
										} else {
											v.setBackground(selection);
										}

								}		
							});

						}
					}.start();

					int chronometer;

					long time = System.currentTimeMillis(); //MEMORIZZA IL MOMENTO IN CUI è STATO PREMUTO IL TASTO
					if(currentSessionName.equals(sessionNameDefault)) //cioè non è stato cambiato
						currentSessionName = "Session:"+ Utility.getStringTime(time); //assegno nome default UNICO (altrimenti tengo quello cambiato)


					if(!ForegroundService.isRunning()){
						//il service non sta andando
						if(!sds.existCurrentSession()){
							//non esiste sessione corrente: creane una nuova
							session = null;
							session = addSession(currentSessionName, "" + time, time, 0);
							activity.setTitle(currentSessionName);
							//FA PARTIRE IL SERVICE
							serviceIntent = new Intent(activity, ForegroundService.class);
							String activeServ = Utility.checkLocationServices(activity, true);
							serviceIntent.putExtra("activeServices", activeServ);
							activity.startService(serviceIntent);
							info = activity.getResources().getString(R.string.starttime) + Utility.getStringTime(System.currentTimeMillis());
							chronometer = 0;
						}
						else
						{
							session = sds.currentSession();

							//ESISTE SESSIONE CORRENTE
							if(sds.currentSession().isOnPause()){
								//è IN PAUSA
								sds.resumeSession(session); //LA FACCIO RIPARTIRE
								//FA PARTIRE IL SERVICE
								serviceIntent = new Intent(activity, ForegroundService.class);
								String activeServ = Utility.checkLocationServices(activity, true);
								serviceIntent.putExtra("activeServices", activeServ);
								activity.startService(serviceIntent);
								chronometer = 0;
								info = activity.getResources().getString(R.string.starttime) + Utility.getStringTime(session.getStartTime());
							}
							else{
								//STA ANDANDO, QUINDI VA MESSA IN PAUSA

								sds.setSessionOnPause(sds.currentSession());
								activity.stopService(serviceIntent); //dovrebbe essere inutile
								chronometer = 1;
								info = "";
							}
						}

					}
					else
					{

						//il service sta già andando (STESSA COSA DI PRIMA MA QUI NON FA PARTIRE IL SERVICE)
						if(!sds.existCurrentSession()){
							//non esiste sessione corrente: creane una nuova
							session = null;
							session = addSession(currentSessionName, "" + time, time, 0);
							activity.setTitle(currentSessionName);
							chronometer = 0;
							info = activity.getResources().getString(R.string.starttime) + Utility.getStringTime(session.getStartTime());
						}
						else
						{
							session = sds.currentSession();

							if(sds.currentSession().isOnPause()){
								sds.resumeSession(session);
								activity.setTitle(session.getName());
								chronometer = 0;

								info = activity.getResources().getString(R.string.starttime) + Utility.getStringTime(System.currentTimeMillis());


							}
							else{
								//pausa

								sds.setSessionOnPause(session);
								//	ForegroundService.storeDuration(sds);
								activity.stopService(serviceIntent); 
								chronometer = 1;
							}
						}

					}

					setCurrentSessionValues(info, session, chronometer);
					if(session != null)
					updateSessionName(session.getName());

				}
			});
			

		}
	}	

	/*
	 * plots_card
	 */
	public  class SecondCardHolder extends RecyclerView.ViewHolder {
		private LinearLayout graphLayout;
		/*private LinearLayout graphXLayout;
		private LinearLayout graphYLayout;
		private LinearLayout graphZLayout;*/

		/*
		 * qui aggiungiamo sui parametri del costruttore la roba da passare per buttarla nelle card (penso)
		 */

		public SecondCardHolder(View v) {
			super(v);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 

			graphLayout = (LinearLayout) v.findViewById(R.id.graph);
			/*
			graphXLayout=(LinearLayout) v.findViewById(R.id.graphx);
			graphYLayout=(LinearLayout) v.findViewById(R.id.graphy);
			graphZLayout=(LinearLayout) v.findViewById(R.id.graphz);*/

			long timePoint = (c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND) - millisecStartGraph);

			graph = new Plot(activity, new Data(timePoint, 0));
			/*
			graphX = new Plot2d(activity, new Data(timePoint,0));
			graphY = new Plot2d(activity, new Data(timePoint,0));
			graphZ = new Plot2d(activity, new Data(timePoint,0));
			 */
			graphLayout.addView(graph, lp);
			/*
			graphXLayout.addView(graphX, lp);
			graphYLayout.addView(graphY, lp);
			graphZLayout.addView(graphZ, lp);
			 */
		}

	}
	/*
	 * fall cards
	 */
	public class FallsHolder extends RecyclerView.ViewHolder{
		
		private TextView fallTime;
		private TextView fallPosition;
		private TextView boolNotif;
		public FallsHolder(View v) {
			super(v);
			fallThumbnail=(ImageView) v.findViewById(R.id.thumbnail_fall);
			fallTime=(TextView) v.findViewById(R.id.fall_time);
			fallPosition=(TextView) v.findViewById(R.id.position);
			boolNotif = (TextView) v.findViewById(R.id.booleanSent);
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO q
					int position = getAdapterPosition();
					Intent intent = new Intent(activity, FallDetailsDialogFragment.class);
					long time = cardContentList.get(position).getTime();
					intent.putExtra(Utility.FALL_TIME_KEY, time);
					intent.putExtra(Utility.SESSION_NAME_KEY, currentSessionName);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
					((FragmentCommunicator)activity).switchFragment(intent);
				}
			});
		}
	}



	public CurrentSessionCardAdapter(Activity activity, long time, boolean startChron, long pauseTime) {

		this.activity=activity;
		c = Calendar.getInstance();
		ForegroundService.connect(this);
		millisecStartGraph = c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND);
		cardContentList = new ArrayList<Fall>();
		cardContentList.add(0, new Fall());
		cardContentList.add(1, new Fall());
		timeSessionUp = time;
		startChronometerOnStart = startChron;
		sds = new SessionDataSource(activity);
		if(pauseTime != 0) {
			timeWhenPaused = pauseTime;
		}
		pause =activity.getResources().getDrawable(R.drawable.button_selector_pause);
		play =activity.getResources().getDrawable(R.drawable.button_selector_play);
		sessionNameDefault = activity.getResources().getString(R.string.defaultSessionName);
		currentSessionName = sessionNameDefault;
		sp = PreferenceManager.getDefaultSharedPreferences(activity);
	}

	Handler mHandler;
	public void runOnUiThread(Runnable r){
		if(mHandler == null)
			mHandler = new Handler(Looper.getMainLooper());
		mHandler.post(r);

	}


	@Override
	public void onBindViewHolder(ViewHolder holder, int i) {

		if(i==0){ //se sono le prime due non fare niente
			
		}
		else if(i == 1){}
		else{
			/*
			 * TODO qui anziché randint va passato il numero della sessione cui la fall fa riferimento
			 */
			Fall fall = cardContentList.get(i);
			FallsHolder Oholder=(FallsHolder) holder;
			//loadBitmap(session.getID(), fallThumbnail, activity);
			//fallThumbnail.setImageBitmap(Utility.createImage(session.getID()));
			
			/*if(sessionBitmap != null)
				fallThumbnail.setImageBitmap(sessionBitmap);
			else
			{
				sessionBitmap = Utility.createImage(session.getID());
				fallThumbnail.setImageBitmap(sessionBitmap);
			}
			*/
			BitmapManager.loadBitmap(session.getID(), fallThumbnail, activity);
			String link = Utility.getMapsLink(fall.getLat(), fall.getLng());
			String position = fall.getLat() != -1 && fall.getLng() != -1 ? fall.getLat() + ", " + fall.getLng() : activity.getResources().getString(R.string.notavailable);

			if(link != null){
				Oholder.fallPosition.setText(Html.fromHtml("<a href=\""+ link + "\">" + "Position: " + position + "</a>"));

				Oholder.fallPosition.setClickable(true);
				Oholder.fallPosition.setMovementMethod (LinkMovementMethod.getInstance());
			}
			else{
				Oholder.fallPosition.setText("Position: " + position);
				Oholder.fallPosition.setClickable(false);
			}

			//Oholder.fallPosition.setText("Position: "+ fall.getPos());
			Oholder.fallTime.setText("Time: " + Utility.getStringTime(fall.getTime()));
			if(fall.wasNotified()){
				Oholder.boolNotif.setText(activity.getResources().getString(R.string.sent));
				Oholder.boolNotif.setTextColor(Color.GREEN);
			}
			else if(sp.getStringSet(CONTACTS_KEY, null) == null || sp.getStringSet(CONTACTS_KEY, null).size()==0)
			{
				Oholder.boolNotif.setText(activity.getResources().getString(R.string.requiresSetup));
				Oholder.boolNotif.setTextColor(Color.RED);
			}


		}

	}




	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {


		if(type==0){
			return new FirstCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.first_current_session_currentsession_card, parent, false));
		}
		if(type==1)
		{
			return new SecondCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plots_card, parent, false));

		}
		else 
		{

			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fall_card, parent, false);


			return new FallsHolder(view);
		}

	}


	@Override
	public int getItemViewType(int position) {
		if(position==0) return 0;
		if(position == 1) return 1;
		return 2;
	}



	@Override
	public int getItemCount() {
		return cardContentList.size();
	}



	@Override
	public void serviceUpdate(float x, float y, float z, long time) {
		//lastX = x; lastY = y; lastZ = z; 
		last = Math.sqrt(x*x + y*y + z*z);

		c = Calendar.getInstance();
		if(graph != null){
			graph.pushValue(new Data(time, last));
			graph.invalidate();
			
		}
	}


	public void clearGraphs()
	{
		/*
		if(graphX != null && graphY != null && graphZ != null){
			graphX.clear();
			graphY.clear();
			graphZ.clear();
		}*/
		if(graph != null)
			graph.clear();
		//altrimenti fallisce silenziosamente
	}


	/*
	 * QUI SALVIAMO I TEMPI NEL DATABASE 
	 */
	static long timePause = 0;
	private void pauseChronometer()
	{
		timePause = duration.getBase() - SystemClock.elapsedRealtime();

		duration.stop();
	}
	private void startChronometer()
	{
		if(timePause == 0){

			long base = SystemClock.elapsedRealtime()-timeSessionUp;
			duration.setBase(base);
			duration.start();
		}
		else{

			duration.setBase(SystemClock.elapsedRealtime() + timePause);
			duration.start();
		}
	}
	public void stopChronometer()
	{
		duration.setBase(SystemClock.elapsedRealtime());
		timeSessionUp = 0;
		timePause = 0; //non deve riprendere da tempo stop
		duration.stop();
	}

	private void addFallToCardList(Fall fall)
	{
		if(!cardContentList.contains(fall)){
			cardContentList.add(fall);
			notifyItemInserted(cardContentList.size()-1);
		}
		else
		{
			int i = 0;
			for(; i < cardContentList.size() && !cardContentList.get(i).equals(fall); i++);
			cardContentList.set(i, fall);
			notifyItemChanged(i);

		}
	}

	@Override
	public void serviceUpdate(Fall fall, String sessionName) {
		currentSessionName = sessionName;
		addFallToCardList(fall);
	}

	public void addFall(FallDataSource.Fall f, SessionDataSource.Session s)
	{
		addFallToCardList(f);
	}

	public void clearFalls()
	{
		while(cardContentList.size()>2)
		{
			cardContentList.remove(2);
			this.notifyItemRemoved(2);
		}

	}


	@Override
	public void sessionTimeOut() {
		//NON NECESSARIO QUI
	}

	@Override
	public boolean equalsClass(ServiceReceiver obj) {
		// TODO Auto-generated method stub
		if(obj instanceof CurrentSessionCardAdapter)
			return true;
		return false;
	}

	public void updateSessionName(String newName)
	{
		currentSessionName = newName;
	}
	public void setCurrentSessionValues(String infoString, SessionDataSource.Session s, int chronometer)
	{
		session = s;
		if(info != null && infoString != "")
		{
			info.setText(infoString);
			info.setVisibility(View.VISIBLE);
		}
		else if(info != null)
			info.setVisibility(View.GONE);
		if(infoString != "" && info == null)
			infoText = infoString;
		
		if(thumbNailCurrent != null && session != null){
			BitmapManager.loadBitmap(session.getID(), thumbNailCurrent, activity);
			thumbNailCurrent.setVisibility(View.VISIBLE);
		}
		else if(thumbNailCurrent != null)
			thumbNailCurrent.setVisibility(View.GONE);
		/*if(thumbNailCurrent == null && session != null)
			bitmapThumbNailCurrent = sessionBitmap;*/


		switch(chronometer)
		{
		case 0:
			this.startChronometer();
			break;
		case 1:
			this.pauseChronometer();
			break;
		default:
			break;
		}

	}
	
	
	public SessionDataSource.Session addSession(String name, String pic, long timeStart, long timeEnd) {

		SessionDataSource.Session session = null;
		if(!sds.existSession(name)){

			try{
				session = sds.openNewSession(name, pic, timeStart, timeEnd);
			}
			catch(SQLiteConstraintException e){
				e.printStackTrace();
			} catch (fallDetectorException.BoolNotBoolException e) {
				e.printStackTrace();
			} catch (MoreThanOneOpenSessionException e) {
				e.printStackTrace();
			} catch (DublicateNameSessionException e) {
				e.printStackTrace();
			}

		}
		else
		{
			Toast.makeText(activity, "Can't add session with same name", Toast.LENGTH_LONG).show();
			if(ForegroundService.isRunning())
				activity.stopService(serviceIntent);
			stopChronometer();
		}

		return session;
	}
	
	
}

