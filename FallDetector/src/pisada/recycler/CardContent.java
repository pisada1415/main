package pisada.recycler;

/*
 * due oggetti cardcontent considerati uguali quando hanno uguale id e tempo
 * cadute hanno tutte id = 0.
 * serve a distinguere le prime due card tra loro e dalle cadute
 */
public class CardContent {

	private String pos;
	private String timeLiteral;
	private long time;
	private String link;
	private boolean notifSuccessfull;
	private static int count;
	private int id;
	
	@Override
	public boolean equals(Object o){
		CardContent cc;
		if(o instanceof CardContent)
		cc = (CardContent) o;
		else return false;
		if(cc.id == this.id  && cc.timeLiteral.equals(this.timeLiteral))
			return true;
		return false;
	}
	
	public CardContent(String pos, String link, String timeLiteral, long time,  boolean notifSuccessfull)
	{
		this.pos = pos;
		this.timeLiteral = timeLiteral;
		this.link = link;
		this.notifSuccessfull = notifSuccessfull;
	}
	public CardContent()
	{
		id = ++count;
	}
	
	public boolean notifiedSuccess()
	{
		return this.notifSuccessfull;
	}
	
	public String getLink()
	{
		return this.link;
	}
	public String getPos()
	{
		return this.pos;
	}
	
	public String getTimeLiteral()
	{
		return this.timeLiteral;
	}
	
	public long getTime()
	{
		return this.time;
	}
	
	
}
