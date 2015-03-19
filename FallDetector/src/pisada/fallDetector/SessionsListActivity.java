package pisada.fallDetector;

/*
 * questa è la mainactivity. Vorrei farla in stile Google FIT (dateci un'occhiata) così rispettiamo
 * le richieste di mettere lo start stop (bottone in alto iniziale) nella stessa schermata
 * della lista di sessions..
 */

import java.util.ArrayList;
import java.util.List;

import recycler.CardAdapter;
import recycler.Member;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


public class SessionsListActivity extends ActionBarActivity {
	private RecyclerView rView;
	private CardAdapter cardAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	ArrayList<Member> members=new ArrayList<Member>();
	private int counter=0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sessions_list);
		rView=(RecyclerView) findViewById(R.id.my_recycler_view);
		rView.setHasFixedSize(true);
		for(int i=0;i<50;i++){
			Member m=new Member(i,"cazzo");
			members.add(m);
		}
		cardAdapter=new CardAdapter(members);
		rView.setAdapter(cardAdapter);
		mLayoutManager = new LinearLayoutManager(this);
		rView.setLayoutManager(mLayoutManager);
		Button btn=(Button) findViewById(R.id.addButton);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				counter++;
				Member m=new Member(counter, "ZIOOOOOOO");
				members.add(m);
				cardAdapter.addItem(m);
			}


		});;

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sessions_list, menu);
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


