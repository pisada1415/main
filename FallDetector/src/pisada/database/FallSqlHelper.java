package pisada.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class FallSqlHelper extends SQLiteOpenHelper{

	private static final String DATABASE_NAME="fall.db";
	private static final int DATABASE_VERSION=1;
	public static final int NO_VALUE_FOR_TIME_COLUMN=-1;


	//DEFINIZIONE TABELLA SESSIONE
	public static final String SESSION_TABLE="SESSION";
	public static final String SESSION_NAME="Name";
	public static final String SESSION_IMG="Img";
	public static final String SESSION_START_TIME="StartTime";
	public static final String SESSION_END_TIME="EndTime";
	public static final String SESSION_CLOSE_COLUMN="Close";
	public static final String SESSION_DURATION="Duration";
	public static final String SESSION_STOP_TIME_PREFERENCE="StopTimePreference";
	public static final String SESSION_PAUSE_COLUMN="Pause";
	public static final int CLOSE=1;
	public static final int OPEN=0;
	public static final int PAUSE=1;
	public static final int RUNNING=0;


	//DEFINIZIONE TABELLA SESSSIONE
	public static final String CREATE_SESSION_TABLE=
			"CREATE TABLE IF NOT EXISTS "+SESSION_TABLE+"("
					+SESSION_NAME+ " TEXT PRIMARY KEY, "
					+SESSION_START_TIME+ " INTEGER NOT NULL, "
					+SESSION_END_TIME+ " INTEGER DEFAULT -1, "
					+SESSION_IMG+" TEXT, "
					+SESSION_CLOSE_COLUMN+ " INTEGER DEFAULT 0, "
					+SESSION_PAUSE_COLUMN+ " INTEGER DEFAULT 0, "
					+SESSION_DURATION+" INTEGER DEFAULT 0, "
					+SESSION_STOP_TIME_PREFERENCE+" INTEGER DEFAULT -1,"
					+ "CHECK("+SESSION_NAME+" != ''), "
					+ "CHECK("+SESSION_CLOSE_COLUMN+" = "+0+" OR "+SESSION_CLOSE_COLUMN+" = "+1+"));";

	//DEFINIZIONE TABELLA CADUTA
	public static final String FALL_TABLE="FALL"; 
	public static final String FALL_TIME="FallTime";
	public static final String FALL_FSESSION="FallSession";
	public static final String FALL_LNG="Lng";
	public static final String FALL_LAT="Lat";

	public static final String CREATE_FALL_TABLE=	
			"CREATE TABLE IF NOT EXISTS "+FALL_TABLE+"("+
					FALL_TIME+" INTEGER, "+
					FALL_FSESSION+" TEXT,"+
					FALL_LAT+" REAL NOT NULL,"+
					FALL_LNG+" REAL NOT NULL,"+
					"PRIMARY KEY ("+FALL_TIME+","+FALL_FSESSION+") "+
					"FOREIGN KEY ("+FALL_FSESSION+") REFERENCES "+SESSION_TABLE+"("+SESSION_NAME+")"+
					");";

	//DEFINIZIONE TABELLA AQUISIZIONE
	public static final String ACQUISITION_TABLE="ACQUISITION"; 
	public static final String ACQUISITION_TIME="AcquisitionTime";
	public static final String ACQUISITION_FALL_TIME="AcquisitionFallTime";
	public static final String ACQUISITION_ASESSION="AcquisitionSession";
	public static final String ACQUISITION_XAXIS="X";
	public static final String ACQUISITION_YAXIS="Y";
	public static final String ACQUISITION_ZAXIS="Z";



	public static final String CREATE_ACQUISITION_TABLE=	
			"CREATE TABLE IF NOT EXISTS "+ACQUISITION_TABLE+"("+
					ACQUISITION_TIME+" INTEGER, "+
					ACQUISITION_FALL_TIME+" INTEGER,"+
					ACQUISITION_ASESSION+" TEXT,"+
					ACQUISITION_XAXIS+" REAL NOT NULL,"+
					ACQUISITION_YAXIS+" REAL NOT NULL,"+
					ACQUISITION_ZAXIS+ " REAL NOT NULL,"+
					"PRIMARY KEY ("+ACQUISITION_TIME+","+ACQUISITION_FALL_TIME+","+ACQUISITION_ASESSION+") "+
					"FOREIGN KEY ("+ACQUISITION_FALL_TIME+","+ACQUISITION_ASESSION+") REFERENCES "+FALL_TABLE+"("+FALL_TIME+","+FALL_FSESSION+")"+
					");";


	public FallSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	//CREA TABELLE 
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SESSION_TABLE);
		db.execSQL(CREATE_FALL_TABLE);
		db.execSQL(CREATE_ACQUISITION_TABLE);

	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
