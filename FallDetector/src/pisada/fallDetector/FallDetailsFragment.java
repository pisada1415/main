package pisada.fallDetector;



import java.util.Calendar;

import pisada.plotmaker.Data;
import pisada.plotmaker.Plot2d;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FallDetailsFragment extends FallDetectorFragment {
	private Plot2d plot;
	private LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 
	private Calendar c = Calendar.getInstance();
	private Activity activity;
	private String sessionName;
	private long fallTime;
	
	public FallDetailsFragment()
	{
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		plot = new Plot2d(activity, new Data(0,0));
		View view; 
		view = inflater.inflate(R.layout.activity_fall_details, null);
		RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.relative01);

		LinearLayout graphLayout = (LinearLayout) rl.findViewById(R.id.linear01);
		graphLayout.addView(plot, lp);
		
		plot.pushValue(new Data(1,10));
		plot.pushValue(new Data(2,3));
		plot.pushValue(new Data(3,14));
		plot.pushValue(new Data(4,2));
		plot.pushValue(new Data(5,1));
		
		return rl;  
	}
	
	@Override
	public void setSessionName(String s)
	{
		sessionName = s;
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
	
	}
	
	@Override
	public void setFallTime(long time){
		fallTime = time;
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fall_details, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
	
}
