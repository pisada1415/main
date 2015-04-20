package pisada.test;

import java.util.concurrent.ConcurrentLinkedQueue;

import pisada.database.Acquisition;
import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.R;
import pisada.fallDetector.R.id;
import pisada.fallDetector.R.layout;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DatabaseActivity extends Activity {

	private Session sessione;
	private ConcurrentLinkedQueue<Acquisition> acquisition = new ConcurrentLinkedQueue<Acquisition>();
	private static final int LENGTH = 10000;
	private FallDataSource fall;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);
		Button b = (Button) findViewById(R.id.storaXmila);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long inizio = System.currentTimeMillis();
				try{
					sessione = (new SessionDataSource(getApplicationContext())).openNewSession("Sessione Test", "Immagine", System.currentTimeMillis());
				}catch(Exception e){
					Toast.makeText(getApplicationContext(),""+e, Toast.LENGTH_SHORT).show();
				}
				
				for(int i=0,x=0,y=0,z=0;i<LENGTH;i++,x+=100,y+=300,z+=500){
					acquisition.add(new Acquisition(System.currentTimeMillis()+i,x,y,z));
				}
				
				fall = new FallDataSource(getApplicationContext());
				fall.insertFall(sessione, acquisition, 20, 30);
				long fine = System.currentTimeMillis()-inizio;
				Toast.makeText(getApplicationContext(), "Per storare "+LENGTH+" valori:\n"+fine+" millisec", Toast.LENGTH_LONG).show();
			}
		});
	}
}
