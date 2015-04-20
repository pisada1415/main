package pisada.fallDetector;

import pisada.database.Acquisition;

public class DetectorAlgorithm {

	
	public static boolean danielAlgorithm(ExpiringList list)
	{
		int i = 0;
		for(Acquisition a : list.timerAcquisitionList)
		{
			i++;
		}
		System.out.println(" contati " + i + " elementi");
		/*
		 * occhiata qui:
		 * http://www.vogella.com/code/de.vogella.android.sensor/src/de/vogella/android/sensor/SensorTestActivity.html
		 */
		return true; //sempre caduta confermata
	}
}
