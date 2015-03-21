package pisada.fallDetector;

//DA FARE
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Session {
	private String name;
	private long startTime;
	private long endTime;
	private String imgString;
	private Context context;


	public Session(String name, String imgString,long startTime,long endtTime, Context context){
		this.name=name;
		this.imgString=imgString;
		this.startTime=startTime;
		this.endTime=endTime;
	}
	
	public long startTime(){return startTime;}
	
	public long endTime(){return endTime;}
	
	public String name(){return name;}
	
	public void setEndTime(long endTime){this.endTime=endTime;}
}




