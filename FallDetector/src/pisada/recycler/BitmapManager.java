package pisada.recycler;

import pisada.fallDetector.R;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
/*
 * funziona cos�: chiami il metodo loadBitmap passando come primo parametro il numero della session, seconda l'imageview
 * e terzo il context. un bitmap (placeholder) viene caricato immediatamente su ogni item, poi quando � pronto quello dato dal task viene caricato quello 
 * quindi adesso si possono fare quanti calcoli si vuole per generare le immagini, non rallenter� il thread UI.
 * 
 * quello che succede �:
 * l'imageview si salva un riferimento all'asynctask pi� recente associato a essa, che pu� poi essere utilizzato quando il task
 * ha finito il suo lavoro. 
 */
public class BitmapManager {
	public static void loadBitmap(int resId, ImageView imageView, Context act) {
	    if (cancelPotentialWork(resId, imageView)) {
	        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	        /*
	         * prima di iniziare il "lavoro" bindi un asyncdrawable alla imageview in modo che quando ha finito
	         * si aggiorni da solo
	         */
	        final AsyncDrawable asyncDrawable = new AsyncDrawable(act.getResources(), BitmapFactory.decodeResource(act.getResources(), R.drawable.placeholder), task);
	        imageView.setImageDrawable(asyncDrawable);
	        task.execute(resId);
	    }
	}
	
	private static boolean cancelPotentialWork(int data, ImageView imageView) {
		/*
		 * questo cancella il lavoro di un eventuale altro task gi� associato con la stessa imageview
		 */
		final BitmapWorkerTask bitmapWorkerTask = BitmapWorkerTask.getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final int bitmapData = bitmapWorkerTask.getData();
			// se bitmapdata non � ancora stato settato o � diverso dal nuovo data (data � il numero session)
			if (bitmapData == 0 || bitmapData != data) {
				// cancella il task precedente
				bitmapWorkerTask.cancel(true);
			} else {
				// c'� lo stesso tipo di lavoro in azione: non serve farne partire un altro
				return false;
			}
		}
		// niente � stato cancellato
		return true;
	}
}
