package pisada.fallDetector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.location.LocationManager;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.Settings;
import android.widget.Toast;
@SuppressLint("SimpleDateFormat")
public class Utility {

	public final static String FALL_TIME_KEY = "fall_time";
	public final static String SESSION_NAME_KEY = "session_name";

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
	
	public static String getStringHour(long timeMillis)
	{
		final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
		// milliseconds to date 
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		Date date = calendar.getTime();
		return formatter.format(date);

	}
	public static String getStringDate(long timeMillis)
	{
		final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		// milliseconds to date 
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		Date date = calendar.getTime();
		return formatter.format(date);

	}

	public static String getMapsLink(double lat, double lng){
		if(lat == -1 || lng == -1)
			return null;
		return "https://www.google.com/maps/@" + lat+"," + lng + ",13z";
	}

	public static Bitmap createImage(int sessionNumber){


		sessionNumber+=3;
		ArrayList<int[]> primes=getPrimes(sessionNumber);
		int size=primes.size();
		Bitmap icon=Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
		icon.eraseColor(Color.rgb(224, 224, 224));
		Canvas canvas=new Canvas(icon);
		Paint paint=new Paint();
		paint.setStyle(Style.FILL);
		int[] colors={Color.CYAN,Color.GREEN,Color.MAGENTA,Color.YELLOW,Color.BLUE,Color.RED};

		for(int i=size-1;i>=0;i--){
			int prime=primes.get(i)[0];
			int exp=primes.get(i)[1];
		//	paint.setARGB(((prime*101)%128)+128, ((exp*153)%128)+128,(int) ((Math.pow(prime, exp)*91)%128)+128, 0);
			paint.setColor(colors[(int)(Math.pow(prime, exp))%6]);
			canvas.drawPath(getPolygon(prime*exp,new Point(100,100),100/exp), paint);

		}





		return icon;

	}


	private static ArrayList<int[]> getPrimes(int sNumber){




		ArrayList<int[]> factors=new ArrayList<int[]>();
		if(sNumber==1){
			int[] tmp={1,1};
			factors.add(tmp);
			return factors;
		}

		for(int i=1;i<=sNumber;i++){
			if(!isPrime(i)||sNumber%i!=0)continue;
			int j=1;
			int pow=i;
			while(sNumber%pow==0){
				pow*=i;
				if(sNumber%pow!=0) break;
				else j++;
			}
			int[] fact={i,j};
			factors.add(fact);
		}

		return factors;


	}


	private static boolean isPrime(int n) {

		boolean prime = true;
		if(n==1)return false;
		if(n==2)return true;
		double  sqrt=Math.sqrt(n);
		if(n%2==0)return false;
		for (long i = 3; i <= sqrt; i += 2)

			if (n % i == 0) {

				prime = false;

				break;

			}
		return prime;
	}

	private static Path getPolygon(int n, Point startPoint, float dimension){

		float startX=startPoint.x;
		float startY=startPoint.y;
		float deg=(float) (2*Math.PI/((float) n));
		ArrayList<float[]> points=new ArrayList<float[]>();
		for(int i=0; i<n;i++){
			float[] p={(float)(startX+ Math.sin(deg*i)*dimension),(float)(startY-Math.cos(deg*i)*dimension)};
			points.add(p);
		}
		Path path=new Path();
		path.moveTo(points.get(0)[0],points.get(0)[1]);
		for(int i=1;i<n;i++){
			path.lineTo(points.get(i)[0],points.get(i)[1]);
		}

		return path;
	}

	public static String longToDuration(long l)
	{
		String duration = "";
		int days = (int)(TimeUnit.MILLISECONDS.toDays(l));
		int hours = (int)(TimeUnit.MILLISECONDS.toHours(l) - TimeUnit.DAYS.toHours(days));
		int minutes = (int)(TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days));
		int seconds = (int)(TimeUnit.MILLISECONDS.toSeconds(l)- TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days));

		if(days != 0)
			duration += days + " days,";
		if(hours != 0)
			duration += hours + " hrs, ";
		duration += minutes + " min, " + seconds + " sec";
		return duration;
	}




}
