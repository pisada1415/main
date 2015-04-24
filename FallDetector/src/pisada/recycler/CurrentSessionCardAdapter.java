package pisada.recycler;


import java.util.ArrayList;
import java.util.Calendar;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.fallDetector.ForegroundService;
import pisada.fallDetector.R;
import pisada.fallDetector.ServiceReceiver;
import pisada.fallDetector.Utility;
import pisada.plotmaker.Data;
import pisada.plotmaker.Plot2d;
import android.app.Activity;
import android.content.SharedPreferences;
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
import android.widget.TextView;
public class CurrentSessionCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ServiceReceiver {

	private static ArrayList<CardContent> cardContentList;
	private Activity activity;

	private ArrayList<Double> lastFallThumbnailData;
	private String lastFallTime;
	private String lastFallPosition;

	private long time;
	private double /*lastX, lastY, lastZ,*/ last;
	private static Plot2d /*graphX, graphY, graphZ,*/ graph;
	private Calendar c;
	private long millisecStartGraph;
	private static Chronometer duration; 
	private static long timeSessionUp;
	private static long timeWhenPaused = 0;
	
	private SharedPreferences sp;
	
	private static boolean startChronometerOnStart = false;
	
	private final String CONTACTS_KEY = "contacts";
	/*
	 * 
	 * first_new_currentsession_card
	 */
	public class FirstCardHolder extends RecyclerView.ViewHolder {
		private Button startPauseButton;
		private Button stopButton;
		private ImageView thumbNail;
		private TextView info;
		
		public FirstCardHolder(View v) {
			super(v);
			startPauseButton = (Button)v.findViewById(R.id.start_pause_button);
			stopButton = (Button)v.findViewById(R.id.stop_button);
			duration = (Chronometer) v.findViewById(R.id.chronometer);
			thumbNail = (ImageView)v.findViewById(R.id.thumbnail);
			info =  (TextView) v.findViewById(R.id.info);
			if(startChronometerOnStart)
				startChronometer();
			if(timeWhenPaused != 0)
			{
				duration.setBase(SystemClock.elapsedRealtime() - timeWhenPaused);
				duration.start();
				duration.stop();
				timeWhenPaused = 0;
			}
			//prendi valore start session dal database (qui uso un valore esempio)
			
			
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
	public  class FallsHolder extends RecyclerView.ViewHolder {
		private ImageView fallThumbnail;
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
		}

	}



	public CurrentSessionCardAdapter(Activity activity, long time, boolean startChron, long pauseTime) {

		this.activity=activity;
		c = Calendar.getInstance();
		if(!ForegroundService.isConnected(this))
			ForegroundService.connect(this);
		millisecStartGraph = c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND);
		cardContentList = new ArrayList<CardContent>();
		cardContentList.add(0,new CardContent());
		cardContentList.add(1, new CardContent());
		timeSessionUp = time;
		startChronometerOnStart = startChron;
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
			FirstCardHolder fch = (FirstCardHolder) holder;
			fch.thumbNail.setImageBitmap(Utility.createImage(Utility.randInt(2, 100)));
		}
		else if(i == 1){}
		else{
			/*
			 * TODO qui anziché randint va passato il numero della sessione cui la fall fa riferimento
			 */
			CardContent fall = cardContentList.get(i);
			FallsHolder Oholder=(FallsHolder) holder;
			Oholder.fallThumbnail.setImageBitmap(Utility.createImage(Utility.randInt(2, 100)));
			String link = fall.getLink();
			if(link != null){
				Oholder.fallPosition.setText(Html.fromHtml("<a href=\""+ link + "\">" + "Position: " + fall.getPos() + "</a>"));
				
				Oholder.fallPosition.setClickable(true);
				Oholder.fallPosition.setMovementMethod (LinkMovementMethod.getInstance());
			}
			else{
				Oholder.fallPosition.setText("Position: " + fall.getPos());
				Oholder.fallPosition.setClickable(false);
			}
			
			//Oholder.fallPosition.setText("Position: "+ fall.getPos());
			Oholder.fallTime.setText("Time: " + fall.getTime());
			if(fall.notifiedSuccess()){
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
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {


		if(type==0){
			return new FirstCardHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_new_currentsession_card, viewGroup, false));
		}
		if(type==1)
		{
			return new SecondCardHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plots_card, viewGroup, false));

		}
		else return new FallsHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fall_card, viewGroup, false));

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
		
		this.time = time;

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
	public void pauseChronometer()
	{
		timePause = duration.getBase() - SystemClock.elapsedRealtime();

		duration.stop();
	}
	public static void startChronometer()
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
	
	private void addFallToCardList(String position, String link, String time, boolean b)
	{
		CardContent cc = new CardContent(position,link,time, b);
		if(!cardContentList.contains(cc)){
			cardContentList.add(cc);
			notifyItemInserted(cardContentList.size()-1);
		}
		else
		{
			int i = 0;
			for(; i < cardContentList.size() && !cardContentList.get(i).equals(cc); i++);
			cardContentList.set(i, cc);
			notifyItemChanged(i);
			
		}
	}

	@Override
	public void serviceUpdate(String fallPosition, String link, String time, boolean b) {
		addFallToCardList(fallPosition, link, time, b);
	}
	
	public void addFall(FallDataSource.Fall f, SessionDataSource.Session s)
	{
		String time = Utility.getStringTime(f.getTime());
		String position;
		position = (f.getLat() != -1 && f.getLng() != -1) ? "" + f.getLat() + ", " + f.getLng() : "Not available";
		addFallToCardList(position, Utility.getMapsLink(f.getLat(), f.getLng()), time, f.wasNotified());
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
	

}

