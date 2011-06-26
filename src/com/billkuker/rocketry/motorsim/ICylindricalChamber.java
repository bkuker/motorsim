package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

public interface ICylindricalChamber {
	public Amount<Length> getLength();


	public Amount<Length> getID();

	
	public Amount<Length> getOD();
}
