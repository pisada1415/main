package pisada.fallDetector;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;
public class Utility {

	
	public static String checkLocationServices(final Context context, boolean showDialog)
	{
		

		LocationManager lm = null;
		 boolean gps_enabled = false,network_enabled = false;
		    if(lm==null)
		        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		    try{	
		    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		    }catch(IllegalArgumentException ex){
		    	Toast.makeText(context, "Can't get location from GPS", Toast.LENGTH_SHORT).show();
		    }
		    try{
		    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		    }catch(IllegalArgumentException ex){
		    	Toast.makeText(context, "Can't get location from network provider", Toast.LENGTH_SHORT).show();

		    }

		   if(!gps_enabled && !network_enabled && showDialog){
		        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		        dialog.setMessage("GPS is not enabled");
		        dialog.setPositiveButton("Open settings", new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
		                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		                context.startActivity(myIntent);
		            }
		        });
		        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

		            }
		        });
		        
		        dialog.show();

		    }
		   if(gps_enabled)
			   return LocationManager.GPS_PROVIDER;
		   if(network_enabled)
			   return LocationManager.NETWORK_PROVIDER;
		   return null;
	}
	
	
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public static int randomizeToColor(double d)
	{
		if(d <= 255)
			return randInt(0, (int)d);
		else
		{
			return randomizeToColor(d/randInt(1, 3));
		}
	}
	
	public static String getStringTime(long timeMillis)
	{
		final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy_hh:mm:ss");
		// milliseconds to date 
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		Date date = calendar.getTime();
		return formatter.format(date);

	}
	
	public static String getMapsLink(double lat, double lng){
		return "https://www.google.com/maps/@" + lat+"," + lng + ",13z";
	}
	
	public static Bitmap createImage(int sessionNumber){
		Bitmap icon=Bitmap.createBitmap(200, 200, Config.RGB_565);
		Canvas canvas=new Canvas(icon);
		Paint paint=new Paint();
		canvas.drawLine(0,0, 100, 100, paint);
		return icon;
	
	}
	
}
