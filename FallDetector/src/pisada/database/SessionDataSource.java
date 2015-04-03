package pisada.database;

import java.util.ArrayList;

import pisada.fallDetector.Acquisition;
import pisada.fallDetector.Session;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class SessionDataSource {
	private SQLiteDatabase database;
	private FallSqlHelper databaseHelper;
	private String[] allColumns={FallSqlHelper.SESSION_NAME,FallSqlHelper.SESSION_IMG,FallSqlHelper.SESSION_START_TIME,FallSqlHelper.SESSION_END_TIME, FallSqlHelper.SESSION_CLOSE_COLUMN, FallSqlHelper.SESSION_DURATION, FallSqlHelper.SESSION_STOP_TIME_PREFERENCE};
	private Context context;

	public SessionDataSource(Context context){
		databaseHelper=new FallSqlHelper(context);
		this.context=context;
	}
	public void open() throws SQLException {
		database = databaseHelper.getWritableDatabase();
	}


	public void close() {
		databaseHelper.close();
	}

	//STORE NUOVA ACQUISIZIONE DATI I PARAMETRI
	public Session insert(String name,String img, long startTime, long endTime,long stopTimePreference, int close) throws BoolNotBoolException{

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_NAME, name);
		values.put(FallSqlHelper.SESSION_IMG, img);
		values.put(FallSqlHelper.SESSION_START_TIME, startTime);
		values.put(FallSqlHelper.SESSION_END_TIME, endTime);
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, close);
		values.put(FallSqlHelper.SESSION_DURATION, 0);
		values.put(FallSqlHelper.SESSION_STOP_TIME_PREFERENCE, stopTimePreference);
		database.insert(FallSqlHelper.SESSION_TABLE, null,values);
		return new Session(name,img,startTime,endTime, stopTimePreference,close, context);

	}

	//STORE NUOVA ACQUISIZIONE DATA LA SESSIONE
	public Session insert(Session s) throws SQLiteConstraintException, BoolNotBoolException{
		String name=s.name(), img=s.img();
		long startTime=s.startTime(),  endTime=s.endTime(), stopTimePreference=s.stopTimePreference();
		int close=s.integerIsClose();

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_NAME, name);
		values.put(FallSqlHelper.SESSION_IMG, img);
		values.put(FallSqlHelper.SESSION_START_TIME, startTime);
		values.put(FallSqlHelper.SESSION_END_TIME, endTime);
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, close);
		values.put(FallSqlHelper.SESSION_DURATION, 0);
		values.put(FallSqlHelper.SESSION_STOP_TIME_PREFERENCE, stopTimePreference);
		database.insert(FallSqlHelper.SESSION_TABLE, null,values);
		return new Session(name,img,startTime,endTime,stopTimePreference,close, context);

	}




	//RITORNA SESSIONE CORRENTE APERTA
	public Session currentSession(){
		Cursor cursor = database.query(FallSqlHelper.SESSION_TABLE,allColumns,FallSqlHelper.SESSION_CLOSE_COLUMN+" = 0",null,null,null,null);
		cursor.moveToFirst();
		if(cursor.getCount()==0)return null;
		Session s=cursorToSession(cursor);
		cursor.close();
		return s;

	}

	//RITORNA TRUE SE ESISTE SESSIONE APERTA CORRENTE
	public boolean existCurrentSession(){
		Cursor cursor = database.query(FallSqlHelper.SESSION_TABLE,allColumns,FallSqlHelper.SESSION_CLOSE_COLUMN+" = 0",null,null,null,null);
		if(cursor.getCount()==0){
			cursor.close();
			return false;
		}
		cursor.close();
		return true;		
	}

	//RITORNA TUTTE LE SESSIONI
	public ArrayList<Session> sessions(){
		ArrayList<Session> list=new ArrayList<Session>();

		Cursor cursor= database.rawQuery("SELECT * FROM "+FallSqlHelper.SESSION_TABLE+" ORDER BY "+FallSqlHelper.SESSION_START_TIME+" DESC",null );
		if(cursor.getCount()==0)return list;
		while(cursor.moveToNext()){
			Session s=cursorToSession(cursor);
			list.add(s);
		}
		cursor.close();
		return list;

	}


	//CONVERTE RIGA IN SESSIONE
	private Session cursorToSession(Cursor cursor){
		if(cursor.getCount()==0)return null;
		String name=cursor.getString(cursor.getColumnIndex(FallSqlHelper.SESSION_NAME));
		String img=cursor.getString(cursor.getColumnIndex(FallSqlHelper.SESSION_IMG));
		long startTime=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.SESSION_START_TIME));
		long endTime=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.SESSION_END_TIME));
		long stopTime=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.SESSION_STOP_TIME_PREFERENCE));
		int isClose=cursor.getInt(cursor.getColumnIndex(FallSqlHelper.SESSION_CLOSE_COLUMN));
		Session session=null;
		try{
			session=new Session(name,img,startTime,endTime, stopTime, isClose,context);
		}
		catch(BoolNotBoolException e){
			e.printStackTrace();
		}
		return session;
	}

	//NUMERO DI TUTTE LE SESSIONI
	public int sessionCount(){
		Cursor cursor=database.query(FallSqlHelper.SESSION_TABLE, allColumns,null,null,null,null,null);
		int count=cursor.getCount();
		cursor.close();
		return count;
	}

	//DA CHIAMARE SOLO SE SI VUOLE AGGIORNARE IL DATABASE
	/*public void closeSession(String name){
		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, FallSqlHelper.CLOSE);
		values.put(FallSqlHelper.SESSION_END_TIME, System.currentTimeMillis());
		database.update(FallSqlHelper.SESSION_TABLE, values, FallSqlHelper.SESSION_NAME+" = "+ name, null);
	}*/



	public Session getSession(String name){

		Cursor cursor=database.rawQuery("SELECT * FROM "+ FallSqlHelper.SESSION_TABLE+" WHERE "+FallSqlHelper.SESSION_NAME+" = '"+name+"'", null);
		if(cursor.getCount()==0)return null;
		cursor.moveToFirst();
		return cursorToSession(cursor);
	}

	public boolean existSession(String name){

		Session s=getSession(name);
		if(s==null)return false;
		return true;
	}

	

	//CHIUDE SIA LA SESSIONE NEL DATABASE, SIA COME OGGETTO PASSATO. RICORDARSI DI UPDATARE LA DURATA FUORI SE NO USARE 'closeAfterUpdateSession'
	public void closeSession(Session s){

		long endTime=System.currentTimeMillis();

		String name=s.name();
		//database.update(FallSqlHelper.SESSION_TABLE, values, FallSqlHelper.NAME+" = "+ name, null);
		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_CLOSE_COLUMN+" = "+FallSqlHelper.CLOSE+", "
				+ FallSqlHelper.SESSION_END_TIME+" = "+endTime
				+ " WHERE "+FallSqlHelper.SESSION_NAME+" = '"+name+"';");
		s.setClose(endTime);

	}

	//AGGIORNA DURATA E CHIUDE SESSIONE. SIA OGGETTO CHE DATABASE
	public void closeAfterUpdateSession(Session s, long addDuration){
		updateSessionDuration(s,addDuration);
		closeSession(s);
	}
	
	
	//RITORNA L'ULTIMA DURATA STORATA NEL DATABASE DELLA LA SESSIONE PASSATA....INUTILE CREDO
	public long sessionDuration(Session s){

		String[] column={FallSqlHelper.SESSION_DURATION};
		String where=FallSqlHelper.SESSION_NAME+" = "+s.name();
		Cursor cursor=database.query(FallSqlHelper.SESSION_TABLE, column,where,null,null,null,null);
		cursor.moveToFirst();
		long duration=cursor.getLong(0);
		cursor.close();
		return duration;
	}

	
	//AGGIORNA NEL DATABASE E RITORNA LA DURATA DELLA SESSIONE IN INPUT SOMMANDO LA DURATA DA AGGIUNGERE
	public long updateSessionDuration(Session s, long addDuration){

		String[] column={FallSqlHelper.SESSION_DURATION};
		String where=FallSqlHelper.SESSION_NAME+" = "+s.name();
		long oldDuration, newDuration;

		Cursor cursor=database.query(FallSqlHelper.SESSION_TABLE, column,where,null,null,null,null);
		cursor.moveToFirst();
		oldDuration= cursor.getLong(0);
		newDuration=oldDuration+addDuration;

		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_DURATION+" = "+newDuration+
				" WHERE "+FallSqlHelper.SESSION_NAME+" = '"+s.name()+"';");
		s.setStopTimePreference(newDuration);
		return newDuration;

	}

	
	//CAMBIA STOPTIMEPREFERENCE DELL'OGGETTO E NEL DATABASE
	public void changeStopTimePreference(Session s, long newStopTime){
		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_STOP_TIME_PREFERENCE+" = "+newStopTime+
				" WHERE "+FallSqlHelper.SESSION_NAME+" = '"+s.name()+"';");

		s.setStopTimePreference(newStopTime);
	}


}


