package pisada.fallDetector;

import java.io.IOException;

import pisada.database.SessionDataSource;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;



public class StartSessionReminderActivity extends Activity {
	MediaPlayer mp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		
		SessionDataSource s = new SessionDataSource(this);
		mp = new MediaPlayer();
		if(s.existCurrentSession())
			this.finish();
		else{
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			setContentView(R.layout.activity_start_session_reminder);
			Button stopAlarm = (Button) findViewById(R.id.startCurrentSessionActivity);
			stopAlarm.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if(mp.isPlaying())
						mp.stop();
					Intent intent = new Intent(StartSessionReminderActivity.this, CurrentSessionFragment.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
					startActivity(intent);
					StartSessionReminderActivity.this.finish();
				}
			});
			
			try {
				mp.setDataSource(this, defaultRingtoneUri);
				mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
				mp.prepare();
				mp.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp)
					{
						mp.release();
					}
				});
				mp.start();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			playSound(this, defaultRingtoneUri);
		}
	}
	private void playSound(final Context context, Uri alert) {


		Thread background = new Thread(new Runnable() {
			public void run() {
				try {

					mp.start();

				} catch (Throwable t) {
					Log.i("Animation", "Thread  exception "+t);
				}   
			}
		});
		background.start();
	}

	   
}