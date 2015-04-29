package pisada.fallDetector;

import android.support.v4.app.Fragment;
import android.view.View;


/*
 * faccio una classe astratta da sottoclassare per far sì che possa usare un fragment generico nella Main
 */
public abstract class FallDetectorFragment extends Fragment{
	public void playPauseService(View v){};
	public void stopService(View v){};
	public String getSessionName(){return null;};
	public void addSession(View v){};
	public void currentSessionDetails(View v){};
	public void setSessionName(String name){};
}
