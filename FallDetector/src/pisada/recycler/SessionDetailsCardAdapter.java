package pisada.recycler;


import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.FragmentCommunicator;
import pisada.fallDetector.R;
import pisada.fallDetector.Utility;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
public class SessionDetailsCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static ArrayList<CardContent> cardContentList;
	private Activity activity;
	
	private static String currentSessionName;
	private Session session;
	private SessionDataSource sessionData;
	

	/*
	 * first card
	 */
	public  class FirstCardHolder extends RecyclerView.ViewHolder {
		private ImageView thumbNail;
		private TextView info;
		public FirstCardHolder(View v) {
			super(v);
			info = (TextView)v.findViewById(R.id.info);
			thumbNail = (ImageView)v.findViewById(R.id.thumbnailSessionDetails);
			
		}

	}
	/*
	 * fall cards
	 */
	public  class FallsHolder extends RecyclerView.ViewHolder{
		private ImageView fallThumbnail;
		private TextView fallTime;
		private TextView fallPosition;
		private TextView boolNotif;
		//TODO notifica mandata correttamente o no
		public FallsHolder(View v) {
			super(v);
			fallThumbnail=(ImageView) v.findViewById(R.id.thumbnail_fall);
			fallTime=(TextView) v.findViewById(R.id.fall_time);
			fallPosition=(TextView) v.findViewById(R.id.position);
			boolNotif = (TextView) v.findViewById(R.id.booleanSent);
			((TextView) v.findViewById(R.id.successNotif)).setVisibility(View.GONE);
			v.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int position = getAdapterPosition();
					Intent intent = new Intent(activity, pisada.fallDetector.FallDetailsFragment.class);
					long time = cardContentList.get(position).getTime();
					intent.putExtra("fallTime", time);
					intent.putExtra("fallSession", currentSessionName);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
					((FragmentCommunicator)activity).switchFragment(intent);
					//Toast.makeText(activity, "premuta caduta " + cardContentList.get(position).getTime(), Toast.LENGTH_SHORT).show();
				
				}
			});
		}
		

	}



	public SessionDetailsCardAdapter(Activity activity, SessionDataSource sessionData, String sessionName) {

		this.activity=activity;
		if(sessionData == null)
			sessionData = new SessionDataSource(activity);
			
		session = sessionData.getSession(sessionName);
		cardContentList = new ArrayList<CardContent>();
		cardContentList.add(0, new CardContent());
		
	}

	


	@Override
	public void onBindViewHolder(ViewHolder holder, int i) {

		if(sessionData == null)
		sessionData = new SessionDataSource(activity);
		/*
		 * TODO qui vanno messi i valori al titolo cronometro ecc ecc in base a session
		 */
		
		if(i==0){ //se sono le prime due non fare niente
			
			FirstCardHolder fch = (FirstCardHolder) holder;
			fch.thumbNail.setImageBitmap(Utility.createImage(Utility.randInt(2, 100)));
			Resources res = activity.getResources();
			String infoString = res.getString(R.string.starttime)+Utility.getStringTime(session.getStartTime())+
					"\n"+res.getString(R.string.duration)+Utility.longToDuration(sessionData.sessionDuration(session));
			
			fch.info.setText(infoString);
		}
		else{
			/*
			 * TODO qui anziché randint va passato il numero della sessione cui la fall fa riferimento
			 */
			CardContent fall = cardContentList.get(i);
			FallsHolder Oholder=(FallsHolder) holder;
			Oholder.fallThumbnail.setImageBitmap(Utility.createImage(Utility.randInt(2, 100)));
			String link = fall.getLink();
			if(link != null){
				Oholder.fallPosition.setText(Html.fromHtml("<a href=\""+ link + "\">" + "Position: " + fall.getPos() + "</a>"));
				
				Oholder.fallPosition.setClickable(true);
				Oholder.fallPosition.setMovementMethod (LinkMovementMethod.getInstance());
			}
			else{
				Oholder.fallPosition.setText("Position: " + fall.getPos());
				Oholder.fallPosition.setClickable(false);
			}
			
			Oholder.fallTime.setText("Time: " + fall.getTimeLiteral());
			
			Oholder.boolNotif.setVisibility(View.GONE);
			
			
			
		}

	}


	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {


		if(type==0){
			return new FirstCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.first_sessiondetails_card, parent, false));
		}
		else 
			{
			
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fall_card, parent, false);
		    
		    
				return new FallsHolder(view);
			}

	}


	@Override
	public int getItemViewType(int position) {
		if(position==0) return 0;
		return 2;
	}



	@Override
	public int getItemCount() {
		return cardContentList.size();
	}


		
	private void addFallToCardList(String position, String link, String timeLiteral, long time, boolean b)
	{
		CardContent cc = new CardContent(position,link,timeLiteral, time, b);
		if(!cardContentList.contains(cc)){
			cardContentList.add(cc);
			notifyItemInserted(cardContentList.size()-1);
		}
		else
		{
			int i = 0;
			for(; i < cardContentList.size() && !cardContentList.get(i).equals(cc); i++);
			cardContentList.set(i, cc);
			notifyItemChanged(i);
			
		}
	}

	
	public void addFall(FallDataSource.Fall f)
	{
		long timeLong = f.getTime();
		String timeLiteral = Utility.getStringTime(timeLong);
		String position;
		position = (f.getLat() != -1 && f.getLng() != -1) ? "" + f.getLat() + ", " + f.getLng() : "Not available";
		addFallToCardList(position, Utility.getMapsLink(f.getLat(), f.getLng()), timeLiteral, timeLong, f.wasNotified());
	}
	
	public void clearFalls()
	{
		while(cardContentList.size()>2)
		{
			cardContentList.remove(2);
			this.notifyItemRemoved(2);
		}
		
	}


}

