package pisada.fallDetector;

import pisada.database.FallDataSource;
import pisada.database.SessionDataSource;
import android.content.Context;

public class Monitor {
	
	//coda di thread creata automaticamente nel "class-object"
	/*
	 * Since only one class object exists in the Java VM per class, only one thread can 
	 * execute inside a static synchronized method in the same class.
	 */
	
	private static SessionDataSource sds;
	private static FallDataSource fds;
	
	public static synchronized void initSessionDataSource(Context c){
		sds = new SessionDataSource(c);
	}
	
	public static synchronized void initFallDataSource(Context c){
		fds = new FallDataSource(c);
	}
	
	public static synchronized void insertFall(){
		//...
	}

}
