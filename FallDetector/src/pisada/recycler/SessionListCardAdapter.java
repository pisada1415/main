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
import android.support.v7.widget.RecyclerView.LayoutManager;
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
	private static SessionDataSource sessionData;
	private RecyclerView rView;


	public static class OldSessionHolder extends RecyclerView.ViewHolder {
		private TextView vName;
		public OldSessionHolder(View v) {
			super(v);
			vName =  (TextView) v.findViewById(R.id.nameText);
		}
	}


	public  class NewSessionHolder extends RecyclerView.ViewHolder {
		private TextView newSessionText;
		private Button addSessionButton;
		private EditText typeSession;


		public NewSessionHolder(View v) {
			super(v);
			this.newSessionText=(TextView) v.findViewById(R.id.new_session_text);
			this.addSessionButton=(Button) v.findViewById(R.id.add_session_button);
			this.typeSession=(EditText) v.findViewById(R.id.type_session);
			if(sessionData.existCurrentSession()){
				v.setLayoutParams(new LayoutParams(v.getWidth(),0));
			}

		}

	}

	public static class CurrentSessionHolder extends RecyclerView.ViewHolder {
		private TextView sessionName;
		private TextView sessionStart;

		public CurrentSessionHolder(View v) {
			super(v);
			sessionName=(TextView) v.findViewById(R.id.current_session_name_text);
			sessionStart=(TextView) v.findViewById(R.id.current_session_start_text);
			if(!sessionData.existCurrentSession()){
				v.setLayoutParams(new LayoutParams(v.getWidth(),0));
			}
		}

	}


	public SessionListCardAdapter(SessionsListActivity activity, RecyclerView rView) {

		this.activity=activity;
		this.sessionData=new SessionDataSource(activity);
		this.rView=rView;
		sessionData.open();

		this.sessionList=sessionData.sessions();
		sessionList.add(0,new Session());

		if(sessionData.existCurrentSession()){
			sessionList.set(1, sessionData.currentSession());
		}
		else sessionList.add(1,new Session());

	}

	@Override
	public int getItemCount() {
		return sessionList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int i) {

		Session currSession=sessionData.currentSession();
		switch(i) {
		case 0: 
			if(sessionData.existCurrentSession()){
				holder.itemView.setLayoutParams(new LayoutParams(holder.itemView.getWidth(),0));
				holder.itemView.invalidate();
			}
			return;

		case 1:
			if(currSession!=null){
				CurrentSessionHolder cHolder=(CurrentSessionHolder) holder;
				cHolder.sessionName.setText(currSession.name()+"\n Close: "+currSession.booleanIsClose());
				cHolder.sessionStart.setText(String.valueOf(currSession.startTime()).toString());
			}
			return;
		}

		OldSessionHolder Oholder=(OldSessionHolder) holder;
		Session session = sessionList.get(i);
		Oholder.vName.setText("Name: "+session.name()+"\nStart Time: "+session.startTime()+"\nendTime: "+session.endTime()+"\n Close: "+session.booleanIsClose());

	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {


		Session currSession=sessionData.currentSession();

		if(type==0) return new NewSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_new_session_card, viewGroup, false));

		if(type==1)return new CurrentSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_curr_session_card,viewGroup,false));

		return new OldSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.old_session_card, viewGroup, false));

	}

	//AGGIUNGE NUOVA SESSIONE ALL'ADAPTER, SENZA STORE NEL DATABASE. STORE DA FARE FUORI PRIMA
	public void addNewSession(Session s) {
		Session currSession=sessionList.get(1);

		if(currSession.isValidSession()){
			sessionData.closeSession(currSession);
			sessionList.add(1,s);
			notifyItemInserted(1);
		}
		else{
			sessionList.set(1,s);
			notifyItemInserted(1);

		}



	}

	//CHIUDE SESSIONE CORRENTE APPOGGIANDOSI AL METODO DI SESSIONDATASOURCE
	public void closeCurrentSession(){
		Session currSession=sessionList.get(1);
		if(currSession.isValidSession()) {
			sessionData.closeSession(currSession);
			sessionList.add(1,new Session());
			notifyItemInserted(1);

		}
	}

	@Override
	public int getItemViewType(int position) {

		switch(position){
		case 0: return 0;
		case 1: return 1;
		}
		return 3;

	}





}

