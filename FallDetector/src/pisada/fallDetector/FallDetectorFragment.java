package pisada.fallDetector;

import android.support.v4.app.Fragment;
import android.view.View;


/*
 * faccio una classe da sottoclassare così non devo implementare tutti i metodi di una eventuale interfaccia in ogni fragment (sarebbero tanti)
 */
public class FallDetectorFragment extends Fragment{
	public void playPauseService(View v){};
	public void stopService(View v){};
	public String getSessionName(){return null;};
	public void setSessionName(String s){};
	public void setFallTime(long timeMillis){};
	public void addSession(View v){};
}
