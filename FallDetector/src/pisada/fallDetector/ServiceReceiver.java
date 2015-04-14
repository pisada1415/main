package pisada.fallDetector;


public interface ServiceReceiver {
	
	public void serviceUpdate(float x, float y, float z, long time);
	public void serviceUpdate(String fallPosition, String link, String time, long img);

}
