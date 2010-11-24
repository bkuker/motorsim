package com.billkuker.rocketry.motorsim.grain.util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.grain.ExtrudedGrain;

public abstract class ExtrudedShapeGrain extends ExtrudedGrain {
	
	public static ExtrudedShapeGrain DEFAULT_GRAIN = new ExtrudedShapeGrain(){
		{
			try{
				Shape outside = new Ellipse2D.Double(0, 0, 30, 30);
				xsection.add(outside);
				xsection.inhibit(outside);
				xsection.subtract(new Ellipse2D.Double(10,10, 10, 10));
				setLength(Amount.valueOf(70, SI.MILLIMETER));
				setForeEndInhibited(false);
				setAftEndInhibited(false);
			} catch ( Exception e ){
				throw new Error(e);
			}
		}
	};

	protected BurningShape xsection = new BurningShape();

	protected Amount<Length> webThickness;

	public Amount<Area> surfaceArea(Amount<Length> regression) {
		Amount<Area> zero = Amount.valueOf(0, Area.UNIT);
		
		if (regression.isGreaterThan(webThickness()))
			return zero;
		
		Amount<Length> rLen = regressedLength(regression);
		
		if (rLen.isLessThan(Amount.valueOf(0, SI.MILLIMETER)))
			return zero;

		java.awt.geom.Area burn = getCrossSection(regression);
		
		if (burn.isEmpty())
			return zero;
		
		burn.subtract(getCrossSection(regression.plus(Amount.valueOf(.001,
				SI.MILLIMETER))));
	
		Amount<Area> xSection = ShapeUtil.area(xsection.getShape(regression));
		
		Amount<Area> sides = ShapeUtil.perimeter(burn).divide(2).times(rLen).to(Area.UNIT);
		Amount<Area> ends = xSection.times(numberOfBurningEnds(regression));
		
		return sides.plus(ends);

	}

	public Amount<Volume> volume(Amount<Length> regression) {
		Amount<Volume> zero = Amount.valueOf(0, Volume.UNIT);
		
		Amount<Length> rLen = regressedLength(regression);
		
		if (rLen.isLessThan(Amount.valueOf(0, SI.MILLIMETER)))
			return zero;
		
		Amount<Area> xSection = ShapeUtil.area(xsection.getShape(regression));

		return xSection.times(rLen).to(Volume.UNIT);

	}


	public Amount<Length> webThickness() {
		if ( webThickness != null )
			return webThickness;
		java.awt.geom.Area a = getCrossSection(Amount.valueOf(0, SI.MILLIMETER));
		Rectangle r = a.getBounds();
		double max = r.getWidth() < r.getHeight() ? r.getHeight() : r
				.getWidth(); // The max size
		double min = 0;
		double guess;
		while (true) {
			guess = min + (max - min) / 2; // Guess halfway through

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
		
		//TODO Need to check # of burning ends!
		if (webThickness.isGreaterThan(getLength().divide(2)))
			webThickness = getLength().divide(2);
		
		return webThickness;
	}

	public java.awt.geom.Area getCrossSection(Amount<Length> regression) {
		return xsection.getShape(regression);
	}
	
	public java.awt.geom.Area getSideView(Amount<Length> regression) {
		java.awt.geom.Area res = new java.awt.geom.Area();
		
		Amount<Length> rLen = regressedLength(regression);
		
		double rLenmm = rLen.doubleValue(SI.MILLIMETER);
		
		for( java.awt.geom.Area a : ShapeUtil.separate(getCrossSection(regression))){
			Rectangle2D bounds = a.getBounds2D();
			Rectangle2D side = new Rectangle2D.Double(bounds.getMinX(), -rLenmm/2.0, bounds.getWidth(), rLenmm);
			res.add(new java.awt.geom.Area(side));
		}
		
		//Shift up or down based on burning ends
		if ( isForeEndInhibited() ){
			res.transform(AffineTransform.getTranslateInstance(0, +rLenmm/2.0));
		}
		if ( isAftEndInhibited() ){
			res.transform(AffineTransform.getTranslateInstance(0, -rLenmm/2.0));
		}
		return res;
	}

}
