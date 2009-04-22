package com.billkuker.rocketry.motorsim.grain;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
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

	/*
	 * 9 times out of 10 we get asked for the same thing
	 * 2x in a row, for volume and for area
	 */
	private Amount<Length> lastRegression = null;
	private Area lastArea = null;
	
	public java.awt.geom.Area getShape(Amount<Length> regression) {
		if ( regression.equals(lastRegression) ){
			return lastArea;
		}
		lastRegression = regression;

		java.awt.geom.Area a = new java.awt.geom.Area();
		for (Shape s : plus)
			a.add(new java.awt.geom.Area(regress(s, regression
					.doubleValue(SI.MILLIMETER), true)));
		for (Shape s : minus)
			a.subtract(new java.awt.geom.Area(regress(s, regression
					.doubleValue(SI.MILLIMETER), false)));
		
		return lastArea = a;
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
				
				a.add(new Area(new RoundRectangle2D.Double(
						r.getX() - d / 2,
						r.getY() - d / 2,
						r.getWidth() + d,
						r.getHeight() + d,
						d,
						d
						)));

				return a;
			}

		}
		return null;
	}

	
}
