package pisada.fallDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsActivity extends ActionBarActivity {


	private final String CONTACTS_KEY = "contacts";
	private ArrayList<String> contacts;
	private SharedPreferences sp;
	private ActionBar actionBar;
	private static final int CONTACT_PICKER_RESULT = 1021;
	private ArrayAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		Set<String> numbers = sp.getStringSet(CONTACTS_KEY, null);
		contacts = numbers != null ? new ArrayList<String>(numbers) : new ArrayList<String>();
		actionBar = getSupportActionBar();
		ListView listView = (ListView) findViewById(R.id.listView1);
		adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, contacts);
		listView.setAdapter(adapter);


		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, View arg1,
					final int position, long id) {
				new AlertDialog.Builder(ContactsActivity.this)
				.setTitle(getResources().getString(R.string.deletecontact))
				.setMessage(getResources().getString(R.string.sure))
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						final String item = (String) parent.getItemAtPosition(position);
						contacts.remove(item);
						adapter.notifyDataSetChanged();
						Set<String> set = new HashSet<String>();
						set.addAll(contacts);
						sp.edit().putStringSet(CONTACTS_KEY, set).commit();

					}
				})
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).show();

				return true;
			}
		}
				);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int layoutResourceId, List<String> objects) {
			super(context, layoutResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contacts, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {

		case android.R.id.home:
			// app icon in action bar clicked; goto parent activity.
			Intent toSettings = new Intent(this, SettingsActivity.class);
			toSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			startActivity(toSettings);
			return true;
		case R.id.action_addcontact:
			/*
			 * apri contacts picker
			 */
			Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
			pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
			startActivityForResult(pickContactIntent, CONTACT_PICKER_RESULT);	

			return true;
		case R.id.action_addnumber:
			/*
			 *  apri dialog [nome-numero] e salva numero nella lista poi salva la lista in sp
			 */
			final View textEntryView = LayoutInflater.from(this).inflate(R.layout.doubletextview, null);



			final AlertDialog dialog = new AlertDialog.Builder(ContactsActivity.this)
			.setTitle(getResources().getString(R.string.insertNumber))
			.setMessage(getResources().getString(R.string.insertContactNumber))
			.setView(textEntryView)
			.setPositiveButton(getResources().getString(R.string.ok), null)
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			}).create();

			final EditText inputName = (EditText) textEntryView.findViewById(R.id.editText1);
			final EditText inputNumber = (EditText) textEntryView.findViewById(R.id.editText2);
			inputNumber.setInputType(InputType.TYPE_CLASS_PHONE);


			dialog.setOnShowListener(new DialogInterface.OnShowListener() {

				@Override
				public void onShow(DialogInterface d) {

					Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
					b.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View view) {

							if(inputName.getText().toString() == "" || inputNumber.getText().toString() == "")
								Toast.makeText(ContactsActivity.this, getResources().getString(R.string.complainInsertionContact), Toast.LENGTH_SHORT).show();
							else{
								String name = inputName.getText().toString(); 
								String number = inputNumber.getText().toString();
								String contact = name + "\n" + number;
								if(!contacts.contains(contact))
									contacts.add(contact);
								Set<String> set = new HashSet<String>();
								set.addAll(contacts);
								sp.edit().putStringSet(CONTACTS_KEY, set).commit();
								dialog.dismiss();
							}


						}
					});
				}
			});

			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				String phoneNo = null ;
				String name = null;
				Uri uri = data.getData();
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				cursor.moveToFirst();
				int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				phoneNo = cursor.getString(phoneIndex);
				name = cursor.getString(nameIndex);
				String contact = ""+name+"\n"+phoneNo;
				if(!contacts.contains(contact))
					contacts.add(contact);
				adapter.notifyDataSetChanged();
				Set<String> set = new HashSet<String>();
				set.addAll(contacts);
				sp.edit().putStringSet(CONTACTS_KEY, set).commit();
			}
		}
	}
	

}
