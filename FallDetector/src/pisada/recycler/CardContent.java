package pisada.recycler;

public class CardContent {

	private String pos;
	private String time;
	private String link;
	private boolean notifSuccessfull;
	private long id;
	
	@Override
	public boolean equals(Object o){
		CardContent cc;
		if(o instanceof CardContent)
		cc = (CardContent) o;
		else return false;
		if(cc.id == this.id && cc.pos.equals(this.pos) && cc.time.equals(this.time)  && this.link.equals(cc.link))
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
		id = System.currentTimeMillis();
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
