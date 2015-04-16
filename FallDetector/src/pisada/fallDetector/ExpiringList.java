package pisada.fallDetector;

import java.util.concurrent.ConcurrentLinkedQueue;


public class ExpiringList {

	private final int EXPIRING_SIZE = 1000 / ForegroundService.MAX_SENSOR_UPDATE_RATE;
	protected ConcurrentLinkedQueue<Acquisition> timerAcquisitionList ; //NON VA PROTECTED. SOLO PER DEBUG
	
	public ExpiringList()
	{
		timerAcquisitionList = new ConcurrentLinkedQueue<Acquisition>();
	}
	
	public void enqueue(Acquisition a)
	{
		
		timerAcquisitionList.add(a);

		if(size() >= EXPIRING_SIZE)
			timerAcquisitionList.poll();
		
		
	}

	public Acquisition peek()
	{
		return timerAcquisitionList.peek();
	}
	
	
	public int size()
	{
		return this.timerAcquisitionList.size();
	}
	
	public Object[] getArray()
	{
		/*
		ArrayList<Acquisition> list = new ArrayList<Acquisition>();
		for(Acquisition a : timerAcquisitionList)
			list.add(a);
		return list;
		*/
		return this.timerAcquisitionList.toArray();
	}
	
	public ConcurrentLinkedQueue<Acquisition> getQueue(){return timerAcquisitionList;}
	
}

