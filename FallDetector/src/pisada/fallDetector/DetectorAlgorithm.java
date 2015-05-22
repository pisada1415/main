package pisada.fallDetector;

import pisada.database.Acquisition;

public class DetectorAlgorithm {


	public static boolean danielAlgorithm(ExpiringList list)
	{

		/*int i = 0;

		for(Acquisition a : list.timerAcquisitionList)
		{
			i++;
			System.out.println(Math.sqrt(a.getXaxis()*a.getXaxis()+a.getYaxis()*a.getYaxis()+a.getZaxis()*a.getZaxis()));
	}


		 */
		boolean fall=false;
		int i=0;
		int j=0;
		for(Acquisition a : list.timerAcquisitionList){

			if(module(a)>2.5 && i>10){
				fall=true;
				break;
			}
			if(module(a)<2){
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