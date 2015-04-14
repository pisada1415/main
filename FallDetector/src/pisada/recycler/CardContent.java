package pisada.recycler;

public class CardContent {

	private String pos;
	private String time;
	private double thumbnail;
	private String link;
	
	public CardContent(String pos, String link, String time, double thumbnail)
	{
		this.pos = pos;
		this.time = time;
		this.thumbnail = thumbnail;
		this.link = link;
	}
	public CardContent()
	{
		
	}
	
	
	public String getLink()
	{
		return this.link;
	}
	public String getPos()
	{
		return this.pos;
	}
	
	public String getTime()
	{
		return this.time;
	}
	
	public double getThumbnail()
	{
		return this.thumbnail;
	}
}
