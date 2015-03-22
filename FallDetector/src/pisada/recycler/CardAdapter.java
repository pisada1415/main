package pisada.recycler;


import java.util.ArrayList;
import pisada.fallDetector.Acquisition;
import pisada.fallDetector.R;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private ArrayList<Acquisition> acquisitionList;
	private Activity activity;



	public static class OldSessionHolder extends RecyclerView.ViewHolder {
		public ImageView vImg;
		public TextView vName;
		public TextView vAdjective;
		public OldSessionHolder(View v) {
			super(v);
			vName =  (TextView) v.findViewById(R.id.nameText);
			vAdjective = (TextView)  v.findViewById(R.id.adjectiveText);
		}
	}
	public  class CurrentSessionHolder extends RecyclerView.ViewHolder {
		public ImageView img;

		public CurrentSessionHolder(View v) {
			super(v);
			img=(ImageView) v.findViewById(R.id.image);

		}
	}


	public CardAdapter(ArrayList<Acquisition> acquisitionList, Activity activity) {

		this.acquisitionList = acquisitionList;
		this.activity=activity;
	}

	@Override
	public int getItemCount() {
		return acquisitionList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int i) {
		if(i==0){
			CurrentSessionHolder Cholder=(CurrentSessionHolder) holder;
			Cholder.img.setImageResource(activity.getResources().getIdentifier("first","drawable",activity.getPackageName()));
		}
		else{
			OldSessionHolder Oholder=(OldSessionHolder) holder;
			Acquisition acquisition = acquisitionList.get(i);
			Oholder.vName.setText("Time: "+acquisition.time()+"\nX: "+acquisition.xAxis()+"\nY: "+acquisition.yAxis()+"\nZ: "+acquisition.xAxis()+"\nSession: "+acquisition.session()+"\nFall: "+acquisition.booleanFall());
			Oholder.vAdjective.setText("culo");
		}

	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		if(i==0) return new CurrentSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_card_view, viewGroup, false));
		else return new OldSessionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview, viewGroup, false));
	}
	public void addItem(Acquisition a) {
		this.acquisitionList.add(a);
		notifyDataSetChanged();

	}

	@Override
	public int getItemViewType(int position) {
		if(position==0) return 0;
		return 1;
	}

}

