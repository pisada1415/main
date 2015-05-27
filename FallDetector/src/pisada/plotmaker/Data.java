package pisada.plotmaker;
/*
 * classe Data (dati) usata al posto della classe point di java
 * perché abbiamo bisogno di valori float
 */
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
