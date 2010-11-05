package com.billkuker.rocketry.motorsim.fuel;

import java.net.URI;
import java.net.URISyntaxException;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.RocketScience;

public abstract class SaintRobertFuel implements Fuel {
	
	public enum Type{
		SI(
				javax.measure.unit.SI.MILLIMETER.divide(javax.measure.unit.SI.SECOND).asType(Velocity.class),
				javax.measure.unit.SI.MEGA(javax.measure.unit.SI.PASCAL).asType(Pressure.class)),
		NONSI(
				NonSI.INCH.divide(javax.measure.unit.SI.SECOND).asType(Velocity.class),
				RocketScience.PSI)
		;
		
		private final Unit<Velocity> v;
		private final Unit<Pressure> p;
		
		Type( Unit<Velocity> v, Unit<Pressure> p){
			this.p = p;
			this.v = v;
		}
	}
	
	private Type t = Type.SI;
	
	public SaintRobertFuel(Type t){
		if ( t == null )
			throw new IllegalArgumentException("Type must be non-null");
		this.t = t;
	}
	
	public Type getType(){
		return t;
	}
	
	protected void setType(final Type t){
		this.t = t;
	}

	public Amount<Velocity> burnRate(Amount<Pressure> pressure) {

		
		double p = pressure.doubleValue(getType().p);
		double a = burnrateCoefficient(pressure);
		double n = burnrateExponent(pressure);
		
		return burnrateConstant().plus(Amount.valueOf( a*Math.pow(p,n), getType().v ));
	}
	
	protected abstract double burnrateCoefficient(Amount<Pressure> pressure);
	
	protected abstract double burnrateExponent(Amount<Pressure> pressure);
	
	protected Amount<Velocity> burnrateConstant(){
		return Amount.valueOf(0, Velocity.UNIT);
	}

	public abstract Amount<VolumetricDensity> getIdealDensity();

	public URI getURI(){
		try {
			return new URI("motorsim:" + this.getClass().getSimpleName());
		} catch (URISyntaxException e) {
			throw new Error("Shouldn't happen", e);
		}
	}
	
	public String getName(){
		return this.getClass().getSimpleName();
	}
	
	public String toString(){
		return getName();
	}
}
