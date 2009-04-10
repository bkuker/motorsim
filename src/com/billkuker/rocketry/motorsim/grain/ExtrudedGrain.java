package com.billkuker.rocketry.motorsim.grain;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.visual.Chart;

public class ExtrudedGrain implements Grain, Grain.Graphical {

	Set<Shape> plus = new HashSet<Shape>();

	Set<Shape> minus = new HashSet<Shape>();

	Set<Shape> inhibited = new HashSet<Shape>();

	Amount<Length> length = Amount.valueOf(25, SI.MILLIMETER);

	Amount<Length> rStep;

	private class RegEntry {
		Amount<Area> surfaceArea;

		Amount<Volume> volume;
	}

	SortedMap<Amount<Length>, RegEntry> data = new TreeMap<Amount<Length>, RegEntry>();

	Amount<Length> webThickness;

	{
		/*
		 * Similar test grain Shape outside = new Ellipse2D.Double(50,50,30,30);
		 * plus.add(outside); minus.add(new Ellipse2D.Double(50,60,10,10));
		 * inhibited.add(outside); length = Amount.valueOf(70, SI.MILLIMETER); /
		 */

		/* Big c-slot */
		Shape outside = new Ellipse2D.Double(0, 0, 30, 30);
		plus.add(outside);
		inhibited.add(outside);
		minus.add(new Rectangle2D.Double(12, 25, 5, 10));
		minus.add(new Ellipse2D.Double(10, 4, 5, 5));
		length = Amount.valueOf(70, SI.MILLIMETER);
		/**/

		/*
		 * Plus sign Shape outside = new Ellipse2D.Double(0,0,200,200);
		 * plus.add(outside); inhibited.add(outside); minus.add(new
		 * Rectangle2D.Double(90,40,20,120)); minus.add(new
		 * Rectangle2D.Double(40,90,120,20));
		 */

		findWebThickness();

		fillInData();
	}

	@Override
	public Amount<Area> surfaceArea(Amount<Length> regression) {
		if (regression.isGreaterThan(webThickness))
			return Amount.valueOf(0, Area.UNIT);
		Amount<Length> highKey = data.tailMap(regression).firstKey();
		Amount<Length> lowKey;
		try {
			lowKey = data.headMap(regression).lastKey();
		} catch (NoSuchElementException e) {
			return data.get(highKey).surfaceArea;
		}

		double lp = regression.minus(lowKey).divide(highKey.minus(lowKey)).to(
				Dimensionless.UNIT).doubleValue(Dimensionless.UNIT);

		Amount<Area> lowVal = data.get(lowKey).surfaceArea;
		Amount<Area> highVal = data.get(highKey).surfaceArea;

		return lowVal.times(1 - lp).plus(highVal.times(lp));
	}

	@Override
	public Amount<Volume> volume(Amount<Length> regression) {
		if (regression.isGreaterThan(webThickness))
			return Amount.valueOf(0, Volume.UNIT);
		Amount<Length> highKey = data.tailMap(regression).firstKey();
		Amount<Length> lowKey;
		try {
			lowKey = data.headMap(regression).lastKey();
		} catch (NoSuchElementException e) {
			return data.get(highKey).volume;
		}

		double lp = regression.minus(lowKey).divide(highKey.minus(lowKey)).to(
				Dimensionless.UNIT).doubleValue(Dimensionless.UNIT);

		Amount<Volume> lowVal = data.get(lowKey).volume;
		Amount<Volume> highVal = data.get(highKey).volume;

		return lowVal.times(1 - lp).plus(highVal.times(lp));
	}

