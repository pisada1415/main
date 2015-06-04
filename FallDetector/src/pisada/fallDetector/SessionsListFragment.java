package pisada.fallDetector;


import pisada.database.FallDataSource.Fall;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;
import fallDetectorException.BoolNotBoolException;



public class SessionsListFragment extends FallDetectorFragment implements ServiceReceiver {

	//private RecyclerView rView;
	private static SessionListCardAdapter cardAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	public static int counter;
	private static SessionDataSource sessionData;
	Intent serviceIntent;
	Activity activity;
	private ImageView FAB;
	private final int TYPE = 1;
	private boolean existSelected=false;

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

		cardAdapter=new SessionListCardAdapter(activity, rView, this);
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
		ForegroundService.connect(this);
	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.clear();
		inflater.inflate(R.menu.sessions_list, menu);
		if(existSelected){
			menu.findItem(R.id.delete_bar).setVisible(true);
			menu.findItem(R.id.archive_bar).setVisible(true);
		}

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
	@Override
	public void serviceUpdate(float x, float y, float z, long time) {
		// TODO Auto-generated method stub

	}
	@Override
	public void serviceUpdate(Fall fall, String sessionName) {
		rView.getAdapter().notifyItemChanged(0);

	}
	@Override
	public void sessionTimeOut() {
		// TODO Auto-generated method stub

	}
	@Override
	public boolean equalsClass(ServiceReceiver obj) {
		if(obj instanceof SessionsListFragment)
			return true;
		return false;
	}
	@Override
	public void runOnUiThread(Runnable r) {


	}

	public void existSelectedItem(boolean existSelected){
		this.existSelected=existSelected;
		getActivity().invalidateOptionsMenu();

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.delete_bar:
			((SessionListCardAdapter) rView.getAdapter()).deleteSelectedSession();
			return true;
		case R.id.archive_bar:
			((SessionListCardAdapter) rView.getAdapter()).archiveSelectedSession();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}



