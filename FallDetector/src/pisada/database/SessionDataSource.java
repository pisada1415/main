package pisada.database;

import pisada.fallDetector.Session;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SessionDataSource {
	private SQLiteDatabase db;
	private FallSqlHelper dbHelper;
	private String[] allColumns={FallSqlHelper.NAME,FallSqlHelper.SESSION_IMG,FallSqlHelper.START_TIME,FallSqlHelper.END_TIME};
	private Context context;
	
	public SessionDataSource(Context context){
		dbHelper=new FallSqlHelper(context);
		this.context=context;
	}
	public void open() throws SQLException {
		db = dbHelper.getWritableDatabase();
	}
	

	public void close() {
		dbHelper.close();
	}
	
	//NUOVA SESSIONE
	public Session storeNewSession(String name,String img, long startTime, long endTime){
		ContentValues values=new ContentValues();
		values.put(FallSqlHelper.NAME, name);
		values.put(FallSqlHelper.SESSION_IMG, img);
		values.put(FallSqlHelper.START_TIME, startTime);
		values.put(FallSqlHelper.END_TIME, endTime);
		
		db.insert(FallSqlHelper.SESSION_TABLE, null,values);
		
		return new Session(name,img,startTime,endTime, context);
	}
	
	//COVERTE RIGA IN SESSIONE
	private Session cursorToAcquisition(Cursor cursor) {
		return new Session(cursor.getString(0),cursor.getString(1),cursor.getLong(2),cursor.getLong(3), context);
	}

}
