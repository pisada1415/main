package pisada.fallDetector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

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
	
	public static boolean playServicesAvailable(Context ctx) {
	    int isAvailable = GooglePlayServicesUtil
	            .isGooglePlayServicesAvailable(ctx);
	    if (isAvailable == ConnectionResult.SUCCESS) {
	        return true;
	    } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
	        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable,(Activity)ctx, 0);
	        dialog.show();
	    } else {
	        Toast.makeText(ctx, "Connect Connect to Maps", Toast.LENGTH_SHORT).show();

	    }
	    return false;
	}
	
}
