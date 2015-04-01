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
	private String[] allColumns={FallSqlHelper.SESSION_NAME,FallSqlHelper.SESSION_IMG,FallSqlHelper.SESSION_START_TIME,FallSqlHelper.SESSION_END_TIME, FallSqlHelper.SESSION_CLOSE_COLUMN};
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
	public Session insert(String name,String img, long startTime, long endTime, int close) throws BoolNotBoolException{

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_NAME, name);
		values.put(FallSqlHelper.SESSION_IMG, img);
		values.put(FallSqlHelper.SESSION_START_TIME, startTime);
		values.put(FallSqlHelper.SESSION_END_TIME, endTime);
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, close);
		database.insert(FallSqlHelper.SESSION_TABLE, null,values);
		return new Session(name,img,startTime,endTime,close, context);

	}

	//STORE NUOVA ACQUISIZIONE DATA LA SESSIONE
	public Session insert(Session s) throws SQLiteConstraintException, BoolNotBoolException{
		String name=s.name(), img=s.img();
		long startTime=s.startTime(),  endTime=s.endTime();
		int close=s.integerIsClose();

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_NAME, name);
		values.put(FallSqlHelper.SESSION_IMG, img);
		values.put(FallSqlHelper.SESSION_START_TIME, startTime);
		values.put(FallSqlHelper.SESSION_END_TIME, endTime);
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, close);
		database.insert(FallSqlHelper.SESSION_TABLE, null,values);
		return new Session(name,img,startTime,endTime,close, context);

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
		int isClose=cursor.getInt(cursor.getColumnIndex(FallSqlHelper.SESSION_CLOSE_COLUMN));
		Session session=null;
		try{
			session=new Session(name,img,startTime,endTime, isClose,context);
		}
		catch(BoolNotBoolException e){
			e.printStackTrace();
		}
		return session;
	}

	//NUMERO DI TUTTE LE SESSIONE
	public int sessionCount(){
		Cursor cursor=database.query(FallSqlHelper.SESSION_TABLE, allColumns,null,null,null,null,null);
		int count=cursor.getCount();
		cursor.close();
		return count;
	}

	//DA CHIAMARE SOLO SE SI VUOLE AGGIORNARE IL DATABASE
	public void closeSession(String name){
		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, FallSqlHelper.CLOSE);
		values.put(FallSqlHelper.SESSION_END_TIME, System.currentTimeMillis());
		database.update(FallSqlHelper.SESSION_TABLE, values, FallSqlHelper.SESSION_NAME+" = "+ name, null);
	}


	//CHIUDE SIA LA SESSIONE NEL DATABASE, SIA COME OGGETTO PASSATO
	public void closeSession(Session s){

		long endTime=System.currentTimeMillis();

		String name=s.name();
		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.SESSION_CLOSE_COLUMN, FallSqlHelper.CLOSE);
		values.put(FallSqlHelper.SESSION_END_TIME, System.currentTimeMillis());
		//database.update(FallSqlHelper.SESSION_TABLE, values, FallSqlHelper.NAME+" = "+ name, null);
		database.execSQL("UPDATE "+FallSqlHelper.SESSION_TABLE
				+ " SET "+ FallSqlHelper.SESSION_CLOSE_COLUMN+" = "+FallSqlHelper.CLOSE+", "
				+ FallSqlHelper.SESSION_END_TIME+" = "+endTime
				+ " WHERE "+FallSqlHelper.SESSION_NAME+" = '"+name+"';");
		s.setClose(endTime);

	}
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


}


