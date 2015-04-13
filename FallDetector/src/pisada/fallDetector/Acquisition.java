package pisada.fallDetector;

import pisada.database.FallDataSource.Fall;
import pisada.database.SessionDataSource.Session;

//DA FARE
//Samuele gay
public class Acquisition {
	private long time;
	private float xAxis;
	private float yAxis;
	private float zAxis;
	boolean isValid=true;
	private Session session;
	private Fall fall;

	public Acquisition(long time,float xAxis,float yAxis, float zAxis){
		this.time=time;
		this.xAxis=xAxis;
		this.yAxis=yAxis;
		this.zAxis=zAxis;
	}
	public Acquisition(){
		isValid=false;
	}

	public long getTime(){	return time;}
	public float getXaxis(){return xAxis;}
	public float getYaxis(){return yAxis;}
	public float getZaxis(){return zAxis;}
	public Session getSession(){return session;}
	public Fall getFall(){return fall;}
	public boolean isValidAcquisition(){return isValid;}
	
	
	/*****************NON USARE**********************/
	public void setSession(Session session){this.session=session;}
	public void setFall(Fall fall){this.fall=fall;}
	/*****************NON USARE**********************/

}
