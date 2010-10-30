package com.billkuker.rocketry.motorsim;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.jscience.physics.amount.Amount;

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
				javax.measure.unit.SI.MILLIMETER.divide(javax.measure.unit.SI.SECOND),
				javax.measure.unit.SI.NEWTON,
				javax.measure.unit.SI.MEGA(javax.measure.unit.SI.PASCAL),
				NEWTON_SECOND
		}),
		NONSI(new Unit[]{
				javax.measure.unit.NonSI.INCH,
				javax.measure.unit.NonSI.POUND_FORCE,
				javax.measure.unit.NonSI.INCH.divide(javax.measure.unit.SI.SECOND),
				PSI,
				NEWTON_SECOND
		});
		
		private static UnitPreference preference = SI;

		static {
			Preferences prefs = Preferences.userNodeForPackage(RocketScience.class);
			String p = prefs.get("PreferedUnits", "SI");
			preference = UnitPreference.valueOf(p);
		}
		
		public static UnitPreference getUnitPreference(){
			return preference;
		}
		
		public static void setUnitPreference( final UnitPreference up ){
			preference = up;
			Preferences prefs = Preferences.userNodeForPackage(RocketScience.class);
			prefs.put("PreferedUnits", up.toString());
		}
		
		protected Set<Unit<?>> units = new HashSet<Unit<?>>();
		
		UnitPreference( Unit<?> u[] ){
			for ( Unit<?> uu : u )
				units.add(uu);
		}
		
		@SuppressWarnings("unchecked")
		public <T extends Quantity> Unit<T> getPreferredUnit(Unit<T> u){
			if ( units.contains(u) )
				return u;
			for( Unit<?> ret : units ){
				if ( ret.isCompatible(u) ){
					return (Unit<T>) ret;
				}
			}
			return u;
		}
	}

	public static <T extends Quantity> String toString(Amount<T> a) {
		final NumberFormat nf = new DecimalFormat("##########.###");
		return nf.format(a.doubleValue(a.getUnit())) + " " + a.getUnit();
	}
	
	public static <T extends Quantity> String ammountToRoundedString(Amount<T> a) {
		if (a == null)
			return "NULL";
		Unit<T> u = RocketScience.UnitPreference.preference.getPreferredUnit(a
				.getUnit());
		double d = a.doubleValue(u);

		DecimalFormat df;

		if (Math.abs(d) < 10.0) {
			df = new DecimalFormat("#.##");
		} else if (Math.abs(d) < 100.0) {
			df = new DecimalFormat("#.#");
		} else {
			df = new DecimalFormat("#");
		}

		return df.format(d) + " " + u.toString();
	}

}
