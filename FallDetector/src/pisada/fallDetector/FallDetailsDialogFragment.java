package pisada.fallDetector;

import java.util.ArrayList;
import java.util.Calendar;

import pisada.database.Acquisition;
import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.plotmaker.Data;
import pisada.plotmaker.Plot;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FallDetailsDialogFragment extends DialogFragment {
	private Plot plot;
	private LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 
	private Calendar c = Calendar.getInstance();
	private String sessionName;
	private Long fallTime;
	private FallDataSource fds;
	private FallDataSource.Fall fall;
	private SessionDataSource sds;
	private SessionDataSource.Session session;
	private ImageView thumbNail;
	private TextView info2, info3, info_date, info_time;
	private final int TYPE = -2;
	private ProgressBar pb;
	private final String fallTimeKey = "FALL_TIME";
	private final String sessionNameKey = "SESSION_NAME";


	public FallDetailsDialogFragment(){
		super();
	}

	public FallDetailsDialogFragment(String name, long time){
		sessionName = name;
		fallTime = time;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if(savedInstanceState != null)
		{
			sessionName = savedInstanceState.getString(this.sessionNameKey);
			fallTime = savedInstanceState.getLong(this.fallTimeKey);
		}
		Activity act = getActivity();
		fds = new FallDataSource(act);
		sds = new SessionDataSource(act);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyDialog);
		// otteniamo layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Passa null come parent view perché andrà nel layout del dialog
		View view = inflater.inflate(R.layout.dialog_fall_details, null);
		/*
		 * parte presa dall'ex activity-fragment
		 */
		plot = new Plot(getActivity(), new Data(0,0));

		LinearLayout rl = (LinearLayout) view.findViewById(R.id.linear01);
		LinearLayout graphLayout = (LinearLayout) rl.findViewById(R.id.graphView);
		graphLayout.addView(plot, lp);
		thumbNail = (ImageView)view.findViewById(R.id.thumbNailFallDetails);
		info_date = (TextView)view.findViewById(R.id.infoFallDateValue);
		info_time= (TextView)view.findViewById(R.id.infoFallTimeValue);
		info2 = (TextView)view.findViewById(R.id.infoFall02);
		info3 = (TextView)view.findViewById(R.id.infoFall03);
		pb = (ProgressBar)view.findViewById(R.id.progressBarFallDetails);




		new Thread() //apertura database con timeout 
		{
			@Override
			public void run()
			{

				hideUI();

				fall = fds.getFall(fallTime, sessionName);
				session = sds.getSession(sessionName);
				if(session == null){((MainActivity)getActivity()).switchFragment(new Intent(getActivity(),SessionsListFragment.class));return;} //viene chiamata se android chiude la classe per mancanza di memoria, in questo caso viene perso per strada il parametro name, questa riga fa ritornare alla home

				final Bitmap picture = Utility.createImage(session.getID());
				showUI();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						thumbNail.setImageBitmap(picture); 
						Resources res = getActivity().getResources();
						String stringInfoDate = Utility.getStringDate(fallTime);
						String stringInfoTime = Utility.getStringHour(fallTime);

						String stringInfo2 = res.getString(R.string.Position);
						double lat = fall.getLat(), lng = fall.getLng();

						if(lat!=-1 && lng != -1){
							String latStr = ("" + fall.getLat()).substring(0,6)+"&#133;";
							String lngStr = (""+fall.getLng()).substring(0,6)+"&#133;";
							stringInfo2 +=  latStr + " ," + lngStr;
						}
						else
							stringInfo2 += res.getString(R.string.notavailable);
						String stringInfo3 = "";
						if(fall.wasNotified()){
							stringInfo3 += res.getString(R.string.notifSentCorrectly);
							info3.setTextColor(getResources().getColor(R.color.darkGreen));
						}
						else{
							stringInfo3 += res.getString(R.string.notifNotSent);
							info3.setTextColor(Color.RED);
						}
						info_date.setText(stringInfoDate);
						info_time.setText(stringInfoTime);
						//info2.setText(stringInfo2);
						info3.setText(stringInfo3);

						String link = Utility.getMapsLink(fall.getLat(), fall.getLng());

						if(link != null){
							info2.setText(Html.fromHtml("<a href=\""+ link + "\">" + stringInfo2 + "</a>"));

							info2.setClickable(true);
							info2.setMovementMethod (LinkMovementMethod.getInstance());
						}
						else{
							info2.setText(stringInfo2);
							info2.setClickable(false);
						}
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



		//inflate e setta il layout al dialog
		builder.setView(view); 
		return builder.create();
	}

	
	private void hideUI()
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				thumbNail.setVisibility(View.GONE);
				info_date.setVisibility(View.GONE);
				info_time.setVisibility(View.GONE);
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
				info_date.setVisibility(View.VISIBLE);
				info_time.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			}
		});
	}

	Handler mHandler;
	public void runOnUiThread(Runnable r){
		if(mHandler == null)
			mHandler = new Handler(Looper.getMainLooper());
		mHandler.post(r);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(this.fallTimeKey, fallTime);
		outState.putString(this.sessionNameKey, sessionName);


	}
}
