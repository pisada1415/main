package pisada.fallDetector;

import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.recycler.CurrentSessionCardAdapter;
import pisada.recycler.SessionDetailsCardAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SessionDetailsFragment extends FallDetectorFragment {
	String sessionName;	
	RecyclerView rView;
	SessionDetailsCardAdapter cardAdapter;
	Activity activity;
	LayoutManager mLayoutManager;
	FallDataSource fallDataSource;
	SessionDataSource sessionData;
	SessionDataSource.Session session;
	
	public SessionDetailsFragment()
	{
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.activity_session, container, false);  
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		sessionData = new SessionDataSource(activity);
		session = sessionData.getSession(sessionName);
		rView=(RecyclerView) getView().findViewById(R.id.session_details_recycler);
		rView.setHasFixedSize(true);
		cardAdapter = new SessionDetailsCardAdapter(activity, new SessionDataSource(activity), sessionName);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(activity);
		rView.setLayoutManager(mLayoutManager);
		/*
		 * qui aggiungere le fall. dovremmo avere tutto TODO
		 */
		
		if(fallDataSource == null)
			fallDataSource = new FallDataSource(activity);
		ArrayList<FallDataSource.Fall> falls = fallDataSource.sessionFalls(session);
		if(falls != null) //OCCHIO POTREBBE NASCONDERE PROBLEMI
			for(int i = falls.size()-1; i >= 0; i--){
				cardAdapter.addFall(falls.get(i));
			}
	}

	@Override
	public void onAttach(Activity a){
		super.onAttach(a);
		activity = a;
		if(sessionName!= null)
			a.setTitle(sessionName);
	}
	

	@Override
	public void setSessionName(String s) {
		sessionName = s;
	}
	
}
