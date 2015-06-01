package pisada.fallDetector;

import pisada.database.Acquisition;

public class DetectorAlgorithm {


	public static boolean danielAlgorithm(ExpiringList list)
	{



		boolean fall=false;
		int i=0;
		int j=0;
		for(Acquisition a : list.timerAcquisitionList){

			if(i>25){
				fall=true;
				break;
			}
			if(module(a)<8){
				i++;
			}
			else{
				j++;
			}

			if(j>5){
				j=0;i=0;
			}


		}

		return fall;

	}



	private static double module(Acquisition a){

		return	Math.sqrt(a.getXaxis()*a.getXaxis()+a.getYaxis()*a.getYaxis()+a.getZaxis()*a.getZaxis());

	}
}