package pisada.recycler;


import java.util.ArrayList;
import java.util.Random;

import fallDetectorException.DublicateNameSessionException;
import pisada.database.FallSqlHelper;
import pisada.database.SessionDataSource;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.CurrentSessionActivity;
import pisada.fallDetector.R;
import pisada.fallDetector.SessionDetailsActivity;
import pisada.fallDetector.SessionsListActivity;
import pisada.fallDetector.Utility;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class SessionListCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private ArrayList<Session> sessionList;
	private static SessionsListActivity activity;
	private static SessionDataSource sessionData;
	private RecyclerView rView;


	public static class OldSessionHolder extends RecyclerView.ViewHolder {
		private TextView vName;
		private Button btn;
		private CardView card;
		public OldSessionHolder(View v) {
			super(v);

			vName =  (TextView) v.findViewById(R.id.nameText);
			btn=(Button) v.findViewById(R.id.old_details_button);
			card=(CardView) v;

		}
	}


	public  class NewSessionHolder extends RecyclerView.ViewHolder {
		private TextView newSessionText;
		private Button addSessionButton;
		private EditText typeSession;
		private CardView card;


		@SuppressLint("NewApi")
		public NewSessionHolder(View v) {
			super(v);
			card=(CardView) v;
			this.newSessionText=(TextView) card.findViewById(R.id.new_session_text);
			this.addSessionButton=(Button) card.findViewById(R.id.add_session_button);
			this.typeSession=(EditText) card.findViewById(R.id.type_session);
			Random random=new Random();
			addSessionButton.setBackground(new BitmapDrawable(activity.getResources(),Utility.createImage((int)random.nextLong()%150)));
			if(sessionData.existCurrentSession()){
				//v.setLayoutParams(new LayoutParams(v.getWidth(),0));
			}

		}

	}

	public static class CurrentSessionHolder extends RecyclerView.ViewHolder {
		private TextView sessionName;
		private TextView sessionStart;
		private Button detailsButton;
		private CardView card;

		public CurrentSessionHolder(View v) {
			super(v);
			sessionName=(TextView) v.findViewById(R.id.current_session_name_text);
			sessionStart=(TextView) v.findViewById(R.id.current_session_start_text);
			detailsButton=(Button) v.findViewById(R.id.details_current_button);
			card=(CardView) v;
			if(!sessionData.existCurrentSession()){
			//	v.setLayoutParams(new LayoutParams(v.getWidth(),0));
			}
		}

	}


	public SessionListCardAdapter(SessionsListActivity activity, RecyclerView rView) {

		this.activity=activity;
		this.sessionData=new SessionDataSource(activity);

		this.sessionList=sessionData.sessions();
		sessionList.add(0,new Session());
		if(!sessionData.existCurrentSession()){
			sessionList.add(1,new Session());
		}
		this.rView=rView;
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
			NewSessionHolder Nholder=(NewSessionHolder) holder;
			if(currSession!=null){
				Nholder.card.setVisibility(CardView.GONE);
			}
			else{
				Nholder.card.setVisibility(CardView.VISIBLE);

			}
			return;

		case 1:
			CurrentSessionHolder cHolder=(CurrentSessionHolder) holder;
			if(currSession!=null){
				cHolder.card.setVisibility(View.VISIBLE);
				cHolder.sessionName.setText(currSession.getName()+"\n Close: "+currSession.booleanIsClose());
				cHolder.sessionStart.setText(String.valueOf(currSession.getStartTime()).toString());

			}
			else{
				cHolder.card.setVisibility(View.GONE);
			}
			return;
		}

		OldSessionHolder Oholder=(OldSessionHolder) holder;
		final Session session = sessionList.get(i);
		Oholder.vName.setText("Name: "+session.getName()+"\nStart Time: "+session.getStartTime()+"\nendTime: "+session.getEndTime()+"\n Close: "+session.booleanIsClose()+"\n Duration: "+sessionData.sessionDuration(session));
		Oholder.btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent=new Intent(activity,SessionDetailsActivity.class);
				intent.putExtra(FallSqlHelper.SESSION_NAME, session.getName());
				activity.startActivity(intent);
			}
		});
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {


		Session currSession=sessionData.currentSession();

		if(type==0) return new NewSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_new_session_card, viewGroup, false));

		if(type==1)return new CurrentSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_curr_session_card,viewGroup,false));

		return new OldSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.old_session_card, viewGroup, false));

	}

	//AGGIUNGE NUOVA SESSIONE ALL'ADAPTER, SENZA STORE NEL DATABASE. STORE DA FARE FUORI PRIMA
	public void addNewSession(String name,String img,long startTime) throws DublicateNameSessionException {

		if(sessionData.existCurrentSession()){
			sessionData.closeSession(sessionData.currentSession());
			sessionList.add(1,sessionData.openNewSession(name, img, startTime));
		}

		else{
			sessionList.set(1,sessionData.openNewSession(name, img, startTime));
			notifyItemChanged(0);
		}


	}

	//CHIUDE SESSIONE CORRENTE APPOGGIANDOSI AL METODO DI SESSIONDATASOURCE
	public void closeCurrentSession(){
		Session currSession=sessionList.get(1);
		if(currSession.isValidSession()) {
			sessionData.closeSession(currSession);
			sessionList.add(1,new Session());
		}

	}
	
	public void check(){
		sessionList=sessionData.sessions();
		sessionList.add(0,new Session());
		if(!sessionData.existCurrentSession()){
			sessionList.add(0, new Session());
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

