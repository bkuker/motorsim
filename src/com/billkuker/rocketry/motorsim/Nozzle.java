package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Pressure;

import org.jscience.physics.amount.Amount;

public interface Nozzle {
	public Amount<Area> throatArea();
	
	public Amount<Force> thrust(Amount<Pressure> Po, Amount<Pressure> Pe, Amount<Pressure> Patm, final double k );
}
