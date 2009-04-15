package com.billkuker.rocketry.motorsim.grain;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class BurningShape {
	Set<Shape> plus = new HashSet<Shape>();
	Set<Shape> minus = new HashSet<Shape>();
	Set<Shape> inhibited = new HashSet<Shape>();
	
	public void add(Shape s){
		plus.add(s);
	}
	
	public void subtract(Shape s){
		minus.add(s);
	}
	
	public void inhibit(Shape s){
		inhibited.add(s);
	}

	public java.awt.geom.Area getShape(Amount<Length> regression) {
		java.awt.geom.Area res = getPlus(regression);
		res.subtract(getMinus(regression));
		return res;
	}
	
	
	private java.awt.geom.Area getPlus(Amount<Length> regression) {
		java.awt.geom.Area a = new java.awt.geom.Area();
		for (Shape s : plus)
			a.add(new java.awt.geom.Area(regress(s, regression
					.doubleValue(SI.MILLIMETER), true)));
		return a;
	}
	
	private java.awt.geom.Area getMinus(Amount<Length> regression) {
		java.awt.geom.Area a = new java.awt.geom.Area();
		for (Shape s : minus)
			a.add(new java.awt.geom.Area(regress(s, regression
					.doubleValue(SI.MILLIMETER), false)));
		return a;
	}
	
	private Shape regress(Shape s, double mm, boolean plus) {
		if (inhibited.contains(s))
			return s;
		if (s instanceof Ellipse2D) {
			Ellipse2D e = (Ellipse2D) s;

			double d = plus ? -2 * mm : 2 * mm;

			double w = e.getWidth() + d;
			double h = e.getHeight() + d;
			double x = e.getX() - d / 2;
			double y = e.getY() - d / 2;

			return new Ellipse2D.Double(x, y, w, h);
		} else if (s instanceof Rectangle2D) {
			Rectangle2D r = (Rectangle2D) s;
			
			if ( plus ){
				double d = -2 * mm;
				double w = r.getWidth() + d;
				double h = r.getHeight() + d;
				double x = r.getX() - d / 2;
				double y = r.getY() - d / 2;
				return new Rectangle2D.Double(x, y, w, h);
			} else {
				//A rectangular hole gets rounded corners as it grows
				java.awt.geom.Area a = new java.awt.geom.Area();
				double d = 2 * mm;
				
				//Make it wider
				double w = r.getWidth() + d;
				double h = r.getHeight();
				double x = r.getX() - d / 2;
				double y = r.getY();
				a.add( new java.awt.geom.Area(new Rectangle2D.Double(x, y, w, h)));
				
				//Make it taller
				w = r.getWidth();
				h = r.getHeight() + d;
				x = r.getX();
				y = r.getY() - d / 2;
				a.add( new java.awt.geom.Area(new Rectangle2D.Double(x, y, w, h)));
				
				//Add rounded corners
				a.add( new java.awt.geom.Area(new Ellipse2D.Double(r.getX()-mm, r.getY()-mm, mm*2, mm*2)));
				a.add( new java.awt.geom.Area(new Ellipse2D.Double(r.getX()+r.getWidth()-mm, r.getY()-mm, mm*2, mm*2)));
				a.add( new java.awt.geom.Area(new Ellipse2D.Double(r.getX()+r.getWidth()-mm, r.getY()+r.getHeight()-mm, mm*2, mm*2)));
				a.add( new java.awt.geom.Area(new Ellipse2D.Double(r.getX()-mm, r.getY()+r.getHeight()-mm, mm*2, mm*2)));
				
				return a;
			}

		}
		return null;
	}

	
}
