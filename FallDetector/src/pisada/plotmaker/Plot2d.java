package pisada.plotmaker;

import java.util.ArrayList;

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
 */
public class Plot2d extends View {

	private Paint paint;
	private ArrayList<Data> values, valuesInPixel;
	private float maxX,maxY,minX,minY,xAxis,yAxis;
	private int vectorLength;
	
	
	/*
	 * defData is a default value (like the first of the list) which is meant to initialize 
	 * the min values
	 */
	public Plot2d(Context context, Data defData) {
		super(context);
		paint = new Paint();
		minX = defData.getX();
		minY = defData.getY();
		maxX = 0;
		maxY = 0;
		values = new ArrayList<Data>();
		vectorLength = values.size();
		
	}
	
	
	public View getPlotView()
	{
		return this;
	}
	
	public void pushValue(Data data)
	{
		values.add(data);
		minX = Math.min(minX, data.getX());
		minY = Math.min(minY, data.getY());
		maxX = Math.max(maxX, data.getX());
		maxY = Math.max(maxY, data.getY());
		vectorLength = values.size();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float canvasHeight = getHeight();
		float canvasWidth = getWidth();
		
		valuesInPixel = toPixel(canvasWidth, canvasHeight, minX, maxX, minY, maxY, values);		
		
		int xAxisInPixels = toPixelInt(canvasHeight, minY, maxY, xAxis);
		int yAxisInPixels = toPixelInt(canvasWidth, minX, maxX, yAxis);
		
		paint.setStrokeWidth(2);
		canvas.drawARGB(255, 255, 255, 255); //white background
		
		for (int i = 0; i < vectorLength-1; i++) {
			paint.setColor(Color.DKGRAY);
			canvas.drawLine(valuesInPixel.get(i).getX(),canvasHeight-valuesInPixel.get(i).getY(),valuesInPixel.get(i+1).getX(),canvasHeight-valuesInPixel.get(i+1).getY(),paint);
		}
		
		paint.setColor(Color.BLACK);
		canvas.drawLine(0,canvasHeight-xAxisInPixels,canvasWidth,canvasHeight-xAxisInPixels,paint);
		canvas.drawLine(yAxisInPixels,0,yAxisInPixels,canvasHeight,paint);
		
		//axes labeling

		float temp = 0.0f;
		int n=10; //number of values per-axis
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize(20.0f);
		for (int i=1;i<=n;i++){
			temp = Math.round(10*(minX+(i-1)*(maxX-minX)/n))/10;
			canvas.drawText(""+temp, (float)toPixelInt(canvasWidth, minX, maxX, temp),canvasHeight-xAxisInPixels+20, paint);
			temp = Math.round(10*(minY+(i-1)*(maxY-minY)/n))/10;
			canvas.drawText(""+temp, yAxisInPixels+20,canvasHeight-(float)toPixelInt(canvasHeight, minY, maxY, temp), paint);
		}
		canvas.drawText(""+maxX, (float)toPixelInt(canvasWidth, minX, maxX, maxX),canvasHeight-xAxisInPixels+20, paint);
		canvas.drawText(""+maxY, yAxisInPixels+20,canvasHeight-(float)toPixelInt(canvasHeight, minY, maxY, maxY), paint);
				
	}
	
	private ArrayList<Data> toPixel(float pixelsX, float pixelsY, float minX, float maxX, float minY, float maxY, ArrayList<Data>val) {
		
		/*
		 * where should the values be in pixels, given max and min values on the screen
		 */
		
		ArrayList<Data> output = new ArrayList<Data>();
		
		for (int i = 0; i < val.size(); i++) {
			int x = (int)(0.1*pixelsX+((val.get(i).getX()-minX)/(maxX-minX))*0.8*pixelsX);
			int y = (int)(0.1*pixelsY+((val.get(i).getY()-minY)/(maxY-minY))*0.8*pixelsY);
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

	

}
