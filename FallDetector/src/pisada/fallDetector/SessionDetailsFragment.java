package pisada.fallDetector;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SessionDetailsFragment extends FallDetectorFragment {
	String sessionName;	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.activity_session, container, false);  
	}

	@Override
	public void onAttach(Activity a){
		super.onAttach(a);
		if(sessionName!= null)
			a.setTitle(sessionName);
	}
	
	@Override
	public void playPauseService(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopService(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSessionName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSessionName(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSession(String s) {
		// TODO Auto-generated method stub
		sessionName = s;
	}
	
}
