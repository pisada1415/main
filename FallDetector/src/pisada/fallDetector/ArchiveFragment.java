package pisada.fallDetector;

/*
 * classe archive semplicissima, inizializza solo l'adapter e la recyclerview
 */

import pisada.recycler.ArchiveCardAdapter;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class ArchiveFragment extends FallDetectorFragment  {

	private Activity activity;
	private final boolean HASOPTIONSMENU = false;
	private final int TYPE = 2;

	public int getType()
	{
		return this.TYPE;
	}

	public ArchiveFragment()
	{
		setHasOptionsMenu(HASOPTIONSMENU);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.archive_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_archive, container, false);  
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
		rView=(RecyclerView) getView().findViewById(R.id.archive_recycler);
		ArchiveCardAdapter cardAdapter=new ArchiveCardAdapter(activity, rView);
		rView.setAdapter(cardAdapter);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
		rView.setLayoutManager(mLayoutManager);
		rView.setItemAnimator(new DefaultItemAnimator());
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
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
