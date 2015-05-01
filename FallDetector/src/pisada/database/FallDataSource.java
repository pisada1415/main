package pisada.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import fallDetectorException.InvalidFallException;
import fallDetectorException.InvalidSessionException;
import pisada.database.SessionDataSource.Session;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class FallDataSource {
	private static SQLiteDatabase database;
	private FallSqlHelper databaseHelper;
	private SessionDataSource sessionData;
	private String[] fallColumns = {FallSqlHelper.FALL_TIME, FallSqlHelper.FALL_FSESSION,FallSqlHelper.FALL_LAT,FallSqlHelper.FALL_LNG, FallSqlHelper.FALL_NOTIFIED_COLUMN};
	private String[] acquisitionColumns = {FallSqlHelper.ACQUISITION_TIME,FallSqlHelper.ACQUISITION_FALL_TIME, FallSqlHelper.ACQUISITION_ASESSION, FallSqlHelper.ACQUISITION_XAXIS, FallSqlHelper.ACQUISITION_YAXIS, FallSqlHelper.ACQUISITION_ZAXIS};
	private Context context;





	public static class Fall {

		private Session session;
		private long time;
		private double lng;
		private double lat;
		private int notified;
		private boolean isValid=true;
		private boolean diodiodihhosscoioi;

		private Fall(long time,Session session, double lat, double lng, int notified){


			if(!session.isValidSession()||session==null)throw new InvalidSessionException();

			this.session=session;
			this.time=time;
			this.lng=lng;
			this.lat=lat;
			this.notified=notified;

		}

		public Fall(){
			isValid=false;
		}

		private void setNotificationSuccess(boolean notified){
			if(notified)this.notified=1;
			else this.notified=0;
		}
			
		public double getLat(){return lat;}
		public double getLng(){return lng;}
		public long getTime(){return time;}
		public Session getSession(){return session;}
		public boolean isValid(){return isValid;}
		public String getSessionName(){return session.getName();}
		public boolean wasNotified(){return notified==1;}
				
		}






	public FallDataSource(Context context){
		synchronized(FallDataSource.class){
			if(databaseHelper==null) databaseHelper=FallSqlHelper.getIstance(context);
			sessionData=new SessionDataSource(context);
			this.context=context;
			open();
		}
	}

	public void open(){
		database=databaseHelper.getWritableDatabase();
	}


	//INSERISCE UNA NUOVA CADUTA DATA UNA SESSIONE E UNA LISTA DI ACQUISIZIONI(passarla ordinata in funzione del tempo che se no è da ordinare ogni volta)
	public Fall insertFall(Session session, ConcurrentLinkedQueue<Acquisition> acquisitionList, double lat, double lng){


		long time=0;
		int i=0;
		int size=acquisitionList.size();
		long lastTime=System.currentTimeMillis();
		for(Acquisition a: acquisitionList){
			if(i==size/2) {
				time=a.getTime();
				break;
			}
			i++;
		}
		System.out.println(System.currentTimeMillis()-lastTime);
		
		//se volete riprodurre lo stream dovete diciamo, chiedere anche il permesso di accedere alla rete, anche questo è un permesso.k

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.FALL_TIME,time);
		values.put(FallSqlHelper.FALL_FSESSION, session.getName());
		values.put(FallSqlHelper.FALL_LAT, lat);
		values.put(FallSqlHelper.FALL_LNG, lng);
		database.insert(FallSqlHelper.FALL_TABLE,null,values);
		Fall fall=new Fall(time, session,lat, lng,FallSqlHelper.UNNOTIFIED);
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
		a.setFall(fall);
		a.setSession(fall.getSession());




	}

	private Acquisition getAcquisition(Fall fall,long acquisitionTime){


		String where=FallSqlHelper.ACQUISITION_ASESSION+" = '"+fall.getSession().getName()+"' AND "+FallSqlHelper.ACQUISITION_FALL_TIME+ " = "+fall.getTime()+" AND "+FallSqlHelper.ACQUISITION_TIME+" = "+acquisitionTime;
		Cursor cursor=database.query(FallSqlHelper.ACQUISITION_TABLE, acquisitionColumns,where, null,null,null,null);
		if(cursor.getCount()==0) {
			cursor.close();
			return null;
		}
		cursor.moveToFirst();
		Acquisition a=cursorToAcquisition(cursor);
		cursor.close();
		return a;
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
		double lat=cursor.getDouble(cursor.getColumnIndex(FallSqlHelper.FALL_LAT));
		double lng=cursor.getDouble(cursor.getColumnIndex(FallSqlHelper.FALL_LNG));
		int notified=cursor.getInt(cursor.getColumnIndex(FallSqlHelper.FALL_NOTIFIED_COLUMN));
		

		return new Fall(time,session,lat,lng, notified);

	}

	//PRIVATO -TRASFORMA RIGA CURSORE IN OGGETTO ACQUISIZIONE 
	private Acquisition cursorToAcquisition(Cursor cursor){
		if(cursor.getCount()==0)return null;
		long time=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_TIME));
		float xAxis=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_XAXIS));
		float yAxis=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_YAXIS));
		float zAxis=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_ZAXIS));
		long fallTime=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_FALL_TIME));
		String sName=cursor.getString(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_ASESSION));
		Acquisition a = new Acquisition(time, xAxis,yAxis,zAxis);
		a.setSession(sessionData.getSession(sName));
		a.setFall(getFall(fallTime,a.getSession()));
		return a;
	}
	
	public void setNotificationSuccess(Fall fall,boolean notified){
		if(!fall.isValid())throw new InvalidFallException();
		int intNotified;
		if(notified)intNotified=FallSqlHelper.NOTIFIED;
		else intNotified=FallSqlHelper.UNNOTIFIED;
		
		database.execSQL("UPDATE "+FallSqlHelper.FALL_TABLE
				+ " SET "+ FallSqlHelper.FALL_NOTIFIED_COLUMN+" = "+intNotified+
				" WHERE "+FallSqlHelper.FALL_FSESSION+" = '"+fall.session.getName()+"' AND "+FallSqlHelper.FALL_TIME+" = "+fall.getTime()+")");

		fall.setNotificationSuccess(notified);
		
		
	}

}
