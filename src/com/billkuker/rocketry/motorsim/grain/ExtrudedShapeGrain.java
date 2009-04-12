package com.billkuker.rocketry.motorsim.grain;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.MotorPart;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;

public class ExtrudedShapeGrain extends MotorPart implements Grain {

	Set<Shape> plus = new HashSet<Shape>();

	Set<Shape> minus = new HashSet<Shape>();

	Set<Shape> inhibited = new HashSet<Shape>();

	Amount<Length> length = Amount.valueOf(25, SI.MILLIMETER);
	
	boolean endSurfaceInhibited = false;

	Amount<Length> rStep;

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
		minus.add(new Rectangle2D.Double(13, 13, 4, 30));
		//minus.add(new Ellipse2D.Double(12, 12, 6, 6));
		length = Amount.valueOf(70, SI.MILLIMETER);
		/**/

		/*
		 * Plus sign Shape outside = new Ellipse2D.Double(0,0,200,200);
		 * plus.add(outside); inhibited.add(outside); minus.add(new
		 * Rectangle2D.Double(90,40,20,120)); minus.add(new
		 * Rectangle2D.Double(40,90,120,20));
		 */

		findWebThickness();

	}

	@Override
	public Amount<Area> surfaceArea(Amount<Length> regression) {
		Amount<Area> zero = Amount.valueOf(0, Area.UNIT);
		
		if (regression.isGreaterThan(webThickness))
			return zero;
		
		Amount<Length> rLen = length;
		if ( !endSurfaceInhibited ) //Regress length if uninhibited
			rLen = length.minus(regression.times(2));
		
		if (rLen.isLessThan(Amount.valueOf(0, SI.MILLIMETER)))
			return zero;

		java.awt.geom.Area burn = getCrossSection(regression);
		
		if (burn.isEmpty())
			return zero;
		
		burn.subtract(getCrossSection(regression.plus(Amount.valueOf(.001,
				SI.MILLIMETER))));
	
		Amount<Area> xSection = crossSectionArea(regression);

		return perimeter(burn).divide(2).times(rLen).plus(
				xSection.times(2)).to(Area.UNIT);
	}

	@Override
	public Amount<Volume> volume(Amount<Length> regression) {
		Amount<Volume> zero = Amount.valueOf(0, Volume.UNIT);
		
		Amount<Length> rLen = length;
		if ( !endSurfaceInhibited ) //Regress length if uninhibited
			rLen = length.minus(regression.times(2));
		
		if (rLen.isLessThan(Amount.valueOf(0, SI.MILLIMETER)))
			return zero;
		
		Amount<Area> xSection = crossSectionArea(regression);

		return xSection.times(rLen).to(Volume.UNIT);

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
		java.awt.geom.Area a = getCrossSection(Amount.valueOf(0, SI.MILLIMETER));
		Rectangle r = a.getBounds();
		double max = r.getWidth() < r.getHeight() ? r.getHeight() : r
				.getWidth(); // The max size
		double min = 0;
		double guess;
		while (true) {
			guess = min + (max - min) / 2; // Guess halfway through
			System.out.println("Min: " + min + " Guess: " + guess + " Max: "
					+ max);
			a = getCrossSection(Amount.valueOf(guess, SI.MILLIMETER));
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

	@Override
	public java.awt.geom.Area getCrossSection(Amount<Length> regression) {
		java.awt.geom.Area res = getPlus(regression);
		res.subtract(getMinus(regression));
		return res;
	}
	
	@Override
	public java.awt.geom.Area getSideView(Amount<Length> regression) {
		java.awt.geom.Area res = new java.awt.geom.Area();
		
		Amount<Length> rLen = length;
		if ( !endSurfaceInhibited ) //Regress length if uninhibited
			rLen = length.minus(regression.times(2));
		
		double rLenmm = rLen.doubleValue(SI.MILLIMETER);
		
		for( java.awt.geom.Area a : separate(getCrossSection(regression))){
			Rectangle2D bounds = a.getBounds2D();
			Rectangle2D side = new Rectangle2D.Double(bounds.getMinX(), -rLenmm/2.0, bounds.getWidth(), rLenmm);
			res.add(new java.awt.geom.Area(side));
		}
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

	public static void main(String args[]) throws Exception {
		ExtrudedShapeGrain e = new ExtrudedShapeGrain();
		new Editor(e).show();
		new GrainPanel(e).show();
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
				throw new Error("Bad segment type from Flattening Path Iterator");
			}
			i.next();
		}
		
		area = area / 2.0; // Result so far is double the signed area
		
		if ( area < 0 ) //Depending on winding it could be negative
			area = area * -1.0;
		
		
		return Amount.valueOf(area, SI.MILLIMETER.pow(2)).to(Area.UNIT);
	}

	public Amount<Length> getLength() {
		return length;
	}

	public void setLength(Amount<Length> length) throws PropertyVetoException {
		fireVetoableChange("length", this.length, length);
		Amount<Length> old = this.length;
		this.length = length;
		firePropertyChange("length", old, length);
	}

	public boolean isEndSurfaceInhibited() {
		return endSurfaceInhibited;
	}

	public void setEndSurfaceInhibited(boolean endSurfaceInhibited) throws PropertyVetoException {
		fireVetoableChange("endSurfaceInhibited", this.endSurfaceInhibited, endSurfaceInhibited);
		boolean old = this.endSurfaceInhibited;
		this.endSurfaceInhibited = endSurfaceInhibited;
		firePropertyChange("endSurfaceInhibited", old, endSurfaceInhibited);
	}

}
