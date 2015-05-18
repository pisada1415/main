package pisada.recycler;


import java.util.ArrayList;

import pisada.database.SessionDataSource;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.FragmentCommunicator;
import pisada.fallDetector.R;
import pisada.fallDetector.SessionDetailsFragment;
import pisada.fallDetector.Utility;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class ArchiveCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

	private ArrayList<Session> sessionList;
	private static Activity activity;
	private static SessionDataSource sessionData;

	public static class SessionHolder extends RecyclerView.ViewHolder
	{
		private TextView vName;
		private Button detailsBtn;
		private Button deleteBtn;
		private Button archiveBtn;
		public SessionHolder(View v) {
			super(v);
			vName =  (TextView) v.findViewById(R.id.nameText);
			detailsBtn=(Button) v.findViewById(R.id.old_details_button);
			deleteBtn=(Button) v.findViewById(R.id.old_delete_button);
			archiveBtn =(Button)v.findViewById(R.id.old_rename_button);
		}
	}


	public ArchiveCardAdapter(final Activity activity, RecyclerView rView) {
		this.activity=activity;
		this.sessionData=new SessionDataSource(activity);

		this.sessionList=sessionData.archivedSessions();
		
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

		SessionHolder Oholder=(SessionHolder) holder;
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

		final int size=sessionList.size();
		Oholder.deleteBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				sessionData.deleteSession(session);
				sessionList.remove(j);
				notifyItemRemoved(j);
				notifyItemRangeChanged(j, size-j);
			}
		});

		Oholder.archiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sessionData.setSessionArchived(session, false);
				sessionList.remove(j);
				notifyItemRemoved(j);
				notifyItemRangeChanged(j, size-j);
			}
		});
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

		return new SessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.old_session_card, viewGroup, false));

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