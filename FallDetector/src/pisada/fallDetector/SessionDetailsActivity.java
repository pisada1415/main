package pisada.fallDetector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author Piero
 * UI punto 2
 */

public class SessionDetailsActivity extends ActionBarActivity {

	private ListView lista;
	private MySessionAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session);
		lista = (ListView) findViewById(R.id.lista);
		String[] array = new String[]{"Caduta 1","Caduta 2","Caduta 3","Caduta 4","Caduta 5","Caduta 6","Caduta 7","Caduta 8",
										"Caduta 9","Caduta 10","Caduta 11","Caduta 12","Caduta 13","Caduta 14","Caduta 15",
										"Caduta 16","Caduta 17","Caduta 18","Caduta 19","Caduta 20"};
		adapter = new MySessionAdapter(this,array);
		lista.setAdapter(adapter);
		lista.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent intent = new Intent(SessionDetailsActivity.this,FallDetailsActivity.class);
				intent.putExtra("segnale",adapter.getSegnale(position));
				startActivity(intent);
			}
		});
	}

	// Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.session, menu);
        return true;
    }
     
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	TextView txt = (TextView) findViewById(R.id.nome_sessione);
        EditText edit = (EditText) findViewById(R.id.edit);
        if(item.getTitle().equals("Modify")){
        	txt.setVisibility(TextView.GONE);
            edit.setVisibility(EditText.VISIBLE);
            edit.setText(txt.getText());
            item.setTitle("Save");
        }else{
            txt.setVisibility(TextView.VISIBLE);
            edit.setVisibility(EditText.GONE);
            txt.setText(edit.getText());
            item.setTitle("Modify");
        }
        return super.onOptionsItemSelected(item);
    }    
}
