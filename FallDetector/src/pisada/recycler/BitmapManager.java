package pisada.recycler;

import java.lang.ref.WeakReference;

import pisada.fallDetector.R;
import pisada.fallDetector.Utility;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
/*
 *ULTIMA MODIFICA: teoricamente funziona anche da cache automaticamente: NNNOONN TESTATO!!!!!!!!!!!!! 
 *probabilità che vada: 0.1% (stima basata sull'esperienza). non serve fare altro che chiamare il solito metodo per aggiungere il bitmap.
 * 
 * funziona così: chiami il metodo loadBitmap passando come primo parametro il numero della session, seconda l'imageview
 * e terzo il context. un bitmap (placeholder) viene caricato immediatamente su ogni item, poi quando è pronto quello dato dal task viene caricato quello 
 * quindi adesso si possono fare quanti calcoli si vuole per generare le immagini, non rallenterà il thread UI.
 * 
 * 
 * 
 * quello che succede è:
 * l'imageview si salva un riferimento all'asynctask più recente associato a essa, che può poi essere utilizzato quando il task
 * ha finito il suo lavoro. 
 */
public class BitmapManager {


	
	protected static LruCache<Integer,Bitmap> mMemoryCache;



	public static void loadBitmap(int resId, ImageView imageView, Context act) {
		if(mMemoryCache == null){
			// questo è il valore massimo della memoria disponibile per l'app.
			//salvato in  kilobytes. la lrucache prende un int nel costruttore
			final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			// usiamo 1/8 della memoria disponibile. (è importante che non sia troppo piccola, poi vediamo)
			final int cacheSize = maxMemory / 8; //circa 4MB su un hdpi
			
			mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(Integer key, Bitmap bitmap) {
					// misuriamo la cache in kilobytes e non in numero items
					return bitmap.getByteCount() / 1024;
				}
			};
		}
		Bitmap cachedBitmap = (Bitmap)mMemoryCache.get(resId);
		if(cachedBitmap!= null)
			imageView.setImageBitmap(cachedBitmap);
		else
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
		 * questo cancella il lavoro di un eventuale altro task già associato con la stessa imageview
		 */
		final BitmapWorkerTask bitmapWorkerTask = BitmapWorkerTask.getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final int bitmapData = bitmapWorkerTask.getData();
			// se bitmapdata non è ancora stato settato o è diverso dal nuovo data (data è il numero session)
			if (bitmapData == 0 || bitmapData != data) {
				// cancella il task precedente
				bitmapWorkerTask.cancel(true);
			} else {
				// c'è lo stesso tipo di lavoro in azione: non serve farne partire un altro
				return false;
			}
		}
		// niente è stato cancellato
		return true;
	}

	

}

class AsyncDrawable extends BitmapDrawable {
	private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
		super(res, bitmap);

		bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	}

	public BitmapWorkerTask getBitmapWorkerTask() {
		return bitmapWorkerTaskReference.get(); //restituisce l'oggetto reference alla reference
	}
}


class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

	private final WeakReference<ImageView> imageViewReference;
	private int data = 0;


	public BitmapWorkerTask(ImageView imageView) {
		// la weakreference ci assicura che l'imageview possa essere garbagecollectata
		imageViewReference = new WeakReference<ImageView>(imageView);

	}

	// decodifica immagine in background
	@Override
	protected Bitmap doInBackground(Integer... params) {
		data = params[0];
		return decodeSampledBitmapFromResource(data, 0, 0);
	}

	public int getData(){
		return this.data;
	}

	private Bitmap decodeSampledBitmapFromResource(int data, int w, int h) {
		// TODO Auto-generated method stub
		Bitmap bitmap = Utility.createImage(data);
		if ( w != 0 && h != 0)
		{
			bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
			BitmapManager.mMemoryCache.put(data, bitmap);
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (isCancelled()) {
			bitmap = null;
		}

		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
			if (this == bitmapWorkerTask && imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

}


