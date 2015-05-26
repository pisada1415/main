package pisada.fallDetector;

import pisada.database.Acquisition;

public class DetectorAlgorithm {


	public static boolean danielAlgorithm(ExpiringList list)
	{

	
		 
		boolean fall=false;
		int i=0;
		int j=0;
		for(Acquisition a : list.timerAcquisitionList){

			if(module(a)>7 && i>2){
				fall=true;
				break;
			}
			if(module(a)<2.5){
				i++;
				if(j>0) j--;
			}
			else{
				j++;
				if(i>0)i--;
			}

			if(j>150){
				j=0;i=0;
			}


		}

		return fall;

	}



	private static double module(Acquisition a){

		return	Math.sqrt(a.getXaxis()*a.getXaxis()+a.getYaxis()*a.getYaxis()+a.getZaxis()*a.getZaxis());

	}
}