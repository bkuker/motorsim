package com.billkuker.rocketry.motorsim;

import java.awt.Graphics2D;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;

import org.jscience.physics.amount.Amount;

public interface Grain   {
	
	
	public java.awt.geom.Area getCrossSection(Amount<Length> regression);
	public java.awt.geom.Area getSideView(Amount<Length> regression);
	
	public interface DiscreteRegression{
		public Amount<Length> optimalRegressionStep();
	}

	public Amount<Area> surfaceArea(Amount<Length> regression);
	
	public Amount<Volume> volume(Amount<Length> regression);
	
	public Amount<Length> webThickness();

}
