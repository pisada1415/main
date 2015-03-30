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
	private int close;


	public Session(String name, String img,long startTime,long endTime, int close, Context context){
		this.name=name;
		this.img=img;
		this.startTime=startTime;
		this.endTime=endTime;
		this.close=close;
		
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
	}
	public long startTime(){return startTime;}
	
	public long endTime(){return endTime;}
	
	public String name(){return name;}
	
	public boolean booleanIsClose(){
		if(close==0)
			return false;
		return true;
	}
	public int integerIsClose(){return close;}
	public String img(){return img;
	}
	
	public void setEndTime(long endTime){this.endTime=endTime;}
}




