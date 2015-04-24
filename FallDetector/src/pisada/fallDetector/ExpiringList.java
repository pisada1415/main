package pisada.fallDetector;

import java.util.concurrent.ConcurrentLinkedQueue;

import pisada.database.Acquisition;


public class ExpiringList {

	private int EXPIRING_SIZE = 1000;
	protected ConcurrentLinkedQueue<Acquisition> timerAcquisitionList ; //NON VA PROTECTED. SOLO PER DEBUG
	
	public ExpiringList()
	{
		timerAcquisitionList = new ConcurrentLinkedQueue<Acquisition>();
		int sizeBasedOnUpdateRate = (Integer) (1000/ForegroundService.MAX_SENSOR_UPDATE_RATE);
		EXPIRING_SIZE = sizeBasedOnUpdateRate > 10 ? sizeBasedOnUpdateRate : 10;
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

