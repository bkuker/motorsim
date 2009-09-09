package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class CylindricalChamber implements Chamber {
	
	private Amount<Length> length = Amount.valueOf(200, SI.MILLIMETER);
	private Amount<Length> oD = Amount.valueOf(31, SI.MILLIMETER);
	private Amount<Length> iD = Amount.valueOf(30, SI.MILLIMETER);

	public Amount<Pressure> burstPressure() {
		return null;
	}

	public Amount<Volume> chamberVolume() {
		return iD.divide(2).pow(2).times(Math.PI).times(length).to(SI.CUBIC_METRE);
	}

	public Amount<Length> getLength() {
		return length;
	}

	public void setLength(Amount<Length> length) {
		this.length = length;
	}

	public Amount<Length> getID() {
		return iD;
	}

	public void setID(Amount<Length> id) {
		iD = id;
	}
	
	public Amount<Length> getOD() {
		return oD;
	}

	public void setOD(Amount<Length> oD) {
		this.oD = oD;
	}

}
