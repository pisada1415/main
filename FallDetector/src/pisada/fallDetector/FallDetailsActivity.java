package pisada.fallDetector;



import java.util.Calendar;

import pisada.plotmaker.Plot2d;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FallDetailsActivity extends ActionBarActivity {
	private Plot2d plot;
	private LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 
	private Calendar c = Calendar.getInstance();
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		View view; 
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		view = inflater.inflate(R.layout.activity_fall_details, null);
		RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.relative01);

		LinearLayout graphLayout = (LinearLayout) rl.findViewById(R.id.linear01);

		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		/*
		graphLayout = new LinearLayout(this);
		graphLayout.setOrientation(LinearLayout.VERTICAL);
		 */
		/*
		 * va preso un layout esistente e va inflatata dentro la view giusta. poi siamo a cavallo
		 */



		//=================================================fine prove debug

		//graphLayout.addView(plot, lp);
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
		switch(id){
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			startActivity(intent);
			return true;
		case android.R.id.home:
			this.finish();
			return true;

		default: return super.onOptionsItemSelected(item);
		}
	}
}
