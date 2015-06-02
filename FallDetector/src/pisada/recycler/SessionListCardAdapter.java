package pisada.recycler;


import java.util.ArrayList;
import java.util.Random;

import javax.sql.DataSource;

import pisada.database.FallDataSource;
import pisada.database.FallDataSource.Fall;
import pisada.database.SessionDataSource;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.FragmentCommunicator;
import pisada.fallDetector.MainActivity;
import pisada.fallDetector.R;
import pisada.fallDetector.SessionDetailsFragment;
import pisada.fallDetector.Utility;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import fallDetectorException.DublicateNameSessionException;



public class SessionListCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

	private ArrayList<Session> sessionList;
	private static Activity activity;
	private static SessionDataSource sessionData;
	private static FallDataSource fallData;
	private ArrayList<Boolean> expandedArray=new ArrayList<Boolean>();
	private boolean existExp=false;






	public static class CurrentSessionHolder extends RecyclerView.ViewHolder {
		private TextView sessionNameText;
		private TextView fallsText;
		private TextView timeText;
		private TextView dateText;
		private TextView sentText;
		private TextView fallsTextfield;
		private TextView timeTextfield;
		private TextView dateTextfield;
		private ImageView img;
		private RelativeLayout fieldLay;
		private RelativeLayout rLay;
		private CardView card;


		public CurrentSessionHolder(View v) {
			super(v);



			sessionNameText=(TextView) v.findViewById(R.id.first_curr_name);
			fallsText=(TextView) v.findViewById(R.id.fist_curr_num_cadute);
			timeText=(TextView) v.findViewById(R.id.first_curr_ora_inizio);
			dateText=(TextView) v.findViewById(R.id.first_curr_date);
			sentText=(TextView) v.findViewById(R.id.first_curr_sent_field);
			fallsTextfield=(TextView) v.findViewById(R.id.first_curr_num_cadute_field);
			timeTextfield=(TextView) v.findViewById(R.id.first_curr_ora_inizio_field);
			dateTextfield=(TextView) v.findViewById(R.id.first_curr_date_field);
			fieldLay=(RelativeLayout) v.findViewById(R.id.first_curr_field_layout);
			img=(ImageView) v.findViewById(R.id.current_session_icon);
			card=(CardView) v;
			rLay=(RelativeLayout) v.findViewById(R.id.curr_card_session_layout);
		}

	}

	public static class OldSessionHolder extends RecyclerView.ViewHolder
	{
		private TextView vName;
		private TextView durationText;
		private TextView startTimeTextView;
		private TextView fallsTextView;
		private TextView fallsTextViewDesc;

		private ImageView sessionIcon;
		private Button expandButton;

		private RelativeLayout buttonsLayout;
		private Button deleteBtn;
		private Button archiveBtn;
		private Button renameBtn;
		private CardView oldCard;
		public OldSessionHolder(View v) {
			super(v);
			oldCard=(CardView) v;
			vName =  (TextView) v.findViewById(R.id.old_name_name);
			renameBtn=(Button) v.findViewById(R.id.old_rename_button);
			deleteBtn=(Button) v.findViewById(R.id.old_delete_button);
			archiveBtn =(Button)v.findViewById(R.id.old_archive_button); //TEMPORANEO
			sessionIcon=(ImageView) v.findViewById(R.id.archive_old_session_icon);
			buttonsLayout= (RelativeLayout) v.findViewById(R.id.buttons_layout);
			expandButton=(Button) v.findViewById(R.id.expand_button);
			startTimeTextView=(TextView) v.findViewById(R.id.old_start_description);
			fallsTextView= (TextView) v.findViewById(R.id.old_falls_description);
			durationText=(TextView) v.findViewById(R.id.old_duration_description);
			fallsTextViewDesc=(TextView) v.findViewById(R.id.old_falls_name);


		}


	}


	public SessionListCardAdapter(final Activity activity, RecyclerView rView) {

		SessionListCardAdapter.activity=activity;
		sessionData=new SessionDataSource(activity);
		fallData=new FallDataSource(activity);

		this.sessionList=sessionData.notArchivedSessions();

		if(!sessionData.existCurrentSession()){
			sessionList.add(0,new Session());
		}

		for(int i=0;i<sessionList.size();i++){
			expandedArray.add(false);
		}
	}

	@Override
	public int getItemCount() {
		return sessionList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder,  final int i) {

		Session currSession=sessionData.currentSession();
		switch(i) {
		case 0:
			CurrentSessionHolder cHolder=(CurrentSessionHolder) holder;

			if(currSession==null){

				cHolder.fieldLay.setVisibility(View.GONE);
				cHolder.img.setVisibility(View.GONE);
				cHolder.sessionNameText.setText(activity.getResources().getString(R.string.no_current_session));

			}

			else{
				cHolder.fieldLay.setVisibility(View.VISIBLE);
				cHolder.img.setVisibility(View.VISIBLE);
				cHolder.sessionNameText.setText(currSession.getName());
				cHolder.timeText.setText(String.valueOf(currSession.getStartTime()).toString());
				cHolder.dateText.setText(Utility.getStringDate(currSession.getStartTime()));
				cHolder.timeText.setText(Utility.getStringHour(currSession.getStartTime()));
				cHolder.fallsText.setText(String.valueOf(currSession.getFallsNumber()));
				ArrayList<Fall> falls=currSession.getFalls();

				if(falls!=null){
					cHolder.sentText.setVisibility(View.VISIBLE);
					cHolder.sentText.setText(activity.getResources().getString(R.string.falls_notified));
					for(Fall f:falls){
						if(!f.wasNotified()) cHolder.sentText.setText(activity.getResources().getString(R.string.falls_unnotified));
						break;
					}
				}
				else  cHolder.sentText.setVisibility(View.INVISIBLE);
				BitmapManager.loadBitmap(currSession.getID(), cHolder.img, activity);
			}

			return;
		}

		final OldSessionHolder Oholder=(OldSessionHolder) holder;
		final Session session = sessionList.get(i);


		OnClickListener sessionDetailListener=new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(existExp){
					int k;


					for(k=0;k<expandedArray.size();k++){

						if(k!=i&&expandedArray.get(k)){
							expandedArray.set(k,false);
							notifyItemChanged(k);
							return;
						}
					}
				}

				Intent intent=new Intent(activity,SessionDetailsFragment.class);
				intent.putExtra(Utility.SESSION_NAME_KEY, session.getName());
				((FragmentCommunicator)activity).switchFragment(intent);

			}
		};
		String fallsNumber="0";
		if(fallData.sessionFalls(session)!=null)
			fallsNumber=String.valueOf(fallData.sessionFalls(session).size());

		Oholder.vName.setText(session.getName());
		Oholder.durationText.setText(Utility.longToDuration(sessionData.sessionDuration(session)));
		Oholder.fallsTextView.setText(String.valueOf(session.getFallsNumber()));
		Oholder.startTimeTextView.setText(Utility.getStringDate(session.getStartTime()));




		BitmapManager.loadBitmap(session.getID(), Oholder.sessionIcon, activity);
		Oholder.oldCard.setOnClickListener(sessionDetailListener);
		Oholder.vName.setOnClickListener(sessionDetailListener);
		Oholder.sessionIcon.setOnClickListener(sessionDetailListener);
		Oholder.renameBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				final EditText input = new EditText(activity);
				input.setText( session.getName());
				input.addTextChangedListener((TextWatcher) activity);
				final Boolean isValid=null;
			
				
					new AlertDialog.Builder(activity)
					.setTitle("Rename")
					.setMessage("Insert name")
					.setView(input)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							String value = input.getText().toString(); 
							if(sessionData.existSession(value)){
								Toast.makeText(activity, "A session with this name already exists", Toast.LENGTH_SHORT).show();
							}
							else{
								sessionData.renameSession(session, value);
								notifyItemChanged(i);
							}

						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// Do nothing.
						}
					}).show();
				
			}


		});


		if(expandedArray.get(i))Oholder.buttonsLayout.setVisibility(View.VISIBLE);
		else Oholder.buttonsLayout.setVisibility(View.GONE);
		Oholder.expandButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for(int k=0;k<expandedArray.size();k++){

					if(k!=i&&expandedArray.get(k)){
						expandedArray.set(k,false);
						notifyItemChanged(k);
					}


				}
				if(expandedArray.get(i)==false){
					expandedArray.set(i, true);
					existExp=true;
					Oholder.buttonsLayout.setVisibility(View.VISIBLE);
					Animation animation = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.expandanimation);
					Oholder.buttonsLayout.startAnimation(animation);
				}
				else{
					expandedArray.set(i, false);
					existExp=false;
					Animation animation = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.collapseanimation);
					animation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {


						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							Oholder.buttonsLayout.setVisibility(View.GONE);


						}
					});
					Oholder.buttonsLayout.startAnimation(animation);
				}


			}
		});

		final int size=sessionList.size();
		Oholder.deleteBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				sessionData.deleteSession(session);
				sessionList.remove(i);
				expandedArray.remove(i);
				notifyItemRemoved(i);
				notifyItemRangeChanged(i, i+15);
			}
		});

		Oholder.archiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sessionData.setSessionArchived(session, true);
				sessionList.remove(i);
				expandedArray.remove(i);
				notifyItemRemoved(i);
				notifyItemRangeChanged(i, i+15);
			}
		});


	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

		if(type==0){
			return new CurrentSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_current_session_sessions_list_card, viewGroup, false));

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
		sessionList=sessionData.notArchivedSessions();
		if(!sessionData.existCurrentSession()){
			sessionList.add(0, new Session());
		}
	}

	@Override
	public int getItemViewType(int position) {

		switch(position){
		case 0: return 0;
		}
		return 3;

	}


	public void closeAllDetails(){
		for(int k=0;k<expandedArray.size();k++){

			if(expandedArray.get(k)){
				expandedArray.set(k,false);
				notifyItemChanged(k);
				return;
			}
		}
	}

}















