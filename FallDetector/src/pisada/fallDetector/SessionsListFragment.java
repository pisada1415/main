package pisada.fallDetector;


import pisada.database.SessionDataSource;
import pisada.recycler.SessionListCardAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;
import fallDetectorException.BoolNotBoolException;



public class SessionsListFragment extends FallDetectorFragment {

	//private RecyclerView rView;
	private static SessionListCardAdapter cardAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	public static int counter;
	private static SessionDataSource sessionData;
	Intent serviceIntent;
	Activity activity;
	private ImageView FAB;
	private final int TYPE = 1;


	public SessionsListFragment()
	{
		setHasOptionsMenu(true);
	}
	public int getType()
	{
		return this.TYPE;
	}
	@Override
	public View onCreateView(android.view.LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_sessions_list, container, false);
	};


	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		activity = a;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		//APRO CONNESSIONI AL DATABASE
		sessionData=new SessionDataSource(activity);

		//INIZIALIZZO RECYCLERVIEW

		rView=(RecyclerView) getView().findViewById(R.id.session_list_recycler);

		cardAdapter=new SessionListCardAdapter(activity, rView);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(activity);
		rView.setLayoutManager(mLayoutManager);
		rView.setItemAnimator(new DefaultItemAnimator());
		this.scroll(MainActivity.sessionsListFragmentLastIndex);
		FAB=(ImageView) activity.findViewById(R.id.FAB);
		if(sessionData.existCurrentSession()) FAB.setVisibility(View.GONE);
		else FAB.setVisibility(View.VISIBLE);
		FAB.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(activity,CurrentSessionFragment.class);
				((FragmentCommunicator)activity).switchFragment(intent);
			}
		});
	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.clear();
		inflater.inflate(R.menu.sessions_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	public void onResume(){
		super.onResume();
		sessionData.open();
		cardAdapter.check();
	}


	@Override
	public void addSession(View v) throws BoolNotBoolException{

		Intent toSamu = new Intent(activity, CurrentSessionFragment.class);
		((FragmentCommunicator)activity).switchFragment(toSamu);

	}
	@Override
	public void currentSessionDetails(View v){

		this.addSession(v);
	}

}



