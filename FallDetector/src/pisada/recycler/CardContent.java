package pisada.recycler;

/*
 * due oggetti cardcontent considerati uguali quando hanno uguale id e tempo
 */
public class CardContent {

	private String pos;
	private String time;
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
		if(cc.id == this.id  && cc.time.equals(this.time))
			return true;
		return false;
	}
	
	public CardContent(String pos, String link, String time,  boolean notifSuccessfull)
	{
		this.pos = pos;
		this.time = time;
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
	
	public String getTime()
	{
		return this.time;
	}
	
	
}
