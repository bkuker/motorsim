package com.billkuker.rocketry.motorsim.fuel;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience.MolarWeight;
import com.billkuker.rocketry.motorsim.visual.Chart;

public class KNDX extends PiecewiseSaintRobertFuel {

	public KNDX() {
		super(SaintRobertFuel.Type.SI);
		add(Amount.valueOf(		.779, SI.MEGA(SI.PASCAL)), 		8.875,	0.619);
		add(Amount.valueOf(		2.572, SI.MEGA(SI.PASCAL)), 	7.553,	-0.009);
		add(Amount.valueOf(		5.93, SI.MEGA(SI.PASCAL)), 		3.841,	0.688);
		add(Amount.valueOf(		8.502, SI.MEGA(SI.PASCAL)), 	17.20,	-0.148);
		add(Amount.valueOf(		11.2, SI.MEGA(SI.PASCAL)), 		4.775,	0.442);
	}

	@Override
	public Amount<VolumetricDensity> getIdealDensity() {
		return Amount.valueOf(1879, 0, SI.KILOGRAM.divide(SI.METER.pow(3))).to(VolumetricDensity.UNIT);
	}

	public CombustionProduct getCombustionProduct() {
		return new CombustionProduct(){
			
			public Amount<Temperature> getIdealCombustionTemperature() {
				return Amount.valueOf(1710, SI.KELVIN);
			}
		
			public Amount<MolarWeight> getEffectiveMolarWeight() {
				return Amount.valueOf("42.39 kg/kmol").to(MolarWeight.UNIT);
			}
		
			public double getRatioOfSpecificHeats() {
				return 1.1308;
			}
		
			public double getRatioOfSpecificHeats2Phase() {
				return 1.043;
			}
		};
	}
	
	public static void main( String args[]) throws Exception{
		KNDX f = new KNDX();
		
		Chart<Pressure, Velocity> burnRate = new Chart<Pressure, Velocity>(
				SI.MEGA(SI.PASCAL),
				SI.METERS_PER_SECOND,
				f,
				"burnRate");
		burnRate.setDomain(
				burnRate.new IntervalDomain(
						Amount.valueOf(0, SI.MEGA(SI.PASCAL)),
						Amount.valueOf(11, SI.MEGA(SI.PASCAL)),
						20
						));
		
		burnRate.show();
	}

	public double getCombustionEfficiency() {
		return .97;
	}

	public double getDensityRatio() {
		return .96;
	}

}
