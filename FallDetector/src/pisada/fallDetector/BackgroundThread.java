package pisada.fallDetector;

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
