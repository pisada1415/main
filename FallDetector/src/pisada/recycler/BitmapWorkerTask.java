package pisada.recycler;

import java.lang.ref.WeakReference;

import pisada.fallDetector.R;
import pisada.fallDetector.Utility;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

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
