package pisada.fallDetector;

import java.util.Random;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
 
/**
 * 
 * @author Piero
 * Adapter per lista cadute sessione
 */

public class MySessionAdapter extends ArrayAdapter<String> {
	
	private final Context context;
	//private final String[] values;
	private boolean[] segnale; 
	
	public MySessionAdapter(Context context, String[] values) {
		super(context,R.layout.adapter, values);
		this.context = context;
		//this.values = values;
		segnale = new boolean[values.length];
	}
 
	@Override
	public View getView(int position, View insertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.adapter, parent, false);
		TextView t = (TextView) rowView.findViewById(R.id.segnalazione);
		Random r = new Random();
		boolean inviata = r.nextBoolean();
		segnale[position] = inviata;
		t.setText("Segnalazione Inviata? "+inviata);
		return rowView;
	}
	
	public boolean getSegnale(int position){
		return segnale[position];
	}
}