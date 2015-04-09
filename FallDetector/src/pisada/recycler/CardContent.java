package pisada.recycler;

public class CardContent {

	private double x;
	private double y;
	private double z;
	private long time;
	
	public CardContent(double x, double y, double z, long time)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.time = time;
	}
	public CardContent()
	{
		
	}
	
	public double getX()
	{
		return this.x;
	}
	public double getY()
	{
		return this.y;
	}
	public double getZ()
	{
		return this.z;
	}
	public double getTime()
	{
		return this.time;
	}
}
