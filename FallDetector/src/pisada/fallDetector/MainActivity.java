package pisada.fallDetector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView txt = (TextView) findViewById(R.id.txt); //qualcuno non ha pushato il layout? mi da errore qui.. :/
        txt.setText("Merdaaaaaaaaaaaaaaaaa");
        //culooooo
        txt.setClickable(true);
        txt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "BESTIAAAA", Toast.LENGTH_LONG).show();;
			}
		});
    }
}