	private void fillInData() {
		double max = webThickness().doubleValue(SI.MILLIMETER);
		double delta = max / 100;
		rStep = Amount.valueOf(delta, SI.MILLIMETER).divide(10);
		for (double r = 0; r <= max + 1; r += delta) {
			RegEntry e = new RegEntry();
			Amount<Length> regression = Amount.valueOf(r, SI.MILLIMETER);

			Amount<Length> rLen = length.minus(regression.times(2));
			if (rLen.isLessThan(Amount.valueOf(0, SI.MILLIMETER)))
				break;

			System.out.println("Calculating area for regression " + regression);

			java.awt.geom.Area burn = getArea(regression);
			if (burn.isEmpty())
				break;
			burn.subtract(getArea(regression.plus(Amount.valueOf(.001,
					SI.MILLIMETER))));

			//Amount<Area> xSection = crossSectionArea(getArea(regression));
			
			Amount<Area> xSection = crossSectionArea(regression);

			e.volume = xSection.times(rLen).to(Volume.UNIT);

			e.surfaceArea = perimeter(burn).divide(2).times(rLen).plus(
					xSection.times(2)).to(Area.UNIT);

			data.put(regression, e);

		}

		RegEntry e = new RegEntry();
		e.surfaceArea = Amount.valueOf(0, Area.UNIT);
		e.volume = Amount.valueOf(0, Volume.UNIT);
		data.put(webThickness(), e);
	}

	
	private Amount<Area> crossSectionArea(Amount<Length> regression) {
		//Get the PLUS shape and sum its area
		java.awt.geom.Area plus = getPlus(regression);
		Amount<Area> plusArea = Amount.valueOf(0, SI.SQUARE_METRE);
		for (java.awt.geom.Area a : separate(plus)) {
			plusArea = plusArea.plus(area(a));
		}
		
		//Get the MINUS shape, intersect it with PLUS to get just the parts
		//that are removed, sum it's area
		java.awt.geom.Area minus = getMinus(regression);
		minus.intersect(plus);
		Amount<Area> minusArea = Amount.valueOf(0, SI.SQUARE_METRE);
		for (java.awt.geom.Area a : separate(minus)) {
			minusArea = minusArea.plus(area(a));
		}
		
		//Subtract PLUS from MINUS and return
		Amount<Area> area = plusArea.minus(minusArea);
		System.out.println(area.to(SI.MILLIMETER.pow(2)));
		return area;
	}

	@Override
	public Amount<Length> webThickness() {
		return webThickness;
	}

	private Amount<Length> perimeter(java.awt.geom.Area a) {
		//TODO: I think I need to handle seg_close!!
		PathIterator i = a.getPathIterator(new AffineTransform(), .001);
		double x = 0, y = 0;
		double len = 0;
		while (!i.isDone()) {
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			if (type == PathIterator.SEG_LINETO) {
				// System.out.println("Line");
				double nx = coords[0];
				double ny = coords[1];
				// System.out.println(x+","+y+ " to " + nx+"," + ny);
				len += Math.sqrt(Math.pow(x - nx, 2) + Math.pow(y - ny, 2));
				x = nx;
				y = ny;
			} else if (type == PathIterator.SEG_MOVETO) {
				// System.out.println("Move");
				x = coords[0];
				y = coords[1];
			} else {
				// System.err.println("Got " + type);
			}
			i.next();
		}
		return Amount.valueOf(len, SI.MILLIMETER);
	}

	private void findWebThickness() {
		java.awt.geom.Area a = getArea(Amount.valueOf(0, SI.MILLIMETER));
		Rectangle r = a.getBounds();
		double max = r.getWidth() < r.getHeight() ? r.getHeight() : r
				.getWidth(); // The max size
		double min = 0;
		double guess;
		while (true) {
			guess = min + (max - min) / 2; // Guess halfway through
			System.out.println("Min: " + min + " Guess: " + guess + " Max: "
					+ max);
			a = getArea(Amount.valueOf(guess, SI.MILLIMETER));
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
		webThickness = Amount.valueOf(guess, SI.MILLIMETER);
		if (webThickness.isGreaterThan(length.divide(2)))
			webThickness = length.divide(2);
	}

	private java.awt.geom.Area getArea(Amount<Length> regression) {
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

			double d = plus ? -2 * mm : 2 * mm;

			double w = r.getWidth() + d;
			double h = r.getHeight() + d;
			double x = r.getX() - d / 2;
			double y = r.getY() - d / 2;

			return new Rectangle2D.Double(x, y, w, h);
		}
		return null;
	}

	public static void main(String args[]) throws Exception {
		ExtrudedGrain e = new ExtrudedGrain();
		new GrainPanel(e).show();
	}

