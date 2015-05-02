package pisada.fallDetector;


/*
 * test animazioni per far apparire il grafico che si forma. manca slide verticale
 */

import java.util.ArrayList;
import java.util.Calendar;

import pisada.database.Acquisition;
import pisada.database.FallDataSource;
import pisada.plotmaker.Data;
import pisada.plotmaker.Plot2d;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FallDetailsFragment extends FallDetectorFragment {
	private Plot2d plot;
	private LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 
	private Calendar c = Calendar.getInstance();
	private Activity activity;
	private String sessionName;
	private long fallTime;
	private FallDataSource fds;
	private FallDataSource.Fall fall;
	private ImageView thumbNail;
	private TextView info, info2;
	private final int TYPE = -2;
	private ProgressBar pb;

	public FallDetailsFragment()
	{
		setHasOptionsMenu(true);
	}


	public int getType()
	{
		return this.TYPE;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		sessionName = getArguments().getString(Utility.SESSION_NAME_KEY);
		fallTime = getArguments().getLong(Utility.FALL_TIME_KEY);

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		plot = new Plot2d(activity, new Data(0,0));
		View view; 
		view = inflater.inflate(R.layout.fragment_fall_details, container, false);
		RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.relative01);
		LinearLayout graphLayout = (LinearLayout) rl.findViewById(R.id.graphView);
		graphLayout.addView(plot, lp);
		thumbNail = (ImageView)view.findViewById(R.id.thumbNailFallDetails);
		info = (TextView)view.findViewById(R.id.infoFall);
		info2 = (TextView)view.findViewById(R.id.infoFall2);
		pb = (ProgressBar)view.findViewById(R.id.progressBarFallDetails);

		return rl;  
	}



	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		activity = a;
	}


	@Override
	public void onActivityCreated(Bundle savedInstance)
	{
		super.onActivityCreated(savedInstance);
		fds = new FallDataSource(activity);
		new Thread() //apertura database con timeout 
		{
			@Override
			public void run()
			{
				
				hideUI();
				
				fall = fds.getFall(fallTime, sessionName);
					
				showUI();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						thumbNail.setImageBitmap(Utility.createImage(Utility.randInt(0, 100))); //TODO prendere valore da database
						Resources res = activity.getResources();
						String stringInfo = res.getString(R.string.date)+Utility.getStringTime(fallTime)+"\n"+res.getString(R.string.Position);
						double lat = fall.getLat(), lng = fall.getLng();
						if(lat!=-1 && lng != -1)
							stringInfo +=  fall.getLat() + " ," + fall.getLng();
						else
							stringInfo += res.getString(R.string.notavailable);
						String stringInfo2 = "";
						if(fall.wasNotified()){
							stringInfo2 += res.getString(R.string.notifSentCorrectly);
							info2.setTextColor(getResources().getColor(R.color.darkGreen));
						}
						else{
							stringInfo2 += res.getString(R.string.notifNotSent);
							info2.setTextColor(Color.RED);
						}
						info.setText(stringInfo);
						info2.setText(stringInfo2);

					}
				});

				ArrayList<Acquisition> acquisitionList = fds.fallAcquisitions(fall);
				//TODO mettere tempo sessione al posto del tempo totale. per ora counter
				int timeMillis = -500;
				float sizeLeft= 1000; //per arrivare a +500
				int numLeft = 0;
				if(acquisitionList.size()!=0)
					numLeft = acquisitionList.size();
				for(Acquisition a : acquisitionList)
				{
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if(numLeft > 1){
						plot.pushValue(new Data(timeMillis*1000, Math.sqrt(a.getXaxis()*a.getXaxis() + a.getYaxis()*a.getYaxis() + a.getZaxis() * a.getZaxis())));
						timeMillis += ((int)sizeLeft/numLeft);
						sizeLeft -= sizeLeft/numLeft;
						numLeft--;
					}
					else
						plot.pushValue(new Data(500*1000, Math.sqrt(a.getXaxis()*a.getXaxis() + a.getYaxis()*a.getYaxis() + a.getZaxis() * a.getZaxis())));



					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							plot.invalidate();
						}
					});

				}


			}
		}.start();


	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.fall_details, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	private void hideUI()
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				thumbNail.setVisibility(View.GONE);
				info.setVisibility(View.GONE);
				info2.setVisibility(View.GONE);
				pb.setVisibility(View.VISIBLE);

			}
		});
	}

	private void showUI()
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				thumbNail.setVisibility(View.VISIBLE);
				info2.setVisibility(View.VISIBLE);
				info.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			}
		});
	}

	/*
	public void pushValue(Data d)
	{
		graph.pushValue(d);
	}
	public void invalidate()
	{
		graph.invalidate();
	}

	public void clearGraphs()
	{
		if(graph != null)
			graph.clear();
	}


	 */
	Handler mHandler;
	public void runOnUiThread(Runnable r){
		if(mHandler == null)
			mHandler = new Handler(Looper.getMainLooper());
		mHandler.post(r);

	}


}
