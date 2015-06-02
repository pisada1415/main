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
import android.widget.ImageView;
import android.widget.TextView;


public class ArchiveCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

	private ArrayList<Session> sessionList;
	private static Activity activity;
	private static SessionDataSource sessionData;

	public static class SessionHolder extends RecyclerView.ViewHolder
	{
		private TextView name;
		private TextView falls;
		private TextView date;
		private ImageView img;
		private Button deleteBtn;
		private Button deArchiveBtn;
		public SessionHolder(View v) {
			super(v);
			img=(ImageView) v.findViewById(R.id.archive_old_session_icon);
			name=(TextView) v.findViewById(R.id.archive_old_name_name);
			falls =(TextView)v.findViewById(R.id.archive_old_falls_description);
			date =(TextView)v.findViewById(R.id.archive_old_start_description);
			deleteBtn=(Button) v.findViewById(R.id.archive_old_delete_button);
			deArchiveBtn=(Button) v.findViewById(R.id.archive_old_archive_button);
			//	deleteBtn=(Button) v.findViewById(R.id.)
		}
	}


	public ArchiveCardAdapter(final Activity activity, RecyclerView rView) {
		ArchiveCardAdapter.activity=activity;
		sessionData=new SessionDataSource(activity);

		this.sessionList=sessionData.archivedSessions();

	}

	@Override
	public int getItemCount() {
		return sessionList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder,  final int i) {

		SessionHolder Oholder=(SessionHolder) holder;
		final Session session = sessionList.get(i);
	
		Oholder.name.setText(session.getName());
		Oholder.falls.setText(String.valueOf(session.getFallsNumber()));
		Oholder.date.setText(Utility.getStringDate(session.getStartTime()));
		BitmapManager.loadBitmap(session.getID(), Oholder.img, activity);
		Oholder.deArchiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sessionData.setSessionArchived(session, false);
				sessionList.remove(i);
				notifyItemRemoved(i);
				notifyItemRangeChanged(i, sessionList.size()-i);
			}
		});

		Oholder.deleteBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sessionData.deleteSession(session);
				sessionList.remove(i);
				notifyItemRemoved(i);
				notifyItemRangeChanged(i, sessionList.size()-i);
			}
		});

		/*final int size=sessionList.size();
		Oholder.deleteBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				sessionData.deleteSession(session);
				sessionList.remove(j);
				notifyItemRemoved(j);
				notifyItemRangeChanged(j, size-j);
			}
		});*/

	
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

		return new SessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.archive_old_session_card, viewGroup, false));

	}



}