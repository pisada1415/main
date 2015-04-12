package pisada.fallDetector;

import java.util.concurrent.ConcurrentLinkedQueue;
/*
 * classe che riempie una lista di acquisition e le fa "scadere" dopo un secondo
 * tenendo della lista solo quelle dell'ultimo secondo.
 */


/*
 * FA CAGARE DIO SJDIASDOIJDOIJFOIEWFJOEWIJFOEIJFOIEWJFOIJ
 */
public class ExpiringList {

	private final int EXPIRING_SIZE = 1000;
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
	

	
}

