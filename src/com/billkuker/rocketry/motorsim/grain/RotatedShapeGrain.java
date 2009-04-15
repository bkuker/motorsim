package com.billkuker.rocketry.motorsim.grain;

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

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;

public class RotatedShapeGrain implements Grain {
	
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
	
	BurningShape shape = new BurningShape();
	
	Amount<Length> web = null;
	
	{
		Shape outside = new Rectangle2D.Double(0,0,15,100);
		shape.add( outside );
		shape.inhibit( outside );
		shape.subtract( new Rectangle2D.Double(0,0,5,100));
		shape.subtract(new Rectangle2D.Double(0, -10, 15, 10));
		shape.subtract(new Rectangle2D.Double(0, 100, 15, 10));
	}

	@Override
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

	@Override
	public Area getSideView(Amount<Length> regression) {
		Area a = new Area();
		Area reg = shape.getShape(regression);
		a.add(reg);
		a.transform(AffineTransform.getScaleInstance(-1, 1));
		a.add(reg);
		return a;
	}

	@Override
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
		
		System.out.println(regression);
	
		double sqmm = yRotatedSurfaceArea(burn);

		
		return Amount.valueOf(sqmm, SI.MILLIMETER.pow(2).asType(javax.measure.quantity.Area.class)).divide(2);

	}

	@Override
	public Amount<Volume> volume(Amount<Length> regression) {
		Area squared = new Area(square(shape.getShape(regression)));
		Amount<javax.measure.quantity.Area> sum = Amount.valueOf(0, SI.SQUARE_METRE);
		for( Area a: ShapeUtil.separate(squared) ){
			sum = sum.plus( ShapeUtil.area(a) );
		}
		Amount<Volume> v = sum.times(Amount.valueOf(Math.PI, SI.MILLIMETER)).to(Volume.UNIT);
		return v;
	}

	@Override
	//TODO find actual thickness
	public Amount<Length> webThickness() {
		if ( web != null )
			return web;
		Rectangle2D b = shape.getShape(Amount.valueOf(0, SI.MILLIMETER)).getBounds2D();
		double webmm = b.getWidth()>b.getHeight()?b.getWidth():b.getHeight();
		return web = Amount.valueOf(webmm, SI.MILLIMETER);
	}
	

	public static void main(String args[]) throws Exception {
		RotatedShapeGrain e = new RotatedShapeGrain();
		new Editor(e).show();
		new GrainPanel(e).show();
	}
	
	private Shape square(java.awt.geom.Area a) {
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
	
	private double yRotatedSurfaceArea(java.awt.geom.Area a) {
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
				
				System.out.println(add);
				
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


}
