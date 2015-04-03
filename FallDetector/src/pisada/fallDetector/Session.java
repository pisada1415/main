package pisada.fallDetector;

//DA FARE
import pisada.database.BoolNotBoolException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Session {
	private String name;
	private long startTime;
	private long endTime;
	private String img;
	private Context context;
	private long stopTimePreference;
	private int close;

//CON STOPTIME
	public Session(String name, String img,long startTime,long endTime,long stopTimePreference, int close, Context context) throws BoolNotBoolException{
		if(close!=0&&close!=1)throw new BoolNotBoolException();
		else{
			this.name=name;
			this.img=img;
			this.startTime=startTime;
			this.endTime=endTime;
			this.close=close;
			this.stopTimePreference=stopTimePreference;
		}

	}
	
	//SENZA STOPTIME
	public Session(String name, String img,long startTime,long endTime, int close, Context context) throws BoolNotBoolException{
		if(close!=0&&close!=1)throw new BoolNotBoolException();
		else{
			this.name=name;
			this.img=img;
			this.startTime=startTime;
			this.endTime=endTime;
			this.close=close;
		}

	}
	//ritorna sessione vuota
	public Session(){

	}
	public long startTime(){return startTime;}
	public long endTime(){return endTime;}
	public String name(){return name;}
	public boolean booleanIsClose(){return close==1;}
	public long stopTimePreference(){return stopTimePreference;}
	
	
	public int integerIsClose(){return close;}
	public String img(){return img;}
	public void setEndTime(long endTime){this.endTime=endTime;}
	public void setClose(long endTime){close=1;this.endTime=endTime;}
	public void setStopTimePreference(long t){stopTimePreference=t;}
	
	public boolean isValidSession() {return name!=null && startTime!=0;}
	public boolean hasStopTimePreference(){return stopTimePreference!=0;}
}




