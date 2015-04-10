package pisada.fallDetector;

import pisada.database.FallDataSource.Fall;

//DA FARE
//Samuele gay
public class Acquisition {
	private long time;
	private float xAxis;
	private float yAxis;
	private float zAxis;
	boolean isValid=true;

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
	public boolean isValidAcquisition(){return isValid;}



}
