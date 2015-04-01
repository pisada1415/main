package pisada.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class FallSqlHelper extends SQLiteOpenHelper{

	private static final String DATABASE_NAME="fall.db";
	private static final int DATABASE_VERSION=1;
	

	//DEFINIZIONE TABELLA SESSIONE
	public static final String SESSION_TABLE="SESSION";
	public static final String SESSION_NAME="Name";
	public static final String SESSION_IMG="Img";
	public static final String SESSION_START_TIME="StartTime";
	public static final String SESSION_END_TIME="EndTime";
	public static final String SESSION_CLOSE_COLUMN="Close";
	public static final int CLOSE=1;
	public static final int OPEN=0;

	public static final String CREATE_SESSION_TABLE=
			"CREATE TABLE IF NOT EXISTS "+SESSION_TABLE+"("
					+SESSION_NAME+ " TEXT PRIMARY KEY, "
					+SESSION_START_TIME+ " INTEGER NOT NULL, "
					+SESSION_END_TIME+ " INTEGER, "
					+SESSION_IMG+" TEXT, "
					+SESSION_CLOSE_COLUMN+ " INTEGER NOT NULL "
					+ "CHECK("+SESSION_CLOSE_COLUMN+" = "+0+" OR "+SESSION_CLOSE_COLUMN+" = "+1+"));";

	//DEFINIZIONE TABELLA AQUISIZIONE
	public static final String ACQUISITION_TABLE="ACQUISITION"; 
	public static final String ACQUISITION_TIME="Time";
	public static final String ACQUISITION_XAXIS="X";
	public static final String ACQUISITION_YAXIS="Y";
	public static final String ACQUISITION_ZAXIS="Z";
	public static final String ACQUISITION_ASESSION="Session";
	public static final String ACQUISITION_FALL_COLUMN="Fall";

	public static final String CREATE_ACQUISITION_TABLE=	
			"CREATE TABLE IF NOT EXISTS "+ACQUISITION_TABLE+"("+
					ACQUISITION_TIME+" INTEGER NOT NULL, "+
					ACQUISITION_XAXIS+" REAL NOT NULL,"+
					ACQUISITION_YAXIS+" REAL NOT NULL,"+
					ACQUISITION_ZAXIS+ " REAL NOT NULL,"+
					ACQUISITION_ASESSION+" TEXT NOT NULL ,"+
					ACQUISITION_FALL_COLUMN+" INTEGER NOT NULL,"+
					"CHECK("+ACQUISITION_FALL_COLUMN+" = "+0+" OR "+ACQUISITION_FALL_COLUMN+" = "+1+"),"+
					"PRIMARY KEY ("+ACQUISITION_TIME+","+ACQUISITION_ASESSION+") "+
					"FOREIGN KEY ("+ACQUISITION_ASESSION+") REFERENCES "+SESSION_TABLE+"("+SESSION_NAME+")"+
					");";


	public FallSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	//CREA TABELLE 
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SESSION_TABLE);
		db.execSQL(CREATE_ACQUISITION_TABLE);

	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
