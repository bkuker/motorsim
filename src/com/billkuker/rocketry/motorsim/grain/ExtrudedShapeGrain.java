package com.billkuker.rocketry.motorsim.grain;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;

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

	BurningShape xsection = new BurningShape();

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
		xsection.add(outside);
		xsection.inhibit(outside);
		xsection.subtract(new Rectangle2D.Double(13, 13, 4, 30));
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

		return ShapeUtil.perimeter(burn).divide(2).times(rLen).plus(
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
		java.awt.geom.Area plus = xsection.getPlus(regression);
		Amount<Area> plusArea = Amount.valueOf(0, SI.SQUARE_METRE);
		for (java.awt.geom.Area a : ShapeUtil.separate(plus)) {
			plusArea = plusArea.plus(ShapeUtil.area(a));
		}
		
		//Get the MINUS shape, intersect it with PLUS to get just the parts
		//that are removed, sum it's area
		java.awt.geom.Area minus = xsection.getMinus(regression);
		minus.intersect(plus);
		Amount<Area> minusArea = Amount.valueOf(0, SI.SQUARE_METRE);
		for (java.awt.geom.Area a : ShapeUtil.separate(minus)) {
			minusArea = minusArea.plus(ShapeUtil.area(a));
		}
		
		//Subtract PLUS from MINUS and return
		Amount<Area> area = plusArea.minus(minusArea);

		return area;
	}

	@Override
	public Amount<Length> webThickness() {
		return webThickness;
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
		return xsection.getShape(regression);
	}
	
	@Override
	public java.awt.geom.Area getSideView(Amount<Length> regression) {
		java.awt.geom.Area res = new java.awt.geom.Area();
		
		Amount<Length> rLen = length;
		if ( !endSurfaceInhibited ) //Regress length if uninhibited
			rLen = length.minus(regression.times(2));
		
		double rLenmm = rLen.doubleValue(SI.MILLIMETER);
		
		for( java.awt.geom.Area a : ShapeUtil.separate(getCrossSection(regression))){
			Rectangle2D bounds = a.getBounds2D();
			Rectangle2D side = new Rectangle2D.Double(bounds.getMinX(), -rLenmm/2.0, bounds.getWidth(), rLenmm);
			res.add(new java.awt.geom.Area(side));
		}
		return res;
	}
	
	public static void main(String args[]) throws Exception {
		ExtrudedShapeGrain e = new ExtrudedShapeGrain();
		new Editor(e).show();
		new GrainPanel(e).show();
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
