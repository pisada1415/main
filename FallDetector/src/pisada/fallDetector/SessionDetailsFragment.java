package pisada.fallDetector;

import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.recycler.CurrentSessionCardAdapter;
import pisada.recycler.SessionDetailsCardAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

/*
 * mettere menu session
 * sovrascrivere session rename
 */
public class SessionDetailsFragment extends FallDetectorFragment {
	String sessionName;	
	RecyclerView rView;
	SessionDetailsCardAdapter cardAdapter;
	Activity activity;
	LayoutManager mLayoutManager;
	FallDataSource fallDataSource;
	SessionDataSource sessionData;
	SessionDataSource.Session session;
	private int TYPE = -1;
	
	public SessionDetailsFragment()
	{
		setHasOptionsMenu(true);
	}
	public int getType()
	{
		return this.TYPE;
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
		sessionName = getArguments().getString(Utility.SESSION_NAME_KEY);
		activity.setTitle(sessionName);
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
	}
	

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.session, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		

		int id = item.getItemId();

		switch (id) {
				case R.id.rename_session:
		{
			// Set an EditText view to get user input 
			final EditText input = new EditText(activity);

			new AlertDialog.Builder(activity)
			.setTitle("Rename")
			.setMessage("Insert name")
			.setView(input)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					
					String value = input.getText().toString(); 
					

					if(!sessionData.existSession(value)){
						sessionData.renameSession(session, value);
						activity.setTitle(value);
						session = sessionData.getSession(value);
						sessionName = value;
					}
					
					else
					{
						Toast.makeText(activity, "Can't add session with same name!", Toast.LENGTH_LONG).show();
						activity.setTitle(value);
					}

				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			}).show();

		}
		return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
