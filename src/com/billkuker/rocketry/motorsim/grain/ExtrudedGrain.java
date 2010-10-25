package com.billkuker.rocketry.motorsim.grain;

import java.beans.PropertyVetoException;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;

public abstract class ExtrudedGrain implements Grain {
	private boolean foreEndInhibited = false;
	private boolean aftEndInhibited = false;
	private Amount<Length> length = Amount.valueOf(100, SI.MILLIMETER);
	private Amount<Length> endLight = Amount.valueOf(0, SI.MILLIMETER);
	
	protected int numberOfBurningEnds(Amount<Length> regression){
		if ( regression.isLessThan(endLight) )
			return 0;
		return (foreEndInhibited?0:1) + (aftEndInhibited?0:1);
	}
	
	protected Amount<Length> regressedLength(Amount<Length> regression){
		if ( regression.isLessThan(endLight) )
			return length;
		return length.minus(regression.minus(endLight).times(numberOfBurningEnds(regression)));
	}

	public boolean isForeEndInhibited() {
		return foreEndInhibited;
	}

	public void setForeEndInhibited(boolean foreEndInhibited)throws PropertyVetoException {
		this.foreEndInhibited = foreEndInhibited;
	}

	public boolean isAftEndInhibited() {
		return aftEndInhibited;
	}

	public void setAftEndInhibited(boolean aftEndInhibited) throws PropertyVetoException {
		this.aftEndInhibited = aftEndInhibited;
	}

	public Amount<Length> getLength() {
		return length;
	}

	public void setLength(Amount<Length> length) throws PropertyVetoException {
		this.length = length;
	}
}
