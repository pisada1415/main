package pisada.recycler;


import java.util.ArrayList;
import java.util.Calendar;

import pisada.fallDetector.ForegroundService;
import pisada.fallDetector.R;
import pisada.fallDetector.ServiceReceiver;
import pisada.fallDetector.Utility;
import pisada.plotmaker.Data;
import pisada.plotmaker.Plot2d;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.SystemClock;
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
	private double lastX, lastY, lastZ;
	private static Plot2d graphX, graphY, graphZ;
	private Calendar c;
	private long millisecStartGraph;
	private static Chronometer duration; 
	private static long timeSessionUp;
	private static long timeWhenPaused = 0;
	
	private static boolean startChronometerOnStart = false;
	/*
	 * 
	 * first_new_currentsession_card
	 */
	public static class FirstCardHolder extends RecyclerView.ViewHolder {
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
		private LinearLayout graphXLayout;
		private LinearLayout graphYLayout;
		private LinearLayout graphZLayout;

		/*
		 * qui aggiungiamo sui parametri del costruttore la roba da passare per buttarla nelle card (penso)
		 */

		public SecondCardHolder(View v) {
			super(v);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 

			graphXLayout=(LinearLayout) v.findViewById(R.id.graphx);
			graphYLayout=(LinearLayout) v.findViewById(R.id.graphy);
			graphZLayout=(LinearLayout) v.findViewById(R.id.graphz);

			graphX = new Plot2d(activity, new Data(c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND) - millisecStartGraph,0));
			graphY = new Plot2d(activity, new Data(c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND) - millisecStartGraph,0));
			graphZ = new Plot2d(activity, new Data(c.get(Calendar.MINUTE)*60*1000 + c.get(Calendar.SECOND)*1000+ c.get(Calendar.MILLISECOND) - millisecStartGraph,0));

			graphXLayout.addView(graphX, lp);
			graphYLayout.addView(graphY, lp);
			graphZLayout.addView(graphZ, lp);

		}

	}
	/*
	 * fall cards
	 */
	public  class FallsHolder extends RecyclerView.ViewHolder {
		private ImageView fallThumbnail;
		private TextView fallTime;
		private TextView fallPosition;

		public FallsHolder(View v) {
			super(v);
			fallThumbnail=(ImageView) v.findViewById(R.id.thumbnail_fall);
			fallTime=(TextView) v.findViewById(R.id.fall_time);
			fallPosition=(TextView) v.findViewById(R.id.position);
		}

	}



	public CurrentSessionCardAdapter(Activity activity, long time, boolean startChron, long pauseTime) {

		this.activity=activity;
		c = Calendar.getInstance();
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
	}



	@Override
	public void onBindViewHolder(ViewHolder holder, int i) {

		if(i==0 || i == 1){ //se sono le prime due non fare niente

		}
		else{
			CardContent fall = cardContentList.get(i);
			FallsHolder Oholder=(FallsHolder) holder;
			Oholder.fallThumbnail.setImageBitmap(getBitmapFromData(fall.getThumbnail()));
			String link = fall.getLink();
			Oholder.fallPosition.setText(Html.fromHtml("<a href=\""+ link + "\">" + "Position: " + fall.getPos() + "</a>"));
			if(link != null){
				Oholder.fallPosition.setClickable(true);
			Oholder.fallPosition.setMovementMethod (LinkMovementMethod.getInstance());
			}
			else
				Oholder.fallPosition.setClickable(false);
			//Oholder.fallPosition.setText("Position: "+ fall.getPos());
			Oholder.fallTime.setText("Time: " + fall.getTime());

		}

	}

	private Bitmap getBitmapFromData(double data) {
		// TODO facciamo l'immagine. oppure decidere se metterlo da un'altra parte e memorizzare l'img nel database
		Bitmap b = Bitmap.createBitmap(100,100,Config.ARGB_8888);
		b.eraseColor(android.graphics.Color.GREEN);
		int rand = Utility.randInt(3, 100);
		
		for(int i = 0; i < rand; i++)
		{
			int x =Utility.randInt(0, 99);
			int y = Utility.randInt(0, 99);
			int r =Utility.randomizeToColor(data);
			int g = Utility.randomizeToColor(data);
			int bc = Utility.randomizeToColor(data);
			b.setPixel(x, y, Color.rgb(r, g, bc));
		}
		return b;
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
	public void addItem(CardContent c) {//DIVENTERà FALL

		cardContentList.add(cardContentList.size(),c);
		notifyItemInserted(cardContentList.size()-1);

	}


	@Override
	public int getItemViewType(int position) {
		if(position==0) return 0;
		if(position == 1) return 1;
		return 2;
	}



	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return cardContentList.size();
	}



	@Override
	public void serviceUpdate(float x, float y, float z, long time) {
		// TODO Auto-generated method stub
		lastX = x; lastY = y; lastZ = z; this.time = time;

		c = Calendar.getInstance();
		if(graphX != null && graphY != null && graphZ != null){
		graphX.pushValue(new Data(time - millisecStartGraph,x));
		graphX.invalidate();
		graphY.pushValue(new Data(time- millisecStartGraph,y));
		graphY.invalidate();
		graphZ.pushValue(new Data(time- millisecStartGraph,z));
		graphZ.invalidate();
		}
	}


	public void clearGraphs()
	{
		if(graphX != null && graphY != null && graphZ != null){
			graphX.clear();
			graphY.clear();
			graphZ.clear();
		}
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
	
	private void addFallToCardList(String position, String link, String time, long img)
	{
		cardContentList.add(new CardContent(position,link,time, img));
		notifyItemInserted(cardContentList.size()-1);
		
	}

	@Override
	public void serviceUpdate(String fallPosition, String link, String time, long img) {
		// TODO se arrivano cadute vengono notificate qui
		addFallToCardList(fallPosition, link, time, img);
	}
	
	public void clearFalls()
	{
		while(cardContentList.size()>2)
		{
			cardContentList.remove(2);
			this.notifyItemRemoved(2);
		}
		
	}
	

}

