package pisada.fallDetector;

import pisada.database.FallDataSource.Fall;


public interface ServiceReceiver {
	public void serviceUpdate(float x, float y, float z, long time);

	public void serviceUpdate(Fall fall, String sessionName);

	public void sessionTimeOut();
	
	public boolean equalsClass(ServiceReceiver obj);
}
