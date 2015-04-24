package pisada.plotmaker;

public class Data {
	public float x;
	public float y;
	
	public Data(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	public Data(double x, double y)
	{
		this.x = (float)x;
		this.y = (float)y;
	}
	public Data(Integer x, Integer y)
	{
		this.x = x.floatValue();
		this.y = y.floatValue();
	}
	public float getX()
	{
		return this.x;
	}
	public float getY()
	{
		return this.y;
	}
}
