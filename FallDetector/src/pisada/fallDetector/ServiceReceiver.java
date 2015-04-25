package pisada.fallDetector;


public interface ServiceReceiver {
	
	public void serviceUpdate(float x, float y, float z, long time);
	public void serviceUpdate(String fallPosition, String link, String timeLiteral, long time, boolean b);
	public void sessionTimeOut();
}
