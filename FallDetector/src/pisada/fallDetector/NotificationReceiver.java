package pisada.fallDetector;

import pisada.database.SessionDataSource;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

	  
	
	  @Override
	  public void onReceive (Context context, Intent intent)
	    {
		 Toast.makeText(context, "ARRIVATA", Toast.LENGTH_LONG).show();
		  NotificationManager nm;
		  SessionDataSource s = new SessionDataSource(context);
		  if(s.existCurrentSession())
			  return;
		  else{
		  Intent notificationIntent = new Intent(context, CurrentSessionActivity.class);
			notificationIntent.setAction(Intent.ACTION_MAIN);
			notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //per far si che risvegli l'activity se sta già runnando e non richiami oncreate
			PendingIntent contentIntent = PendingIntent.getActivity(context,
					232717, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			Resources res = context.getResources();
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

			builder.setContentIntent(contentIntent)
			.setSmallIcon(R.drawable.notificationicon)
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
			.setContentTitle(res.getString(R.string.remember1st))
			.setContentText(res.getString(R.string.remember))
			.setAutoCancel(true);
			
			Notification n = builder.build();

			nm.notify(232717, n);
		  }
	  }

	}