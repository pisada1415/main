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
	private float maxx,maxy,minx,miny,locxAxis,locyAxis;
	private int vectorLength;
	private double min;
	private double max;
	
	
	public Plot2d(Context context, /*float[] xvalues, float[] yvalues,*/  double defValue) {
		super(context);
	/*	this.xvalues=xvalues;
		this.yvalues=yvalues;*/
	//	vectorLength = xvalues.length;
		paint = new Paint();
		min = defValue;
		max = defValue;
		values = new ArrayList<Data>();
		vectorLength = values.size();
	//	getAxes(xvalues, yvalues);
		
	}
	
	
	public View getPlotView()
	{
		return this;
	}
	
	public void pushValue(Data data)
	{
		values.add(data);
		minx = Math.min(minx, data.getX());
		miny = Math.min(miny, data.getY());
		maxx = Math.max(maxx, data.getX());
		maxy = Math.max(maxy, data.getY());
		vectorLength = values.size();
		//this.draw(canvas); //hhhhhhhhhhhhhhhmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmhhhhhhhhhhhhh
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//this.canvas = canvas;
		float canvasHeight = getHeight();
		float canvasWidth = getWidth();
		valuesInPixel = toPixel(canvasWidth, canvasHeight, minx, maxx, miny, maxy, values);

		
		int locxAxisInPixels = toPixelInt(canvasHeight, miny, maxy, locxAxis);
		int locyAxisInPixels = toPixelInt(canvasWidth, minx, maxx, locyAxis);
		String xAxis = "x-axis";
		String yAxis = "y-axis";

		paint.setStrokeWidth(2);
		canvas.drawARGB(255, 255, 255, 255);
		for (int i = 0; i < vectorLength-1; i++) {
			paint.setColor(Color.RED);
			canvas.drawLine(valuesInPixel.get(i).getX(),canvasHeight-valuesInPixel.get(i).getY(),valuesInPixel.get(i+1).getX(),canvasHeight-valuesInPixel.get(i+1).getY(),paint);
		}
		
		paint.setColor(Color.BLACK);
		canvas.drawLine(0,canvasHeight-locxAxisInPixels,canvasWidth,canvasHeight-locxAxisInPixels,paint);
		canvas.drawLine(locyAxisInPixels,0,locyAxisInPixels,canvasHeight,paint);
		
		//Automatic axes markings, modify n to control the number of axes labels

		float temp = 0.0f;
		int n=3;
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize(20.0f);
		for (int i=1;i<=n;i++){
			temp = Math.round(10*(minx+(i-1)*(maxx-minx)/n))/10;
			canvas.drawText(""+temp, (float)toPixelInt(canvasWidth, minx, maxx, temp),canvasHeight-locxAxisInPixels+20, paint);
			temp = Math.round(10*(miny+(i-1)*(maxy-miny)/n))/10;
			canvas.drawText(""+temp, locyAxisInPixels+20,canvasHeight-(float)toPixelInt(canvasHeight, miny, maxy, temp), paint);
		}
		canvas.drawText(""+maxx, (float)toPixelInt(canvasWidth, minx, maxx, maxx),canvasHeight-locxAxisInPixels+20, paint);
		canvas.drawText(""+maxy, locyAxisInPixels+20,canvasHeight-(float)toPixelInt(canvasHeight, miny, maxy, maxy), paint);
		//canvas.drawText(xAxis, canvasWidth/2,canvasHeight-locxAxisInPixels+45, paint);
		//canvas.drawText(yAxis, locyAxisInPixels-40,canvasHeight/2, paint);

		
		
	}
	
	private ArrayList<Data> toPixel(float pixelsX, float pixelsY, float minx, float maxx, float miny, float maxy, ArrayList<Data>val) {
		
		
		ArrayList<Data> output = new ArrayList<Data>();
		
		for (int i = 0; i < val.size(); i++) {
			int x = (int)(0.1*pixelsX+((val.get(i).getX()-minx)/(maxx-minx))*.8*pixelsX);
			int y = (int)(0.1*pixelsY+((val.get(i).getY()-miny)/(maxy-miny))*.8*pixelsY);
			Data a = new Data(x, y);
			output.add(a);
		}
		
		return (output);
	}
	
	private void getAxes(float[] xvalues, float[] yvalues) {
		
		minx=getMin(xvalues);
		miny=getMin(yvalues);
		maxx=getMax(xvalues);
		maxy=getMax(yvalues);
		
		if (minx>=0)
			locyAxis=minx;
		else if (minx<0 && maxx>=0)
			locyAxis=0;
		else
			locyAxis=maxx;
		
		if (miny>=0)
			locxAxis=miny;
		else if (miny<0 && maxy>=0)
			locxAxis=0;
		else
			locxAxis=maxy;
		
	}
	
	private int toPixelInt(float pixels, float min, float max, float value) {
		
		double p;
		int pint;
		p = .1*pixels+((value-min)/(max-min))*.8*pixels;
		pint = (int)p;
		return (pint);
	}

	private float getMax(float[] v) {
		float largest = v[0];
		for (int i = 0; i < v.length; i++)
			if (v[i] > largest)
				largest = v[i];
		return largest;
	}

	private float getMin(float[] v) {
		float smallest = v[0];
		for (int i = 0; i < v.length; i++)
			if (v[i] < smallest)
				smallest = v[i];
		return smallest;
	}

}
