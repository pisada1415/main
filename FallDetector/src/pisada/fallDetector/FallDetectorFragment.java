package pisada.fallDetector;



import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;


/*
 * faccio una classe astratta da sottoclassare per far sì che possa usare un fragment generico nella Main
 */
public abstract class FallDetectorFragment extends Fragment{
	public RecyclerView rView;
	public void playPauseService(View v){};
	public void stopService(View v){};
	public String getSessionName(){return null;};
	public void addSession(View v){};
	public void currentSessionDetails(View v){};
	public void setSessionName(String name){};
	public int getType(){return -29891;};
	public long getFallTime(){return -1;}
	public int getListPosition(){return ((LinearLayoutManager)rView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();}
	public void scroll(int index){rView.getLayoutManager().scrollToPosition(index);}
	@Override
	public void onAttach(Activity a){
		super.onAttach(a);
		a.invalidateOptionsMenu();
	}
}
