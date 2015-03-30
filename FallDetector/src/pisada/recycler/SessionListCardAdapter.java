package pisada.recycler;


import java.util.ArrayList;

import pisada.database.AcquisitionDataSource;
import pisada.database.SessionDataSource;
import pisada.fallDetector.Acquisition;
import pisada.fallDetector.R;
import pisada.fallDetector.Session;
import pisada.fallDetector.SessionsListActivity;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
public class SessionListCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private ArrayList<Session> sessionList;
	private SessionsListActivity activity;
	private SessionDataSource sessionData;


	public static class OldSessionHolder extends RecyclerView.ViewHolder {
		private TextView vName;
		private TextView vAdjective;
		public OldSessionHolder(View v) {
			super(v);
			vName =  (TextView) v.findViewById(R.id.name_text);
			vAdjective = (TextView)  v.findViewById(R.id.adjective_text);

		}
	}


	public  class NewSessionHolder extends RecyclerView.ViewHolder {
		private TextView newSessionText;
		private Button addSessionButton;
		private EditText typeSession;


		public NewSessionHolder(View v) {
			super(v);
			newSessionText=(TextView) v.findViewById(R.id.new_session_text);
			addSessionButton=(Button) v.findViewById(R.id.add_session_button);
			typeSession=(EditText) v.findViewById(R.id.type_session);
			sessionData.open();
		//	v.setLayoutParams(new LayoutParams(v.getWidth(),0));
		}

	}
	
	/*	public static class CurrentSessionHolder extends RecyclerView.ViewHolder {
	private TextView sessionName;
	private TextView sessionStart;

	public CurrentSessionHolder(View v) {
		super(v);
		sessionName=(TextView) v.findViewById(R.id.current_session_name_text);
		sessionStart=(TextView) v.findViewById(R.id.current_session_start_text);
	}

}*/





	public SessionListCardAdapter(SessionsListActivity activity) {

		this.activity=activity;
		this.sessionData=new SessionDataSource(activity);
		sessionData.open();
		
		this.sessionList=sessionData.sessions();
		/*if(!sessionData.existCurrentSession()){
			sessionList.add(0, new Session());
		}*/
		
		//Aggiungo sessione vuota all'indice 0 per far quadrare l'adapter
		sessionList.add(0,new Session());
		sessionData.close();
	}

	@Override
	public int getItemCount() {
		return sessionList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int i) {

		sessionData.open();
		Session currSession=sessionData.currentSession();
		sessionData.close();
		if(i==0){
			/*if(currSession==null){
				//NewSessionHolder nHolder=(NewSessionHolder) holder;
				//nHolder.
			}
			else{
				CurrentSessionHolder cHolder=(CurrentSessionHolder) holder;
				cHolder.sessionName.setText(currSession.name().toString());
				cHolder.sessionStart.setText(String.valueOf(currSession.startTime()).toString());
			}*/


		}
		else{
			OldSessionHolder Oholder=(OldSessionHolder) holder;
			Session session = sessionList.get(i);
			Oholder.vName.setText("Name: "+session.name()+"\nStart Time: "+session.startTime()+"\nIs Close: "+session.integerIsClose());
			Oholder.vAdjective.setText("culo");
			
		}

	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

		sessionData.open();
		Session currSession=sessionData.currentSession();
		sessionData.close();
		if(type==0){
			/*if(currSession==null)return new NewSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_new_session_card, viewGroup, false));
			else return new CurrentSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_curr_session_card,viewGroup,false));
		*/
			return new NewSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_new_session_card, viewGroup, false));
		}
		else return new OldSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.old_session_card, viewGroup, false));

	}
	public void addItem(Session s) {
		this.sessionList.add(1,s);
		 notifyItemInserted(1);

	}
	public void addItemOnNewSession(Session session){
		this.sessionList.remove(0);
		this.sessionList.add(0,session);
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		if(position==0) return 0;
		return 1;
	}


}

