package pisada.recycler;


import java.util.ArrayList;
import java.util.Random;

import pisada.database.FallSqlHelper;
import pisada.database.SessionDataSource;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.FragmentCommunicator;
import pisada.fallDetector.R;
import pisada.fallDetector.SessionDetailsFragment;
import pisada.fallDetector.Utility;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fallDetectorException.DublicateNameSessionException;


public class SessionListCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

	private ArrayList<Session> sessionList;
	private static Activity activity;
	private static SessionDataSource sessionData;


	public static class OldSessionHolder extends RecyclerView.ViewHolder
	{
		private TextView vName;
		private Button detailsBtn;
		private Button deleteBtn;
		private CardView card;
		public OldSessionHolder(View v) {
			super(v);
			vName =  (TextView) v.findViewById(R.id.nameText);
			detailsBtn=(Button) v.findViewById(R.id.old_details_button);
			deleteBtn=(Button) v.findViewById(R.id.old_delete_button);
			card=(CardView) v;

		}


	}


	public  class NewSessionHolder extends RecyclerView.ViewHolder{
		private TextView newSessionText;
		private Button addSessionButton;
		private EditText typeSession;
		private RelativeLayout rLay;
		private CardView card;


		@SuppressLint("NewApi")
		public NewSessionHolder(View v) {
			super(v);
			card=(CardView) v;
			card.setClickable(true);
			this.newSessionText=(TextView) card.findViewById(R.id.new_session_text);
			this.addSessionButton=(Button) card.findViewById(R.id.add_session_button);
			this.typeSession=(EditText) card.findViewById(R.id.type_session);
			this.rLay=(RelativeLayout) card.findViewById(R.id.new_session_layout);
			Random random=new Random();
			addSessionButton.setBackground(new BitmapDrawable(activity.getResources(),Utility.createImage(Math.abs((int)random.nextLong()%50))));


		}





	}

	public static class CurrentSessionHolder extends RecyclerView.ViewHolder {
		private TextView sessionName;
		private TextView sessionStart;
		private Button detailsButton;
		private ImageView img;
		private RelativeLayout rLay;
		private CardView card;

		@SuppressLint("NewApi")
		public CurrentSessionHolder(View v) {
			super(v);
			sessionName=(TextView) v.findViewById(R.id.current_session_name_text);
			sessionStart=(TextView) v.findViewById(R.id.current_session_start_text);
			detailsButton=(Button) v.findViewById(R.id.details_current_button);
			img=(ImageView) v.findViewById(R.id.current_session_icon);
			int id=0;
			Session s=sessionData.currentSession();
			if(s!=null)id=s.getID();
			img.setBackground(new BitmapDrawable(activity.getResources(),Utility.createImage(id)));
			card=(CardView) v;
			this.rLay=(RelativeLayout) v.findViewById(R.id.curr_card_session_layout);
		}

	}


	public SessionListCardAdapter(final Activity activity, RecyclerView rView) {

		this.activity=activity;
		this.sessionData=new SessionDataSource(activity);

		this.sessionList=sessionData.sessions();
		sessionList.add(0,new Session());
		if(!sessionData.existCurrentSession()){
			sessionList.add(1,new Session());
		}
		rView.addOnItemTouchListener(new TouchListener(activity, rView, new ClickListener() {

			@Override
			public void onLongClick(View view, int position) {
			}

			@Override
			public void onClick(View view, int position) {
				notifyItemChanged(position);
			}
		}));
	}

	@Override
	public int getItemCount() {
		return sessionList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder,  int i) {

		Session currSession=sessionData.currentSession();
		switch(i) {
		case 0: 
			if(sessionData.existCurrentSession())	{
				NewSessionHolder Nholder=(NewSessionHolder) holder;
				Nholder.rLay.setVisibility(View.GONE);
			}
			else{
				NewSessionHolder Nholder=(NewSessionHolder) holder;
				Nholder.rLay.setVisibility(View.VISIBLE);
			}
			return;

		case 1:
			CurrentSessionHolder cHolder=(CurrentSessionHolder) holder;
			if(currSession!=null){
				cHolder.rLay.setVisibility(View.VISIBLE);
				cHolder.sessionName.setText(currSession.getName()+"\n Close: "+currSession.booleanIsClose()+"\n ID= "+ currSession.getID());
				cHolder.sessionStart.setText(String.valueOf(currSession.getStartTime()).toString());
			}
			else{
				CurrentSessionHolder Cholder=(CurrentSessionHolder) holder;
				cHolder.rLay.setVisibility(View.GONE);
			}

			return;
		}

		OldSessionHolder Oholder=(OldSessionHolder) holder;
		final Session session = sessionList.get(i);
		final int j=i;
		Oholder.vName.setText("Name: "+session.getName()+ " ID= "+session.getID());//+"\nStart Time: "+session.getStartTime()//+"\nendTime: "+session.getEndTime()+"\n Close: "+session.booleanIsClose()+"\n Duration: "+sessionData.sessionDuration(session));
		Oholder.detailsBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(activity,SessionDetailsFragment.class);
				intent.putExtra(Utility.SESSION_NAME_KEY, session.getName());
				((FragmentCommunicator)activity).switchFragment(intent);
			}
		});
		Oholder.deleteBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//sessionData.deleteSession(session);
			
				notifyItemRemoved(j);
				sessionList.remove(j);
		



			}

		});

	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {


		Session currSession=sessionData.currentSession();

		if(type==0) {
			View v=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_new_session_card, viewGroup, false);
			if(sessionData.existCurrentSession())v.setLayoutParams(new LayoutParams(v.getWidth(),0));
			return new NewSessionHolder(v);
		}

		if(type==1){
			View v=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_current_session_sessions_list_card, viewGroup, false);
			if(!sessionData.existCurrentSession())v.setLayoutParams(new LayoutParams(v.getWidth(),0));
			return new CurrentSessionHolder(v);
		}

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


	class TouchListener implements RecyclerView.OnItemTouchListener{

		GestureDetector gDetector;
		private ClickListener listener;


		public TouchListener(Context context,final RecyclerView recycler, final ClickListener listener ) {

			this.listener=listener;
			gDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){


				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					super.onSingleTapUp(e);
					View card=recycler.findChildViewUnder(e.getX(),e.getY());
					if(card!=null&&listener!=null){
						//	listener.onClick(card, recycler.getChildPosition(card));
					}

					return false;
				}

				@Override
				public void onLongPress(MotionEvent e) {

					// TODO Auto-generated method stub
					super.onLongPress(e);

					View card=recycler.findChildViewUnder(e.getX(),e.getY());
					if(card!=null&&listener!=null){
						//	listener.onLongClick(card, recycler.getChildPosition(card));
					}
				}
			});


		}
		@Override
		public boolean onInterceptTouchEvent(RecyclerView recycler, MotionEvent e) {
			View card=recycler.findChildViewUnder(e.getX(),e.getY());
			if(card!=null&&listener!=null&&gDetector.onTouchEvent(e)){
				//	listener.onClick(card, recycler.getChildPosition(card));
			}
			return false;
		}

		@Override
		public void onTouchEvent(RecyclerView recycler, MotionEvent e) {
			View card=recycler.findChildViewUnder(e.getX(),e.getY());
			if(card!=null&&listener!=null&&gDetector.onTouchEvent(e)){
				//	listener.onClick(card, recycler.getChildPosition(card));
			}

		}




	}
	public interface ClickListener{
		public void onClick(View view,int position);
		public void onLongClick(View view,int position);
	}



}















