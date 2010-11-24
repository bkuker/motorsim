package com.billkuker.rocketry.motorsim.fuel;

import javax.measure.quantity.Temperature;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.RocketScience.MolarWeight;

public class KNSB extends PiecewiseSaintRobertFuel {

	public KNSB() {
		super(SaintRobertFuel.Type.SI);
		add(Amount.valueOf(		.807, SI.MEGA(SI.PASCAL)), 	10.71,	0.625);
		add(Amount.valueOf(		1.5, SI.MEGA(SI.PASCAL)), 	8.763, -0.314);
		add(Amount.valueOf(		3.79, SI.MEGA(SI.PASCAL)), 	7.852,	-0.013);
		add(Amount.valueOf(		7.03, SI.MEGA(SI.PASCAL)), 	3.907,	0.535);
		add(Amount.valueOf(		10.67, SI.MEGA(SI.PASCAL)), 9.653,	0.064);
	}

	@Override
	public Amount<VolumetricDensity> getIdealDensity() {
		return Amount.valueOf(1841, 0, SI.KILOGRAM.divide(SI.METER.pow(3))).to(VolumetricDensity.UNIT);
	}

	public CombustionProduct getCombustionProduct() {
		return new CombustionProduct(){
			
			public Amount<Temperature> getIdealCombustionTemperature() {
				return Amount.valueOf(1600, SI.KELVIN);
			}
		
			public Amount<MolarWeight> getEffectiveMolarWeight() {
				return Amount.valueOf("39.9 kg/kmol").to(MolarWeight.UNIT);
			}
		
			public double getRatioOfSpecificHeats() {
				return 1.1361;
			}
		
			public double getRatioOfSpecificHeats2Phase() {
				return 1.042;
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
