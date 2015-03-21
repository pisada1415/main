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
	public static final String NAME="Name";
	public static final String SESSION_IMG="Img";
	public static final String START_TIME="StartTime";
	public static final String END_TIME="EndTime";

	public static final String CREATE_SESSION_TABLE=
			"CREATE TABLE IF NOT EXISTS "+SESSION_TABLE+"("
					+NAME+ " TEXT PRIMARY KEY, "
					+START_TIME+ " INTEGER NOT NULL, "
					+END_TIME+ " INTEGER, "
					+SESSION_IMG+" );";

	//DEFINIZIONE TABELLA AQUISIZIONE
	public static final String ACQUISITION_TABLE="ACQUISITION"; 
	public static final String TIME="Time";
	public static final String XAXIS="X";
	public static final String YAXIS="Y";
	public static final String ZAXIS="Z";
	public static final String ASESSION="Session";
	public static final String FALL="Fall";
	public static final String CREATE_ACQUISITION_TABLE=	
	"CREATE TABLE IF NOT EXISTS "+ACQUISITION_TABLE+"("+
			TIME+" INTEGER NOT NULL, "+
			XAXIS+" REAL NOT NULL,"+
			YAXIS+" REAL NOT NULL,"+
			ZAXIS+ " REAL NOT NULL,"+
			ASESSION+" TEXT NOT NULL ,"+
			FALL+" INTEGER NOT NULL,"+
			"PRIMARY KEY ("+TIME+","+ASESSION+") "+
			"FOREIGN KEY ("+ASESSION+") REFERENCES "+SESSION_TABLE+"("+NAME+")"+
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
