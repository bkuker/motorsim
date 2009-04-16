package com.billkuker.rocketry.motorsim.fuel;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience.MolarWeight;

public class KNSU extends SaintRobertFuel {
	
	public KNSU(){
		super(Type.English);
	}

	//@Override 
	public Amount<VolumetricDensity> idealDensity() {
		//return Amount.valueOf(1.889, 0, SI.GRAM.divide(SI.CENTIMETER.pow(3))).to(VolumetricDensity.UNIT);
		return Amount.valueOf(1889, 0, SI.KILOGRAM.divide(SI.METER.pow(3))).to(VolumetricDensity.UNIT);
	}

	@Override
	protected double burnrateCoefficient(Amount<Pressure> pressure) {
		return 0.0665;
	}

	@Override
	protected double burnrateExponent(Amount<Pressure> pressure) {
		return 0.319;
	}
	
	public CombustionProduct getCombustionProduct(){
		return new CombustionProduct(){
		
			public Amount<Temperature> idealCombustionTemperature() {
				return Amount.valueOf(1720, SI.KELVIN);
			}
		
			public Amount<MolarWeight> effectiveMolarWeight() {
				return Amount.valueOf("41.98 kg/kmol").to(MolarWeight.UNIT);
			}
		
			public double ratioOfSpecificHeats() {
				return 1.133;
			}
		
			public double ratioOfSpecificHeats2Phase() {
				return 1.044;
			}
		};
	}

}
