package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience.MolarWeight;

public interface Fuel {

	public Amount<VolumetricDensity> getIdealDensity();

	public Amount<Velocity> burnRate(Amount<Pressure> pressure);
	
	public CombustionProduct getCombustionProduct();
	
	public interface CombustionProduct {
		public Amount<Temperature> getIdealCombustionTemperature();

		public Amount<MolarWeight> getEffectiveMolarWeight();
		
		public double getRatioOfSpecificHeats();
		
		public double getRatioOfSpecificHeats2Phase();
	}

	public double getDensityRatio();
	
	public double getCombustionEfficiency();

}
