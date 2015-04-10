package pisada.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import fallDetectorException.InvalidSessionException;
import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.Acquisition;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FallDataSource {
	private SQLiteDatabase database;
	private FallSqlHelper databaseHelper;
	private SessionDataSource sessionData;
	private String[] fallColumns = {FallSqlHelper.FALL_TIME, FallSqlHelper.FALL_FSESSION};
	private String[] acquisitionColumns = {FallSqlHelper.ACQUISITION_TIME,FallSqlHelper.ACQUISITION_FALL_TIME, FallSqlHelper.ACQUISITION_ASESSION, FallSqlHelper.ACQUISITION_XAXIS, FallSqlHelper.ACQUISITION_YAXIS, FallSqlHelper.ACQUISITION_ZAXIS};
	private Context context;



	public static class Fall {

		private Session session;
		private long time;
		private boolean isValid=true;

		private Fall(long time,Session session){
			if(!session.isValidSession()||session==null)throw new InvalidSessionException();

			this.session=session;
			this.time=time;

		}

		public Fall(){
			isValid=false;
		}

		public long getTime(){return time;}
		public Session getSession(){return session;}
	}





	public FallDataSource(Context context){
		databaseHelper=new FallSqlHelper(context);
		sessionData=new SessionDataSource(context);
		this.context=context;
		open();
	}

	public void open(){
		database=databaseHelper.getWritableDatabase();
	}
	public void close(){
		databaseHelper.close();
	}

	//INSERISCE UNA NUOVA CADUTA DATA UNA SESSIONE E UNA LISTA DI ACQUISIZIONI(passarla ordinata in funzione del tempo che se no è da ordinare ogni volta)
	public Fall insertFall(Session session, ArrayList<Acquisition> acquisitionList){

		
		long time=acquisitionList.get(acquisitionList.size()>>>1).getTime();
		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.FALL_TIME,time);
		values.put(FallSqlHelper.FALL_FSESSION, session.getName());
		database.insert(FallSqlHelper.FALL_TABLE,null,values);
		Fall fall=new Fall(time, session);
		for(Acquisition a: acquisitionList){
			insertAcquisition(fall, a);
		}
		return fall;


	}

	//PRIVATO -INSERISCE ACQUISIZIONE NEL DATABASE
	private void insertAcquisition(Fall fall,Acquisition a){


		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.ACQUISITION_TIME,a.getTime());
		values.put(FallSqlHelper.ACQUISITION_FALL_TIME, fall.getTime());
		values.put(FallSqlHelper.ACQUISITION_ASESSION,fall.getSession().getName());
		values.put(FallSqlHelper.ACQUISITION_XAXIS, a.getXaxis());
		values.put(FallSqlHelper.ACQUISITION_YAXIS, a.getYaxis());
		values.put(FallSqlHelper.ACQUISITION_ZAXIS,a.getZaxis());
		database.insert(FallSqlHelper.ACQUISITION_TABLE, null, values);

	}

	//RITORNA LA CADUTA DATA LA CHIAVE
	public Fall getFall(long time, String  sessionName){

		String where= FallSqlHelper.FALL_TIME+" = "+time+" AND "+FallSqlHelper.FALL_FSESSION+" = '"+sessionName+"'";
		Cursor cursor=database.query(FallSqlHelper.FALL_TABLE, fallColumns, where, null, null, null, null);
		if(cursor.getCount()==0)return null;
		cursor.moveToFirst();
		Fall fall=cursorToFall(cursor);
		cursor.close();
		return fall;
	}

	//RITORNA LA CADUTA DATA LA CHIAVE
	public Fall getFall(long time, Session session){
		String where= FallSqlHelper.FALL_TIME+" = "+time+" AND "+FallSqlHelper.FALL_FSESSION+" = '"+session.getName()+"'";
		Cursor cursor=database.query(FallSqlHelper.FALL_TABLE, fallColumns, where, null, null, null, null);
		if(cursor.getCount()==0)return null;
		cursor.moveToFirst();
		Fall fall=cursorToFall(cursor);
		cursor.close();
		return fall;
	}

	//RITORNA TUTTE LE CADUTE DI UNA SESSIONE
	public ArrayList<Fall> sessionFalls(Session session){

		ArrayList<Fall> list=new ArrayList<Fall>();
		Cursor cursor= database.rawQuery("SELECT * FROM "+FallSqlHelper.FALL_TABLE+" WHERE "+FallSqlHelper.FALL_FSESSION+" = '"+session.getName()+"' ORDER BY "+FallSqlHelper.FALL_TIME+" DESC",null );
		if(cursor.getCount()==0)return null;

		while(cursor.moveToNext()){
			list.add(cursorToFall(cursor));
		}
		cursor.close();
		return list;
	}

	/*//RITORNA TUTTE LE ACQUISIZIONI DI UNA CADUTA DATA LA CHIAVE PRIMARIA COMPOSTA 
	public ArrayList<Acquisition> fallAcquisitions(long fallTime, Session session){
		ArrayList<Acquisition> list=new ArrayList<Acquisition>();
		String WHERE=" WHERE "+FallSqlHelper.FALL_FSESSION+" = '"+session.getName()+"'"+" AND "+FallSqlHelper.FALL_TIME+" = "+fallTime;
		Cursor cursor= database.rawQuery("SELECT * FROM "+FallSqlHelper.FALL_TABLE+WHERE+" ORDER BY "+FallSqlHelper.FALL_TIME+" DESC",null );
		while(cursor.moveToNext()){
			list.add(cursorToAcquisition(cursor));
		}
		cursor.close();
		return list;
	}*/

	//RITORNA TUTTE E ACQUISIZIONI DI UNA CADUTA
	public ArrayList<Acquisition> fallAcquisitions(Fall fall){
		ArrayList<Acquisition> list=new ArrayList<Acquisition>();
		String WHERE=" WHERE "+FallSqlHelper.ACQUISITION_ASESSION+" = '"+fall.getSession().getName()+"'"+" AND "+FallSqlHelper.ACQUISITION_FALL_TIME+" = "+fall.getTime();
		Cursor cursor= database.rawQuery("SELECT * FROM "+FallSqlHelper.ACQUISITION_TABLE+" "+WHERE+" ORDER BY "+FallSqlHelper.ACQUISITION_TIME+" DESC",null );
		while(cursor.moveToNext()){
			list.add(cursorToAcquisition(cursor));
		}
		cursor.close();
		return list;
	}
	
	//RITORNA TUTTE LE CADUDE DELLA SESSIONE
	public ArrayList<Acquisition> sessionAcquisitions(Session s){
		ArrayList<Acquisition> acquisitions=new ArrayList<Acquisition>();
		ArrayList<Fall> falls= sessionFalls(s);
		
		for(Fall f: falls){
			for(Acquisition a: fallAcquisitions(f)){
				acquisitions.add(a);
			}
		}
		
		return acquisitions;
	
	}

	//PRIVATO -TRASFORMA RIGA CURSORE IN OGGETTO CADUTA
	private Fall cursorToFall(Cursor cursor){
		if(cursor.getCount()==0) return null;

		long time=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.FALL_TIME));
		Session session=sessionData.getSession(cursor.getString(cursor.getColumnIndex(FallSqlHelper.FALL_FSESSION)));

		return new Fall(time,session);

	}

	//PRIVATO -TRASFORMA RIGA CURSORE IN OGGETTO ACQUISIZIONE 
	private Acquisition cursorToAcquisition(Cursor cursor){
		if(cursor.getCount()==0)return null;
		long time=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_TIME));
		float xAxis=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_XAXIS));
		float yAxis=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_YAXIS));
		float zAxis=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_ZAXIS));
		return new Acquisition(time, xAxis,yAxis,zAxis);
	}

}
