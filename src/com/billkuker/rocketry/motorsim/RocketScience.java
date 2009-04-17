package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class RocketScience {
	public static Unit<Impulse> NEWTON_SECOND = new ProductUnit<Impulse>(SI.NEWTON.times(SI.SECOND));

	public interface MolarWeight extends Quantity {
		public static final Unit<MolarWeight> UNIT = new ProductUnit<MolarWeight>(
				SI.KILOGRAM.divide(SI.MOLE));
	}
	
	public interface Impulse extends Quantity {
		public static Unit<Impulse> UNIT = NEWTON_SECOND;
	}


}
