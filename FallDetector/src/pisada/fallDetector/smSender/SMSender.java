package pisada.fallDetector.smSender;

import java.util.ArrayList;

import pisada.database.FallDataSource;
import pisada.fallDetector.ForegroundService;
import pisada.fallDetector.ServiceReceiver;
import pisada.recycler.CurrentSessionCardAdapter;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

/*
 * in teoria funziona cos�:
 * quando un sms viene mandato con successo, la lista nell'adapter dell'altra activity viene aggiornata.
 * il contenuto di una card � considerato uguale se cambia solo il valore boolean della notifica mandata
 * con successo (vedi sovrascrittura equals nella classe CardContent pacchetto pisada.recycler). questo fa si
 * che venga scambiata la card con quella nuova che contiene il valore aggiornato del campo booleano "notificainviata". poi boh non so se va.
 * 
 */

public class SMSender {

	private boolean wait = false;
	private final int TIMEOUT = 30000;
	private FallDataSource fds;
	public void sendSMSToList(final ArrayList<String> list, final Context ctx, final String message, final FallDataSource.Fall fall)
	{
		fds = new FallDataSource(ctx);

		if(list.size() != 0)
		new Thread(){
			@Override
			public void run()
			{
				for(final String number : list)
				{
					int waitingTime = 0;
					while(wait && waitingTime < TIMEOUT)
						try {
							Thread.sleep(1000);
							waitingTime += 1000;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					waitingTime = 0;
					sendSMS(number, ctx, message, fall);
				}
			}
		}.start();
	}

	private  void sendSMS(String number, final Context ctx, String message, final FallDataSource.Fall fall){


		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";


		PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0,
				new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
				new Intent(DELIVERED), 0);

		//---when the SMS has been sent---
		ctx.registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					//Toast.makeText(ctx.getApplicationContext(), "SMS sent", 
						//	Toast.LENGTH_SHORT).show();
					fds.setNotificationSuccess(fall, true);
					
					if(ForegroundService.connectedActs != null && ForegroundService.connectedActs.size() > 0){

						for(final ServiceReceiver sr : ForegroundService.connectedActs){ 
							Runnable r = new Runnable(){@Override public void run() {sr.serviceUpdate(fall, fall.getSessionName());}};
							if(sr instanceof CurrentSessionCardAdapter)
								((CurrentSessionCardAdapter)sr).runOnUiThread(r);
							else if(sr instanceof Activity)
								((Activity)sr).runOnUiThread(r);
						}
					}
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				//	Toast.makeText(ctx.getApplicationContext(), "Generic failure", 
					//		Toast.LENGTH_SHORT).show();

					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					//Toast.makeText(ctx.getApplicationContext(), "No service", 
						//	Toast.LENGTH_SHORT).show();

					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					//Toast.makeText(ctx.getApplicationContext(), "Null PDU", 
						//	Toast.LENGTH_SHORT).show();

					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					//Toast.makeText(ctx.getApplicationContext(), "Radio off", 
						//	Toast.LENGTH_SHORT).show();

					break;
				default:
					wait = false;
					break;
				}
			}
		}, new IntentFilter(SENT));

		      

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(number, null, message, sentPI, deliveredPI);   
		wait = true;
	}

}