	@Override
	public void draw(Graphics2D g2d, Amount<Length> regression) {

		java.awt.geom.Area reg = getArea(regression);
		java.awt.geom.Area burn = getArea(regression);
		burn.subtract(getArea(regression.plus(Amount.valueOf(.001,
				SI.MILLIMETER))));
		java.awt.geom.Area noreg = getArea(Amount.valueOf(0, SI.MILLIMETER));

		Rectangle bounds = noreg.getBounds();
		g2d.scale(200 / bounds.getWidth(), 200 / bounds.getHeight());
		g2d.translate(-bounds.getX(), -bounds.getY());

		g2d.setStroke(new BasicStroke(0.5f));

		g2d.setColor(Color.GRAY);
		g2d.fill(reg);
		g2d.setColor(Color.RED);
		g2d.draw(burn);
		g2d.setColor(Color.BLACK);
		g2d.draw(noreg);
		

	}

	/*
	 * Separate an area into multiple distinct area.
	 * Area CAN NOT HAVE HOLES. HOLES WILL BE RETURNED AS AREAS,
	 * SO A DONUT WILL TURN INTO TWO CIRCLES.
	 */
	private Set<java.awt.geom.Area> separate(java.awt.geom.Area a) {
		Set<java.awt.geom.Area> res = new HashSet<java.awt.geom.Area>();
		PathIterator i = a.getPathIterator(new AffineTransform());
		GeneralPath cur = null;

		while (!i.isDone()) {
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_CLOSE:
				cur.closePath();
				if (cur != null ){
					java.awt.geom.Area area = new java.awt.geom.Area(cur);
					if ( !a.isEmpty() )
						res.add(area);
				}
				cur = new GeneralPath(i.getWindingRule());
				break;
			case PathIterator.SEG_MOVETO:
				if (cur != null ){
					java.awt.geom.Area area = new java.awt.geom.Area(cur);
					if ( !a.isEmpty() )
						res.add(area);
				}
				cur = new GeneralPath(i.getWindingRule());
				cur.moveTo(coords[0], coords[1]);
				break;
			case PathIterator.SEG_CUBICTO:
				cur.curveTo(coords[0], coords[1], coords[2], coords[3],
						coords[4], coords[5]);
				break;
			case PathIterator.SEG_LINETO:
				cur.lineTo(coords[0], coords[1]);
				break;
			case PathIterator.SEG_QUADTO:
				cur.quadTo(coords[0], coords[1], coords[2], coords[3]);
				break;

			}
			i.next();
		}

		return res;
	}
	
	/*
	 * Return the Area of a singular polygon (NO HOLES OR DISJOINT PARTS).
	 * Coordinates assumed to be in MM.
	 * http://valis.cs.uiuc.edu/~sariel/research/CG/compgeom/msg00831.html
	 * http://stackoverflow.com/questions/451426/how-do-i-calculate-the-surface-area-of-a-2d-polygon
	 * http://www.wikihow.com/Calculate-the-Area-of-a-Polygon
	 */
	private Amount<Area> area(java.awt.geom.Area a) {
		if ( !a.isSingular() )
			throw new IllegalArgumentException("Can not calculate area of non-singular shape!");
		PathIterator i = a.getPathIterator(new AffineTransform(), .001);
		
		
		double x = 0, y = 0, sx = 0, sy = 0;
		double nx, ny;
		double area = 0;
		while (!i.isDone()) {
			double coords[] = new double[6];
			int type = i.currentSegment(coords);
			switch( type ){
			case PathIterator.SEG_CLOSE:
				//Go back to the start
				nx = sx;
				ny = sy;
				area += x * ny;
				area -= y * nx;
				break;
			case PathIterator.SEG_LINETO:
				nx = coords[0];
				ny = coords[1];
				area += x * ny;
				area -= y * nx;

				//Remember the last points
				x = nx;
				y = ny;
				
				break;
			case PathIterator.SEG_MOVETO:
				//Remember the starting point
				x = sx = coords[0];
				y = sy = coords[1];
				break;
			default:
				System.err.println("Got " + type);
			}
			i.next();
		}
		
		area = area / 2.0; // Result so far is double the signed area
		
		if ( area < 0 ) //Depending on winding it could be negative
			area = area * -1.0;
		
		System.out.println(area);
		
		return Amount.valueOf(area, SI.MILLIMETER.pow(2)).to(Area.UNIT);
	}

}
