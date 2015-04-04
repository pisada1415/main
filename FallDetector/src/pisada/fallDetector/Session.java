package pisada.fallDetector;

//DA FARE
import java.util.ArrayList;

import pisada.database.BoolNotBoolException;
import android.content.Context;

public class Session {
	private String name;
	private long startTime;
	private long endTime;
	private String img;
	private Context context;
	private long stopTimePreference;
	private int close;
	private ArrayList<Fall> fallList;


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
			fallList = new ArrayList<Fall>();

			stopTimePreference=-1;
		}

	}
	//ritorna sessione vuota
	public Session(){

		name="NONE";
		startTime=0;
		endTime=0;
		img="NONE";
		context=null;
		//valore booleano non ammesso;
		close=2;
		fallList = new ArrayList<Fall>();

	}
	public long startTime(){return startTime;}
	public long endTime(){return endTime;}
	public String name(){return name;}
	public boolean booleanIsClose(){return close==1;}
	public long stopTimePreference(){return stopTimePreference;}


	public int integerIsClose(){return close;}
	public String img(){return img;}
	public void setEndTime(long endTime){this.endTime=endTime;}

	public void addFall(Fall f){
		fallList.add(f);
	}
	
	public ArrayList<Fall> getFalls(){
		return this.fallList;
	}

	public void setClose(long endTime){close=1;this.endTime=endTime;}

	public void setStopTimePreference(long t) {stopTimePreference=t;}
	
	public boolean isValidSession() {return name!=null && startTime!=0;}
	public boolean hasStopTimePreference(){return stopTimePreference<0;}

}




