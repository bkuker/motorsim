package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience.MolarWeight;

public interface Fuel {

	public Amount<VolumetricDensity> idealDensity();

	public Amount<Velocity> burnRate(Amount<Pressure> pressure);
	
	public CombustionProduct getCombustionProduct();
	
	public interface CombustionProduct {
		public Amount<Temperature> idealCombustionTemperature();

		public Amount<MolarWeight> effectiveMolarWeight();
		
		public double ratioOfSpecificHeats();
		
		public double ratioOfSpecificHeats2Phase();
	}

	/*
	@Deprecated
	public Amount<Temperature> idealCombustionTemperature();

	@Deprecated
	public Amount<MolarWeight> effectiveMolarWeight();
	
	@Deprecated
	public double ratioOfSpecificHeats();
	
	@Deprecated
	public double ratioOfSpecificHeats2Phase();*/

}
