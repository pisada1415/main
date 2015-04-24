package pisada.fallDetector;



import java.util.Calendar;

import pisada.plotmaker.Plot2d;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FallDetailsActivity extends ActionBarActivity {
	private static Plot2d plot;
	private static LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 
	private static Calendar c = Calendar.getInstance();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		View view; 
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		view = inflater.inflate(R.layout.activity_fall_details, null);
		RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.relative01);
		
		LinearLayout graphLayout = (LinearLayout) rl.findViewById(R.id.linear01);
		

/*
		graphLayout = new LinearLayout(this);
		graphLayout.setOrientation(LinearLayout.VERTICAL);
*/
		/*
		 * va preso un layout esistente e va inflatata dentro la view giusta. poi siamo a cavallo
		 */

 

	//=================================================fine prove debug
		
		graphLayout.addView(plot, lp);
		setContentView(rl);
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fall_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
