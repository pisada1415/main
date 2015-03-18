package pisada.fallDetector;

import java.util.ArrayList;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

public class MyAdapter extends ArrayAdapter<Sessione> {
	
	private final Context context;
	private ArrayList<Sessione> sessions;
	private boolean isPlay = false;
	
	public MyAdapter(Context context,ArrayList<Sessione> sessions) {
		super(context,R.layout.general_adapter_session_list,sessions);
		this.sessions = sessions;
		this.context = context;
	}
 
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		if(position==0){
			rowView = inflater.inflate(R.layout.first_adapter_session_list,parent,false);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,500);
			rowView.setLayoutParams(params);
			final ImageButton play_pause = (ImageButton) rowView.findViewById(R.id.play_pause_first_adapter);
			play_pause.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(!isPlay){
						play_pause.setBackgroundResource(R.drawable.play);
						isPlay = true;
					}else{
						play_pause.setBackgroundResource(R.drawable.pause);
						isPlay = false;
					}
				}
			});
		}else{
			rowView = inflater.inflate(R.layout.general_adapter_session_list,parent,false);
		}
		return rowView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		if(position==0){
			rowView = inflater.inflate(R.layout.first_adapter_session_list,parent,false);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,500);
			rowView.setLayoutParams(params);
			final ImageButton play_pause = (ImageButton) rowView.findViewById(R.id.play_pause_first_adapter);
			play_pause.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(!isPlay){
						play_pause.setBackgroundResource(R.drawable.play);
						isPlay = true;
					}else{
						play_pause.setBackgroundResource(R.drawable.pause);
						isPlay = false;
					}
				}
			});
		}else{
			rowView = inflater.inflate(R.layout.general_adapter_session_list,parent,false);
		}
		return rowView;
	}
	
	/*private void createFirstSession(View v){
		
	}
	
	private void createSession(View v){
		
	}*/
}
