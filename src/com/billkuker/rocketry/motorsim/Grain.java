package com.billkuker.rocketry.motorsim;

import java.awt.Graphics2D;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;

import org.jscience.physics.amount.Amount;

public interface Grain   {
	
	public interface Graphical{
		public void draw( Graphics2D g, Amount<Length> regression );
	}
	
	public interface DiscreteRegression{
		public Amount<Length> optimalRegressionStep();
	}

	public Amount<Area> surfaceArea(Amount<Length> regression);
	
	public Amount<Volume> volume(Amount<Length> regression);
	
	public Amount<Length> webThickness();

}
