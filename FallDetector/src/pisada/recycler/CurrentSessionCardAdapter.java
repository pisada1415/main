package pisada.recycler;


import java.util.ArrayList;
import java.util.Calendar;

import pisada.database.FallDataSource;
import pisada.database.FallDataSource.Fall;
import pisada.database.SessionDataSource;
import pisada.fallDetector.ForegroundService;
import pisada.fallDetector.FragmentCommunicator;
import pisada.fallDetector.R;
import pisada.fallDetector.ServiceReceiver;
import pisada.fallDetector.Utility;
import pisada.plotmaker.Data;
import pisada.plotmaker.Plot2d;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
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
public class CurrentSessionCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ServiceReceiver {

	private static ArrayList<FallDataSource.Fall> cardContentList;
	private Activity activity;


	private double last;
	private static Plot2d graph;
	private Calendar c;
	private long millisecStartGraph;
	private static Chronometer duration; 
	private static long timeSessionUp;
	private static long timeWhenPaused = 0;
	private String infoText;
	private SharedPreferences sp;
	private Bitmap bitmapThumbNailCurrent;

	private TextView info;

	private static boolean startChronometerOnStart = false;

	private final String CONTACTS_KEY = "contacts";

	private static String currentSessionName;
	private SessionDataSource sds;
	private ProgressBar pb;
	private ImageView fallThumbnail;
	private ImageView thumbNailCurrent;
	private SessionDataSource.Session session;
	/*
	 * 
	 * first_new_currentsession_card
	 */
	public class FirstCardHolder extends RecyclerView.ViewHolder {

		

		private Button playPause;
		
		
		@SuppressWarnings("deprecation")
		public FirstCardHolder(View v) {
			super(v);
			playPause = (Button) v.findViewById(R.id.start_pause_button);
			duration = (Chronometer) v.findViewById(R.id.chronometer);
			thumbNailCurrent = (ImageView)v.findViewById(R.id.thumbnail);
			info =  (TextView) v.findViewById(R.id.info);
			pb = (ProgressBar)v.findViewById(R.id.progressBarFirstCard);
			pb.setVisibility(View.VISIBLE);
			if(infoText != null)
				info.setText(infoText);
			else
				info.setVisibility(View.GONE);
			if(bitmapThumbNailCurrent != null)
				thumbNailCurrent.setImageBitmap(bitmapThumbNailCurrent);
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

			graph = new Plot2d(activity, new Data(timePoint, 0));
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
		//TODO notifica mandata correttamente o no
		public FallsHolder(View v) {
			super(v);
			fallThumbnail=(ImageView) v.findViewById(R.id.thumbnail_fall);
			fallTime=(TextView) v.findViewById(R.id.fall_time);
			fallPosition=(TextView) v.findViewById(R.id.position);
			boolNotif = (TextView) v.findViewById(R.id.booleanSent);
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int position = getAdapterPosition();
					Intent intent = new Intent(activity, pisada.fallDetector.FallDetailsFragment.class);
					long time = cardContentList.get(position).getTime();
					intent.putExtra(Utility.FALL_TIME_KEY, time);
					intent.putExtra(Utility.SESSION_NAME_KEY, currentSessionName);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
					((FragmentCommunicator)activity).switchFragment(intent);
					//Toast.makeText(activity, "premuta caduta " + cardContentList.get(position).getTime(), Toast.LENGTH_SHORT).show();

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
			fallThumbnail.setImageBitmap(Utility.createImage(session.getID()));
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
		if(/*graphX != null && graphY != null && graphZ != null*/graph != null){
			long timeGraph = (time - millisecStartGraph);

			graph.pushValue(new Data(timeGraph, last));
			graph.invalidate();
			/*graphX.pushValue(new Data(timeGraph,x));
		graphX.invalidate();
		graphY.pushValue(new Data(timeGraph,y));
		graphY.invalidate();
		graphZ.pushValue(new Data(timeGraph,z));
		graphZ.invalidate();
			 */
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
			thumbNailCurrent.setImageBitmap(Utility.createImage(session.getID()));
			thumbNailCurrent.setVisibility(View.VISIBLE);
		}
		else if(thumbNailCurrent != null)
			thumbNailCurrent.setVisibility(View.GONE);
		if(thumbNailCurrent == null && session != null)
			bitmapThumbNailCurrent = Utility.createImage(session.getID());


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
	
	
}

