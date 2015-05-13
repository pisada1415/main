package pisada.fallDetector;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;


public class InfoFragment extends FallDetectorFragment  {

	private final boolean HASOPTIONSMENU = false;
	private final int TYPE = 4;
	private TextView credits;
	private int counter;
	
	public InfoFragment()
	{
		setHasOptionsMenu(HASOPTIONSMENU);
	}
	
	public int getType()
	{
		return this.TYPE;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.info_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_info, container, false);
		
		credits = ((TextView)v.findViewById(R.id.creditsview));
		credits.setText(Html.fromHtml(getString(R.string.credits)));
		
		return v;  
	}

	@Override
	public void onActivityCreated(Bundle savedInstance)
	{
		super.onActivityCreated(savedInstance);

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onAttach(Activity a){
		super.onAttach(a);
		counter = 0;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	



	
	

}
