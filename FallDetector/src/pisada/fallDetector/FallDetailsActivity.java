package pisada.fallDetector;



import java.util.Calendar;

import pisada.plotmaker.Data;
import pisada.plotmaker.Plot2d;
import android.content.Context;
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
	private static LinearLayout graphLayout;
	private static LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 
	private static Calendar c = Calendar.getInstance();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		View view; 
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		view = inflater.inflate(R.layout.activity_fall_details, null);

		graphLayout = (LinearLayout) view.findViewById(R.id.linear01);
		
	
/*
		graphLayout = new LinearLayout(this);
		graphLayout.setOrientation(LinearLayout.VERTICAL);
*/
		/*
		 * va preso un layout esistente e va inflatata dentro la view giusta. poi siamo a cavallo
		 */

  //=====================================================inizio prove debug
		plot = new Plot2d(this, 10);
		plot.pushValue(new Data(0,2));
		plot.pushValue(new Data(1,2));
		plot.pushValue(new Data(2,30));
		plot.pushValue(new Data(3,2));
		plot.pushValue(new Data(4,1));
		plot.pushValue(new Data(5,15));
		plot.pushValue(new Data(9,2));
		plot.pushValue(new Data(10,2));
		plot.pushValue(new Data(11,30));
		plot.pushValue(new Data(12,2));
		plot.pushValue(new Data(13,1));
		plot.pushValue(new Data(14,15));

		
		Thread t = new Thread(){
				@Override
				public void run()
				{
					while(true){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					runOnUiThread (new Thread(new Runnable() { 
				         public void run() {
				        	 int minsec = c.get(Calendar.MINUTE)*60;
				        	 int secsec = c.get(Calendar.SECOND); 
				        	 c = Calendar.getInstance();
				        	 plot.pushValue(new Data(minsec + secsec, Math.random()*100));
								plot.invalidate();
				         }
				     }));
					
					}
				}};
		t.start();
		
	//=================================================fine prove debug
		
		graphLayout.addView(plot, lp);
		setContentView(graphLayout);
		

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
