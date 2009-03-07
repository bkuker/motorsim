package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class RocketScience {

	public interface MolarWeight extends Quantity {
		public static final Unit<MolarWeight> UNIT = new ProductUnit<MolarWeight>(
				SI.KILOGRAM.divide(SI.MOLE));
	}
	

}
