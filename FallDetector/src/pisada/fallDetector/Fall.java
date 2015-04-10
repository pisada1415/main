package pisada.fallDetector;

import java.util.ArrayList;

public class Fall {
	private String thumbnail;
	private long time;
	private String position;
	private boolean successNotify;
	private ArrayList<Double> data;
	
	
	public Fall(String thumbnail, long time, String position, boolean successNotify, ArrayList<Double> data)
	{
		this.thumbnail = thumbnail; this.time = time;
		this.position = position; this.successNotify = successNotify;
		this.data = data;
	}
	
	public Fall(){}
	
	public void setThumbnail(String t){thumbnail = t;}
	public void setTime(long t){time = t;}
	public void setPosition(String p){position = p;}
	public void setSuccessNotify(boolean s){successNotify = s;}
	public void setData(ArrayList<Double> d){data = d;}
	
	
	public String getThumbnail(){return thumbnail;}
	public long getTime(){return time;}
	public String getPosition(){return position;}
	public boolean getSuccessNotify(){return successNotify;}
	public ArrayList<Double> getData(){return data;}
}
