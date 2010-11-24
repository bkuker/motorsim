package com.billkuker.rocketry.motorsim.fuel;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience.MolarWeight;

public class KNER extends SaintRobertFuel {

	public KNER() {
		super(SaintRobertFuel.Type.NONSI);
	}
	

	@Override
	protected double burnrateCoefficient(Amount<Pressure> pressure) {
		return .0037;
	}

	@Override
	protected double burnrateExponent(Amount<Pressure> pressure) {
		return .64;
	}

	@Override
	public Amount<VolumetricDensity> getIdealDensity() {
		return Amount.valueOf(1819.9, 0, SI.KILOGRAM.divide(SI.METER.pow(3))).to(VolumetricDensity.UNIT);
	}

	public CombustionProduct getCombustionProduct() {
		return new CombustionProduct(){
			
			public Amount<Temperature> getIdealCombustionTemperature() {
				return Amount.valueOf(1600, SI.KELVIN);
			}
		
			public Amount<MolarWeight> getEffectiveMolarWeight() {
				return Amount.valueOf("38.78 kg/kmol").to(MolarWeight.UNIT);
			}
		
			public double getRatioOfSpecificHeats() {
				return 1.1391;
			}
		
			public double getRatioOfSpecificHeats2Phase() {
				return 1.0426;
			}
		};
	}

	public double getCombustionEfficiency() {
		return .97;
	}

	public double getDensityRatio() {
		return .96;
	}


}
