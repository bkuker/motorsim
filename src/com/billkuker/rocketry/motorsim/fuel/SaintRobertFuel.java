package com.billkuker.rocketry.motorsim.fuel;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;

public abstract class SaintRobertFuel implements Fuel {
	
	protected enum Type{
		Si(
				SI.MILLIMETER.divide(SI.SECOND).asType(Velocity.class),
				SI.MEGA(SI.PASCAL).asType(Pressure.class)),
		English(
				NonSI.INCH.divide(SI.SECOND).asType(Velocity.class),
				NonSI.POUND_FORCE.divide(NonSI.INCH.pow(2)).asType(Pressure.class))
		;
		
		private final Unit<Velocity> v;
		private final Unit<Pressure> p;
		
		Type( Unit<Velocity> v, Unit<Pressure> p){
			this.p = p;
			this.v = v;
		}
	}
	
	private Type t = Type.Si;
	
	public SaintRobertFuel(Type t){
		this.t = t;
	}

	public Amount<Velocity> burnRate(Amount<Pressure> pressure) {

		
		double p = pressure.doubleValue(t.p);
		double a = burnrateCoefficient(pressure);
		double n = burnrateExponent(pressure);
		
		return burnrateConstant().plus(Amount.valueOf( a*Math.pow(p,n), t.v ));
	}
	
	protected abstract double burnrateCoefficient(Amount<Pressure> pressure);
	
	protected abstract double burnrateExponent(Amount<Pressure> pressure);
	
	protected Amount<Velocity> burnrateConstant(){
		return Amount.valueOf(0, Velocity.UNIT);
	}

	public abstract Amount<VolumetricDensity> idealDensity();

}
