package pisada.recycler;


import java.util.ArrayList;
import pisada.fallDetector.Acquisition;
import pisada.fallDetector.R;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardHolder> {

	private ArrayList<Acquisition> acquisitionList;
	private Context context;
	
	public  class CardHolder extends RecyclerView.ViewHolder {
		public TextView vName;
		public TextView vAdjective;

		public CardHolder(View v) {
			super(v);
			vName =  (TextView) v.findViewById(R.id.nameText);
			vAdjective = (TextView)  v.findViewById(R.id.adjectiveText);
		}
	}

	public CardAdapter(ArrayList<Acquisition> acquisitionList, Context context) {
	
		this.acquisitionList = acquisitionList;
		this.context=context;
	}

	@Override
	public int getItemCount() {
		return acquisitionList.size();
	}

	@Override
	public void onBindViewHolder(CardHolder holder, int i) {
		Acquisition acquisition = acquisitionList.get(i);
		holder.vName.setText("Time: "+acquisition.time()+"\nX: "+acquisition.xAxis()+"\nY: "+acquisition.yAxis()+"\nZ: "+acquisition.xAxis()+"\nSession: "+acquisition.session()+"\nFall: "+acquisition.booleanFall());
		holder.vAdjective.setText("culo");
		int k=0;		
	}

	@Override
	public CardHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View itemView = LayoutInflater.from(viewGroup.getContext()).
				inflate(R.layout.cardview, viewGroup, false);

		return new CardHolder(itemView);
	}
	public void addItem(Acquisition a) {
	       this.acquisitionList.add(a);
	       notifyDataSetChanged();
	     
	}

}

