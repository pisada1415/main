package pisada.fallDetector;

import java.util.ArrayList;
/*
 * classe che riempie una lista di acquisition e le fa "scadere" dopo un secondo
 * tenendo della lista solo quelle dell'ultimo secondo.
 */
public class ExpiringList {

	private final int EXPIRING_TIME = 1000;
	private ArrayList<Acquisition> timerAcquisitionList;
	private boolean stopChanging = false;
	
	public ExpiringList()
	{
		timerAcquisitionList = new ArrayList<Acquisition>();
	}
	
	public synchronized void add(Acquisition a)
	{
		if(!stopChanging){
		timerAcquisitionList.add(a);
		new Thread(){
			@Override
			public void run()
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!stopChanging && timerAcquisitionList.size()>0)
					timerAcquisitionList.remove(0);
			}
		}.start();}
	}
	
	public Acquisition get(int i)
	{
		return this.timerAcquisitionList.get(i);
	}
	
	public int size()
	{
		return this.timerAcquisitionList.size();
	}
	
	public ArrayList<Acquisition> getList()
	{
		/*
		ArrayList<Acquisition> list = new ArrayList<Acquisition>();
		for(Acquisition a : timerAcquisitionList)
			list.add(a);
		return list;
		*/
		return this.timerAcquisitionList;
	}
	
	public ArrayList<Acquisition> stopChanging(boolean bool)
	{
		stopChanging = bool;
		return this.timerAcquisitionList;
		
	}

	
}

