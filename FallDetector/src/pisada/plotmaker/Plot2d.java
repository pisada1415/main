package pisada.plotmaker;

import java.text.DecimalFormat;
import java.util.ArrayList;

import pisada.fallDetector.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;


/*
 * usage:
 * create a new Plot2d using the constructor
 * add the values using the pushValue method
 * get the view calling getplpotview
 * quindi value iniziale di yaxis in realtà deve coincidere con secondi iniziali 
 * quindi in teoria si risolve partendo da 0?
 */
public class Plot2d extends View {

	private Paint paint;
	private ArrayList<Data> values, valuesInPixel;
	private float maxX,maxY,minX,minY,xAxis;//, yAxis;
	
	private final int MAX_VALUES_STORED = 100;
	private DecimalFormat formatter;
	private Context context;
	/*
	 * defData is a default value (like the first of the list) which is meant to initialize 
	 * the min values
	 */
	
	//arriva un tempo e il value è 0. cazzo sarebbe yaxis... 
	
	public Plot2d(Context context, Data defData) {
		super(context);
		this.context = context;
		paint = new Paint();
		minX = defData.x;
		minY = defData.y;
		maxX = 0;
		maxY = 0;
		values = new ArrayList<Data>();
		formatter = new DecimalFormat("#.00");
	}
	
	
	public View getPlotView()
	{
		return this;
	}
	
	public void pushValue(Data data)
	{
		while(values.size() >= MAX_VALUES_STORED)
			values.remove(0);
		values.add(data);
		
		//minx dovrebe essere il valore minimo del tempo quindi il valore di tempo del primo della lista
		minX = values.get(0).x;
		//minX = Math.min(minX, data.getX());
		minY = Math.min(minY, data.y);
		//maxX = Math.max(maxX, data.getX());
		maxX = values.get(values.size()-1).x;	
		maxY = Math.max(maxY, data.y);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float canvasHeight = getHeight();
		float canvasWidth = getWidth();
		
		valuesInPixel = toPixel(canvasWidth, canvasHeight, minX, maxX, minY, maxY, values);		
		
		int xAxisInPixels = toPixelInt(canvasHeight, minY, maxY, xAxis);
	//	int yAxisInPixels = toPixelInt(canvasWidth, minX, maxX, yAxis);
		int yAxisInPixels = 10; //valore fissato sullo schermo indipendente dai valori di x
		paint.setStrokeWidth(2);
		canvas.drawRGB(39, 39, 39); //white background
		
		for (int i = 0; i < valuesInPixel.size()-1; i++) {
			paint.setColor(getResources().getColor(R.color.lightBlue));
			canvas.drawLine(valuesInPixel.get(i).x,canvasHeight-valuesInPixel.get(i).y,valuesInPixel.get(i+1).x,canvasHeight-valuesInPixel.get(i+1).y,paint);
		}
		
		paint.setColor(Color.WHITE);
		canvas.drawLine(0,canvasHeight-xAxisInPixels,canvasWidth,canvasHeight-xAxisInPixels,paint);
		canvas.drawLine(yAxisInPixels,0,yAxisInPixels,canvasHeight,paint);
		
		//axes labeling

		float temp = 0.0f;
		int n=5; //number of values per-axis
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize(20.0f);
		for (int i=1;i<=n;i++){
			temp = Math.round(10*(minX+(i-1)*(maxX-minX)/n))/10; //valore in millisecondi /1000 = valore in secondi
			canvas.drawText(""+formatter.format(temp/1000), (float)toPixelInt(canvasWidth, minX, maxX, temp),canvasHeight-xAxisInPixels+20, paint);
			temp = Math.round(10*(minY+(i-1)*(maxY-minY)/n))/10;
			canvas.drawText(""+temp, yAxisInPixels+20,canvasHeight-(float)toPixelInt(canvasHeight, minY, maxY, temp), paint);
		}
		
		String output = formatter.format(maxX/1000);
		canvas.drawText(output, (float)toPixelInt(canvasWidth, minX, maxX, maxX),canvasHeight-xAxisInPixels+20, paint);
		output = formatter.format(maxY);
		canvas.drawText(output, yAxisInPixels+20,canvasHeight-(float)toPixelInt(canvasHeight, minY, maxY, maxY), paint);
				
	}
	
	private ArrayList<Data> toPixel(float pixelsX, float pixelsY, float minX, float maxX, float minY, float maxY, ArrayList<Data>val) {
		
		/*
		 * where should the values be in pixels, given max and min values on the screen
		 */
		
		ArrayList<Data> output = new ArrayList<Data>();
		
		for (int i = 0; i < val.size(); i++) {
			int x = (int)(0.1*pixelsX+((val.get(i).x-minX)/(maxX-minX))*0.8*pixelsX);
			int y = (int)(0.1*pixelsY+((val.get(i).y-minY)/(maxY-minY))*0.8*pixelsY);
			Data a = new Data(x, y);
			output.add(a);
			
		}
		
		return (output);
	}
	
	
	private int toPixelInt(float pixels, float min, float max, float value) {
		/*
		 * where should the value be in pixels, given max and min values on the screen
		 */
		return (int)(0.1*pixels+((value-min)/(max-min))*0.8*pixels);
	}
	
	public void clear()
	{
		values = new ArrayList<Data>();
		valuesInPixel = new ArrayList<Data>();
		this.invalidate();
	}

	

}
