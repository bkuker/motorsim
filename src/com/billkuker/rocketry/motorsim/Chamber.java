package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Volume;

import org.jscience.physics.amount.Amount;

public interface Chamber {
	public Amount<Volume> chamberVolume();
	
	public Amount<Pressure> burstPressure();
	
}
