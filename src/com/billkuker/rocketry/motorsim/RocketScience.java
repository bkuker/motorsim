package com.billkuker.rocketry.motorsim;

import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

public class RocketScience {
	public static Unit<Pressure> PSI = new ProductUnit<Pressure>(NonSI.POUND_FORCE.divide(NonSI.INCH.pow(2)));
	public static Unit<Impulse> NEWTON_SECOND = new ProductUnit<Impulse>(SI.NEWTON.times(SI.SECOND));
	static{
		UnitFormat.getInstance().label(PSI, "psi");
		UnitFormat.getInstance().label(NEWTON_SECOND, "Ns");
	}

	public interface MolarWeight extends Quantity {
		public static final Unit<MolarWeight> UNIT = new ProductUnit<MolarWeight>(
				SI.KILOGRAM.divide(SI.MOLE));
	}
	
	public interface Impulse extends Quantity {
		public static Unit<Impulse> UNIT = NEWTON_SECOND;
	}

	public static enum UnitPreference{
		SI(new Unit[]{
				javax.measure.unit.SI.MILLIMETER,
				javax.measure.unit.SI.NEWTON,
				javax.measure.unit.SI.MEGA(javax.measure.unit.SI.PASCAL),
				NEWTON_SECOND
		}),
		NonSI(new Unit[]{
				javax.measure.unit.NonSI.INCH,
				javax.measure.unit.NonSI.POUND_FORCE,
				PSI,
				NEWTON_SECOND
		});
		
		public static UnitPreference preference = NonSI;
		
		protected Set<Unit<?>> units = new HashSet<Unit<?>>();
		
		UnitPreference( Unit u[] ){
			for ( Unit uu : u )
				units.add(uu);
		}
		
		public Unit getPreferredUnit(Unit u){
			if ( units.contains(u) )
				return u;
			for( Unit ret : units ){
				if ( ret.isCompatible(u) ){
					return ret;
				}
			}
			return u;
		}
	}

}
