package pisada.fallDetector.smSender;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSender {

	private static boolean wait = false;
	private static final int TIMEOUT = 30000;
	
	public static void sendSMSToList(final ArrayList<String> list, final Context ctx, final String message)
	{
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					waitingTime = 0;
					sendSMS(number, ctx, message);
				}
			}
		}.start();
	}

	private static void sendSMS(String number, final Context ctx, String message){


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
					Toast.makeText(ctx.getApplicationContext(), "SMS sent", 
							Toast.LENGTH_SHORT).show();

					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(ctx.getApplicationContext(), "Generic failure", 
							Toast.LENGTH_SHORT).show();

					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(ctx.getApplicationContext(), "No service", 
							Toast.LENGTH_SHORT).show();

					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(ctx.getApplicationContext(), "Null PDU", 
							Toast.LENGTH_SHORT).show();

					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(ctx.getApplicationContext(), "Radio off", 
							Toast.LENGTH_SHORT).show();

					break;
				default:
					wait = false;
					break;
				}
			}
		}, new IntentFilter(SENT));

		//---when the SMS has been delivered---
		ctx.registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					Toast.makeText(ctx.getApplicationContext(), "SMS delivered", 
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(ctx.getApplicationContext(), "SMS not delivered", 
							Toast.LENGTH_SHORT).show();
					break;                        
				}
			}
		}, new IntentFilter(DELIVERED));        

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(number, null, message, sentPI, deliveredPI);   
		wait = true;
	}

}



