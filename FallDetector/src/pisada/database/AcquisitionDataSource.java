package pisada.database;






import java.util.ArrayList;

import pisada.database.SessionDataSource.Session;
import pisada.fallDetector.Acquisition;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class AcquisitionDataSource {
	private SQLiteDatabase database;
	private FallSqlHelper dbHelper;
	private String[] allColumns = {FallSqlHelper.ACQUISITION_TIME,FallSqlHelper.ACQUISITION_XAXIS, FallSqlHelper.ACQUISITION_YAXIS, FallSqlHelper.ACQUISITION_ZAXIS, FallSqlHelper.ACQUISITION_ASESSION, FallSqlHelper.ACQUISITION_FALL_COLUMN};

	public AcquisitionDataSource(Context context){
		dbHelper=new FallSqlHelper(context);
	}
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	public void close() {
		dbHelper.close();
	}

	//STORE DI NUOVA ACQUISIZIONE DATI I PARAMETRI
	public Acquisition insert(long time,float xAxis,float yAxis,float zAxis, String session, int fall)throws Exception{


		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.ACQUISITION_TIME, time);
		values.put(FallSqlHelper.ACQUISITION_XAXIS,xAxis);
		values.put(FallSqlHelper.ACQUISITION_YAXIS,yAxis);
		values.put(FallSqlHelper.ACQUISITION_ZAXIS,zAxis);
		values.put(FallSqlHelper.ACQUISITION_ASESSION, session);
		values.put(FallSqlHelper.ACQUISITION_FALL_COLUMN,fall);
		database.insert(FallSqlHelper.ACQUISITION_TABLE, null,values);
		return new Acquisition(time,xAxis,yAxis,zAxis,session, fall);	

	}

	//STORE DI NUOVA ACQUISIZIONE DATA L'ACQUISIZIONE
	public Acquisition insert(Acquisition acquisition)throws Exception{


		long time=acquisition.time();
		float xAxis=acquisition.xAxis(), yAxis=acquisition.yAxis(), zAxis=acquisition.yAxis(); 
		String session=acquisition.session();
		int fall=acquisition.integerFall();

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.ACQUISITION_TIME, time);
		values.put(FallSqlHelper.ACQUISITION_XAXIS,xAxis);
		values.put(FallSqlHelper.ACQUISITION_YAXIS,yAxis);
		values.put(FallSqlHelper.ACQUISITION_ZAXIS,zAxis);
		values.put(FallSqlHelper.ACQUISITION_ASESSION, session);
		values.put(FallSqlHelper.ACQUISITION_FALL_COLUMN,fall);

		database.insert(FallSqlHelper.ACQUISITION_TABLE, null,values);

		return new Acquisition(time,xAxis,yAxis,zAxis,session, fall);	

	}

	//RESTITUISCE L'ACQUISIZIONE DATA LA CHIAVE COMPOSTA
	public Acquisition getAcquisition(long time,String session){

		Cursor cursor = database.query(FallSqlHelper.ACQUISITION_TABLE,allColumns, FallSqlHelper.ACQUISITION_TIME + " = " + time+ " AND "+FallSqlHelper.ACQUISITION_ASESSION+"='"+session+"'", null,null, null, null);
		cursor.moveToFirst();
		Acquisition a=cursorToAcquisition(cursor);
		cursor.close();
		return a;

	}

	//TRASFORMA UNA RIGA IN UN ACQUISIZIONE
	private Acquisition cursorToAcquisition(Cursor cursor) {
		if(cursor.getCount()==0)return null;

		long time=cursor.getLong(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_TIME));
		float x=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_XAXIS));
		float y=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_YAXIS));
		float z=cursor.getFloat(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_ZAXIS));
		String sessionName=cursor.getString(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_ASESSION));
		int fall=cursor.getInt(cursor.getColumnIndex(FallSqlHelper.ACQUISITION_FALL_COLUMN));


		return new Acquisition(time,x,y,z,sessionName,fall);
	}

	//RESTiTUISCE TUTTE LE ACQUISIZIONI DATO IL NOME DELLA SESSIONE
	public ArrayList<Acquisition> acquisitions(String session){
		ArrayList<Acquisition> list=new ArrayList<Acquisition>();
		Cursor cursor=database.query(FallSqlHelper.ACQUISITION_TABLE, allColumns, FallSqlHelper.ACQUISITION_ASESSION+"='"+session+"'",null,null, null, null);
		if(cursor.getCount()==0)return list;
		while(cursor.moveToNext()){
			list.add(cursorToAcquisition(cursor));
		}
		cursor.close();
		return list;

	}

	//RESTITUISCE TUTTE LE ACQUISIZIONI SEGNALATE COME 'CADUTA' DATO IL NOME DELLA SESSIONE
	public ArrayList<Acquisition> sessionFalls(String sessionName){

		ArrayList<Acquisition> list=new ArrayList<Acquisition>();
		Cursor cursor = database.query(FallSqlHelper.ACQUISITION_TABLE,allColumns, FallSqlHelper.ACQUISITION_FALL_COLUMN + " = " +1+ " AND "+FallSqlHelper.ACQUISITION_ASESSION+"='"+sessionName+"'", null,null, null, null);
		if(cursor.getCount()==0)return list;
		while(cursor.moveToNext()){
			list.add(cursorToAcquisition(cursor));
		}
		cursor.close();
		return list;

	}

	//RESTITUISCE TUTTE LE ACQUISIZIONI SEGNALATE COME 'CADUTA' DATA LA SESSIONE
	public ArrayList<Acquisition> sessionFalls(Session session){

		ArrayList<Acquisition> list=new ArrayList<Acquisition>();
		Cursor cursor = database.query(FallSqlHelper.ACQUISITION_TABLE,allColumns, FallSqlHelper.ACQUISITION_FALL_COLUMN + " = " +1+ " AND "+FallSqlHelper.ACQUISITION_ASESSION+"='"+session.getName()+"'", null,null, null, null);
		if(cursor.getCount()==0)return list;
		while(cursor.moveToNext()){
			list.add(cursorToAcquisition(cursor));
		}
		cursor.close();
		return list;

	}


}


