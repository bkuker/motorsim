package com.billkuker.rocketry.motorsim.grain;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.MotorPart;

public class MultiGrain extends MotorPart implements Grain, Grain.Composite, PropertyChangeListener {
	
	private Grain grain = null;
	private int count = 1;
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		int old = this.count;
		this.count = count;
		firePropertyChange("Count", old, count );
	}
	
	public void setGrain(Grain g){
		Grain old = grain;
		grain = g;
		if ( g instanceof MotorPart ){
			((MotorPart)g).addPropertyChangeListener(this);
		}
		firePropertyChange("Grain", old, grain);
	}
	
	public Grain getGrain(){
		return grain;
	}

	private double flush = 1;
	private Amount<Length> delay = Amount.valueOf(0, SI.MILLIMETER);
	
	public MultiGrain(){
	}
	
	public MultiGrain( Grain g, int c ){
		count = c;
		setGrain(g);
	}
	
	private Amount<Length> getAdjustedRegression(Amount<Length> regression, int grain){
		return regression.minus(delay.times(grain)).times(Math.pow(flush,grain));
	}

	public Amount<Area> surfaceArea(Amount<Length> regression) {
		Amount<Area> ret = Amount.valueOf(0, SI.SQUARE_METRE);
		for ( int i = 0; i < count; i++ ){
			ret = ret.plus(grain.surfaceArea(getAdjustedRegression(regression, i)));
		}
		return ret;
	}

	public Amount<Volume> volume(Amount<Length> regression) {
		Amount<Volume> ret = Amount.valueOf(0, SI.CUBIC_METRE);
		for ( int i = 0; i < count; i++ ){
			ret = ret.plus(grain.volume(getAdjustedRegression(regression, i)));
		}
		return ret;
	}

	public Amount<Length> webThickness() {
		return grain.webThickness().plus(delay.times(count));
	}

	public java.awt.geom.Area getCrossSection(Amount<Length> regression) {
		return grain.getCrossSection(regression);
	}

	public java.awt.geom.Area getSideView(Amount<Length> regression) {
		Rectangle2D unburntBounds = grain.getSideView(Amount.valueOf(0, SI.MILLIMETER)).getBounds2D();
		
		java.awt.geom.Area ret = new java.awt.geom.Area();

		for ( int i = 0 ; i < count ; i++ ){
			java.awt.geom.Area g = grain.getSideView(getAdjustedRegression(regression, i));
			ret.add(g);	
			ret.transform(AffineTransform.getTranslateInstance(0, -(unburntBounds.getHeight() + 10)));
		}
		return ret;
	}

	public List<Grain> getGrains() {
		ArrayList<Grain> ret = new ArrayList<Grain>();
		ret.add(grain);
		return Collections.unmodifiableList(ret);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		firePropertyChange(evt);
	}

}
