package pisada.fallDetector;
//DA FARE
public class Acquisition {
	private long time;
	private float xAxis;
	private float yAxis;
	private float zAxis;
	private String aSession;
	private int fall;

	public Acquisition(long time,float xAxis,float yAxis, float zAxis, String aSession, int fall){
		this.time=time;
		this.xAxis=xAxis;
		this.yAxis=yAxis;
		this.zAxis=zAxis;
		this.aSession=aSession;
		this.fall=fall;

	}

	public long time(){	return time;}
	public float xAxis(){return xAxis;}
	public float yAxis(){return yAxis;}
	public float zAxis(){return zAxis;}
	public boolean booleanFall(){
		if(fall==0)
			return false;
		return true;
	}
	public int integerFall(){return fall;}

	public String session(){return aSession;}


}
