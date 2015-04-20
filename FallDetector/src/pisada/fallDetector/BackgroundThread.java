package pisada.fallDetector;

import pisada.database.Acquisition;

public class BackgroundThread extends Thread {

	Acquisition acquisition;
	public BackgroundThread(Acquisition acq)
	{
		acquisition = acq;
	}
	
	public Acquisition getAcquisition(){
		return this.acquisition;
	}
}
