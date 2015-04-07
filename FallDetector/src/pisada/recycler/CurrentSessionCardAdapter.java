package pisada.recycler;


import java.util.ArrayList;
import java.util.Calendar;

import pisada.fallDetector.ForegroundService;
import pisada.fallDetector.R;
import pisada.fallDetector.ServiceReceiver;
import pisada.plotmaker.Data;
import pisada.plotmaker.Plot2d;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
public class CurrentSessionCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ServiceReceiver {

	private ArrayList<CardContent> cardContentList;
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
	private static boolean startChronometerOnStart = false;
	/*
	 * 
	 * first_new_currentsession_card
	 */
	public static class FirstCardHolder extends RecyclerView.ViewHolder {
		private TextView sessionName;
		private Button startPauseButton;
		private Button stopButton;
		
		private ImageView thumbNail;
		private TextView info;
		
		public FirstCardHolder(View v) {
			super(v);
			sessionName = (TextView)v.findViewById(R.id.session_name);
			startPauseButton = (Button)v.findViewById(R.id.start_pause_button);
			stopButton = (Button)v.findViewById(R.id.stop_button);
			duration = (Chronometer) v.findViewById(R.id.chronometer);
			thumbNail = (ImageView)v.findViewById(R.id.thumbnail);
			info =  (TextView) v.findViewById(R.id.info);
			if(startChronometerOnStart)
				startChronometer();
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
			fallTime=(Button) v.findViewById(R.id.fall_time);
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
		if(timePause != 0)
			duration.setBase(SystemClock.elapsedRealtime() - timePause);
	}



	@Override
	public void onBindViewHolder(ViewHolder holder, int i) {

		if(i==0 || i == 1){ //se sono le prime due non fare niente
			/*if(currSession==null){
				//NewSessionHolder nHolder=(NewSessionHolder) holder;
				//nHolder.
			}
			else{
				CurrentSessionHolder cHolder=(CurrentSessionHolder) holder;
				cHolder.sessionName.setText(currSession.name().toString());
				cHolder.sessionStart.setText(String.valueOf(currSession.startTime()).toString());
			}*/


		}
		else{
			FallsHolder Oholder=(FallsHolder) holder;
			Oholder.fallThumbnail.setImageBitmap(getBitmapFromData(lastFallThumbnailData));
			Oholder.fallPosition.setText("posizione");
			Oholder.fallTime.setText("tempo");

		}

	}

	private Bitmap getBitmapFromData(ArrayList<Double> lastFallThumbnailData2) {
		// TODO Auto-generated method stub
		Bitmap b = Bitmap.createBitmap(100,100,Config.ARGB_4444);
		return b;
	}



	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {


		if(type==0){
			/*if(currSession==null)return new NewSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_new_session_card, viewGroup, false));
			else return new CurrentSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_curr_session_card,viewGroup,false));
			 */
			return new FirstCardHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_new_currentsession_card, viewGroup, false));
		}
		if(type==1)
		{
			return new SecondCardHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plots_card, viewGroup, false));

		}
		else return new FallsHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fall_card, viewGroup, false));

	}
	public void addItem(CardContent c) {//DIVENTERà FALL

		cardContentList.add(c);
		notifyItemInserted(1);

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
	
	
	

}

