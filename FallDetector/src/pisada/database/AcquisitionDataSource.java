package pisada.database;






import java.util.ArrayList;

import pisada.fallDetector.Acquisition;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class AcquisitionDataSource {
	private SQLiteDatabase database;
	private FallSqlHelper dbHelper;
	private String[] allColumns = {FallSqlHelper.TIME,FallSqlHelper.XAXIS, FallSqlHelper.YAXIS, FallSqlHelper.ZAXIS, FallSqlHelper.ASESSION, FallSqlHelper.FALL};

	public AcquisitionDataSource(Context context){
		dbHelper=new FallSqlHelper(context);
	}
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	public void close() {
		dbHelper.close();
	}

	//NUOVA ACQUISIZIONE
	public Acquisition insert(long time,float xAxis,float yAxis,float zAxis, String session, int bool)throws Exception{

		if(bool!=0 && bool!=1)throw new BoolNotBoolException("bool deve essere o 0 o 1");

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.TIME, time);
		values.put(FallSqlHelper.XAXIS,xAxis);
		values.put(FallSqlHelper.YAXIS,yAxis);
		values.put(FallSqlHelper.ZAXIS,zAxis);
		values.put(FallSqlHelper.ASESSION, session);
		values.put(FallSqlHelper.FALL,bool);
		database.insert(FallSqlHelper.ACQUISITION_TABLE, null,values);

		return new Acquisition(time,xAxis,yAxis,zAxis,session, bool);	
	}

	//NUOVA ACQUISIZIONE
	public Acquisition insert(Acquisition acquisition)throws Exception{


		long time=acquisition.time();
		float xAxis=acquisition.xAxis(), yAxis=acquisition.yAxis(), zAxis=acquisition.yAxis(); 
		String session=acquisition.session();
		int bool=acquisition.integerFall();

		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.TIME, time);
		values.put(FallSqlHelper.XAXIS,xAxis);
		values.put(FallSqlHelper.YAXIS,yAxis);
		values.put(FallSqlHelper.ZAXIS,zAxis);
		values.put(FallSqlHelper.ASESSION, session);
		values.put(FallSqlHelper.FALL,bool);

		database.insert(FallSqlHelper.ACQUISITION_TABLE, null,values);

		return new Acquisition(time,xAxis,yAxis,zAxis,session, bool);	
	}

	//RESTITUISCE L'ACQUISIZIONE DATA LA CHIAVE COMPOSTA
	public Acquisition getAcquisition(long time,String session){

		Cursor cursor = database.query(FallSqlHelper.ACQUISITION_TABLE,allColumns, FallSqlHelper.TIME + " = " + time+ " AND "+FallSqlHelper.ASESSION+"='"+session+"'", null,null, null, null);
		cursor.moveToFirst();
		Acquisition a=cursorToAcquisition(cursor);
		cursor.close();
		return a;

	}

	//TRASFORMA UNA RIGA IN UN ACQUISIZIONE
	private Acquisition cursorToAcquisition(Cursor cursor) {
		return new Acquisition(cursor.getLong(0),cursor.getFloat(1),cursor.getFloat(2),cursor.getFloat(3),cursor.getString(4),cursor.getInt(5));
	}

	//RESTiTUISCE TUTTE LE ACQUISIZIONI
	public ArrayList<Acquisition> acquisitions(String session){
		ArrayList<Acquisition> list=new ArrayList<Acquisition>();
		Cursor cursor=database.query(FallSqlHelper.ACQUISITION_TABLE, allColumns, FallSqlHelper.ASESSION+"='"+session+"'",null,null, null, null);
		cursor.moveToFirst();

		while(cursor.moveToNext()){
			list.add(cursorToAcquisition(cursor));
		}

		return list;

	}
}

class BoolNotBoolException extends Exception{
	public BoolNotBoolException() { super(); }
	public BoolNotBoolException(String message) { super(message); }

}
