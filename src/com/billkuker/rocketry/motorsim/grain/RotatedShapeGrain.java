package com.billkuker.rocketry.motorsim.grain;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;

public class RotatedShapeGrain implements Grain {
	
	private static Logger log = Logger.getLogger(RotatedShapeGrain.class);
	
	public static RotatedShapeGrain DEFAULT_GRAIN = new RotatedShapeGrain(){
		{
			try{
				Shape outside = new Rectangle2D.Double(0,0,15,70);
				shape.add( outside );
				shape.inhibit( outside );
				shape.subtract( new Rectangle2D.Double(0,0,5,70));
				shape.subtract(new Rectangle2D.Double(0, -10, 15, 10));
				shape.subtract(new Rectangle2D.Double(0, 70, 15, 10));
			} catch ( Exception e ){
				throw new Error(e);
			}
		}
	};
	

	
	public enum Quality {
		High()
			{{
				 surfaceAreaStep = .001;
				 squareFlatteningError = 0.001;
				 squareSubdivide = .01;
				 areaFlatteningError = .001;
			}},
		Low()			{{
			 surfaceAreaStep = .001;
			 squareFlatteningError = .1;
			 squareSubdivide = .1;
			 areaFlatteningError = .1;
		}};
		
		double surfaceAreaStep = .001;
		double squareFlatteningError = 0.001;
		double squareSubdivide = .01;
		double areaFlatteningError = .001;
	}
	
	Quality quality = Quality.Low;
	
	protected BurningShape shape = new BurningShape();
	
	Amount<Length> web = null;
	
	public Area getCrossSection(Amount<Length> regression) {
		Area ret = new Area();
		for( Area a : ShapeUtil.separate(shape.getShape(regression))){
			Rectangle2D b = a.getBounds2D();
			Ellipse2D inner = new Ellipse2D.Double(-b.getMinX(), -b.getMinX(), b.getMinX()*2, b.getMinX()*2);
			Ellipse2D outer = new Ellipse2D.Double(-b.getMaxX(), -b.getMaxX(), b.getMaxX()*2, b.getMaxX()*2);
			Area aa = new Area(outer);
			aa.subtract(new Area(inner));
			ret.add(aa);
		}
		return ret;
	}

	public Area getSideView(Amount<Length> regression) {
		Area a = new Area();
		Area reg = shape.getShape(regression);
		a.add(reg);
		a.transform(AffineTransform.getScaleInstance(-1, 1));
		a.add(reg);
		return a;
	}

	public Amount<javax.measure.quantity.Area> surfaceArea(
			Amount<Length> regression) {
		Amount<javax.measure.quantity.Area> zero = Amount.valueOf(0, javax.measure.quantity.Area.UNIT);
		
		if (regression.isGreaterThan(webThickness()))
			return zero;

		java.awt.geom.Area burn = shape.getShape(regression);
		
		if (burn.isEmpty())
			return zero;
		
		burn.subtract(shape.getShape(regression.plus(Amount.valueOf(quality.surfaceAreaStep,
				SI.MILLIMETER))));
	
		double sqmm = yRotatedSurfaceArea(burn);

		
		return Amount.valueOf(sqmm, SI.MILLIMETER.pow(2).asType(javax.measure.quantity.Area.class)).divide(2);

	}

	public Amount<Volume> volume(Amount<Length> regression) {
		Shape squared = square(shape.getShape(regression));
		Amount<javax.measure.quantity.Area> sum = Amount.valueOf(0, SI.SQUARE_METRE);
		//for( Area a: ShapeUtil.separate(squared) ){
		//	sum = sum.plus( ShapeUtil.area(a) );
		//}
		sum = ShapeUtil.area(squared);
		Amount<Volume> v = sum.times(Amount.valueOf(Math.PI, SI.MILLIMETER)).to(Volume.UNIT);
		return v;
	}

	public Amount<Length> webThickness() {
		if (web != null)
			return web;

		java.awt.geom.Area a = shape.getShape(Amount.valueOf(0, SI.MILLIMETER));
		Rectangle r = a.getBounds();
		double max = r.getWidth() < r.getHeight() ? r.getHeight() : r
				.getWidth(); // The max size
		double min = 0;
		double guess;
		while (true) {
			guess = min + (max - min) / 2; // Guess halfway through
			log.debug("Min: " + min + " Guess: " + guess + " Max: " + max);
			a = shape.getShape(Amount.valueOf(guess, SI.MILLIMETER));
			if (a.isEmpty()) {
				// guess is too big
				max = guess;
			} else {
				// min is too big
				min = guess;
			}
			if ((max - min) < .01)
				break;
		}
		web = Amount.valueOf(guess, SI.MILLIMETER);

		return web;

	}

	private Shape square(Shape a) {
		PathIterator i = a.getPathIterator(new AffineTransform(), quality.squareFlatteningError);
		GeneralPath cur = new GeneralPath();

		double last[] = {0,0};
		while (!i.isDone()) {
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_CLOSE:
				cur.closePath();
				break;
			case PathIterator.SEG_MOVETO:
				cur.moveTo(Math.pow(coords[0],2), coords[1]);
				last[0] = coords[0];
				last[1] = coords[1];
				break;
			case PathIterator.SEG_CUBICTO:
				throw new Error("Non-flattened geometry!");
			case PathIterator.SEG_LINETO:
				double x = last[0];
				double y = last[1];
				double len = Math.sqrt(Math.pow(last[0]-coords[0], 2) + Math.pow(last[1]-coords[1], 2));
				int steps = (int)(len / quality.squareSubdivide) + 5;
				for (int s = 0; s < steps; s++) {
					x += (coords[0] - last[0]) / steps;
					y += (coords[1] - last[1]) / steps;
					cur.lineTo(Math.pow(x, 2), y);
				}
				last[0] = coords[0];
				last[1] = coords[1];
				break;
			case PathIterator.SEG_QUADTO:
				throw new Error("Non-flattened geometry!");

			}
			i.next();
		}
		return cur;
	}
	
	private double yRotatedSurfaceArea(Shape a) {
		PathIterator i = a.getPathIterator(new AffineTransform(), quality.areaFlatteningError);
		double x = 0, y = 0;
		double mx = 0, my = 0;
		double len = 0;
		while (!i.isDone()) {
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_CLOSE) {


				double nx = coords[0];
				double ny = coords[1];
				
				if ( type == PathIterator.SEG_CLOSE ){
					nx = mx;
					ny = my;
				}
				
				double dy = Math.abs(y-ny);
				double dx = Math.abs(x-nx);
				double xl = x>nx?x:nx;
				double xs = x<nx?x:nx;	
				
				double add = 0;
				if ( dx == 0 ){
					//Cylender
					add = 2 * Math.PI * xl * dy;
				} else if ( dy == 0 ){
					//disk
					 add = Math.PI * xl * xl - Math.PI * xs * xs;
				}else{
					double h = xl/dx * dy;
					double s1 = Math.sqrt(xl*xl + h*h);
					double s2 = Math.sqrt(xs*xs + (h-dy)*(h-dy));
					add = Math.PI * (xl*s1 - xs*s2);
				}
				
				len += add;

				x = nx;
				y = ny;
			} else if (type == PathIterator.SEG_MOVETO) {
				mx = x = coords[0];
				my = y = coords[1];
			} else {
				throw new Error("Non-flattened geometry!");
			}
			i.next();
		}
		return len;
	}

	

	public static void main(String args[]) throws Exception {
		RotatedShapeGrain e = DEFAULT_GRAIN;
		new Editor(e).showAsWindow();
		new GrainPanel(e).showAsWindow();
	}
	
}
