package com.billkuker.rocketry.motorsim.grain;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;

public class GrainSet implements Grain, Grain.Composite {
	
	private Vector<Grain> grains = new Vector<Grain>();
	
	private double flush = 1;
	private Amount<Length> delay = Amount.valueOf(0, SI.MILLIMETER);
	
	public GrainSet( Grain... gs ){
		for ( Grain g : gs )
			grains.add(g);
	}
	
	private Amount<Length> getAdjustedRegression(Amount<Length> regression, int grain){
		return regression.minus(delay.times(grain)).times(Math.pow(flush,grain));
	}

	public Amount<Area> surfaceArea(Amount<Length> regression) {
		Amount<Area> ret = Amount.valueOf(0, SI.SQUARE_METRE);
		for ( int i = 0; i < grains.size(); i++ ){
			ret = ret.plus(grains.elementAt(i).surfaceArea(getAdjustedRegression(regression, i)));
		}
		return ret;
	}

	public Amount<Volume> volume(Amount<Length> regression) {
		Amount<Volume> ret = Amount.valueOf(0, SI.CUBIC_METRE);
		for ( int i = 0; i < grains.size(); i++ ){
			ret = ret.plus(grains.elementAt(i).volume(getAdjustedRegression(regression, i)));
		}
		return ret;
	}

	public Amount<Length> webThickness() {
		Amount<Length> max = Amount.valueOf(0, SI.MILLIMETER);;
		for ( int i = 0; i < grains.size(); i++ ){
			Grain g = grains.elementAt(i);
			Amount<Length> t = g.webThickness().plus(delay.times(i));
			if ( t.isGreaterThan(max))
				max = t;
		}
		return max;
	}

	public java.awt.geom.Area getCrossSection(Amount<Length> regression) {
		java.awt.geom.Area a = new java.awt.geom.Area();
		for ( int i = 0; i < grains.size(); i++ ){
			Rectangle2D unburntBounds = grains.elementAt(i).getSideView(Amount.valueOf(0, SI.MILLIMETER)).getBounds2D();
			a.add(grains.elementAt(i).getCrossSection(getAdjustedRegression(regression, i)));
			a.transform(AffineTransform.getTranslateInstance(0, -(unburntBounds.getHeight() + 5)));
		}
		return a;
	}

	public java.awt.geom.Area getSideView(Amount<Length> regression) {
		
		
		java.awt.geom.Area ret = new java.awt.geom.Area();

		for ( int i = 0 ; i < grains.size() ; i++ ){
			Rectangle2D unburntBounds = grains.elementAt(i).getSideView(Amount.valueOf(0, SI.MILLIMETER)).getBounds2D();
			java.awt.geom.Area g = grains.elementAt(i).getSideView(getAdjustedRegression(regression, i));
			ret.add(g);	
			ret.transform(AffineTransform.getTranslateInstance(0, -(unburntBounds.getHeight() + 10)));
		}
		return ret;
	}

	public List<Grain> getGrains() {
		return Collections.unmodifiableList(grains);
	}

}
