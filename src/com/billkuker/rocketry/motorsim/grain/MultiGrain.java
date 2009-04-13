package com.billkuker.rocketry.motorsim.grain;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;

public class MultiGrain implements Grain {
	
	private Grain grain;
	private int count;
	
	public MultiGrain( Grain g, int c ){
		grain = g;
		count = c;
	}

	public Amount<Area> surfaceArea(Amount<Length> regression) {
		return grain.surfaceArea(regression).times(count);
	}

	public Amount<Volume> volume(Amount<Length> regression) {
		return grain.volume(regression).times(count);
	}

	public Amount<Length> webThickness() {
		return grain.webThickness();
	}

	public java.awt.geom.Area getCrossSection(Amount<Length> regression) {
		return grain.getCrossSection(regression);
	}

	public java.awt.geom.Area getSideView(Amount<Length> regression) {
		Rectangle2D unburntBounds = grain.getSideView(Amount.valueOf(0, SI.MILLIMETER)).getBounds2D();
		
		java.awt.geom.Area ret = new java.awt.geom.Area();
		java.awt.geom.Area g = grain.getSideView(regression);

		for ( int i = 0 ; i < count ; i++ ){
			ret.add(g);
			ret.transform(AffineTransform.getTranslateInstance(0, unburntBounds.getHeight() + 10));
		}
		return ret;
	}

}
