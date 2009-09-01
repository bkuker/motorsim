package com.billkuker.rocketry.motorsim.fuel;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.quantity.Pressure;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

public abstract class PiecewiseSaintRobertFuel extends SaintRobertFuel {
	
	private static final Logger log = Logger.getLogger(PiecewiseSaintRobertFuel.class);

	private class Entry{
		double a;
		double n;
	}
	
	private SortedMap<Amount<Pressure>, Entry> entries = new TreeMap<Amount<Pressure>, Entry>();
	
	protected PiecewiseSaintRobertFuel(Type t) {
		super(t);
	}
	
	protected void add(Amount<Pressure> p, final double _a, final double _n){
		entries.put(p, new Entry(){{a = _a; n = _n;}});
	}

	@Override
	protected double burnrateCoefficient(Amount<Pressure> pressure) {
		try {
			Amount<Pressure> samplePressure = entries.tailMap(pressure).firstKey();
			Entry e = entries.get(samplePressure);
			return e.a;
		} catch ( NoSuchElementException e ){
			log.warn("Pressure " + pressure + " is outside of expiermental range for " + this.getClass().getSimpleName());
			return entries.get(entries.lastKey()).a;
		}
	}

	@Override
	protected double burnrateExponent(Amount<Pressure> pressure) {
		try {
			Amount<Pressure> samplePressure = entries.tailMap(pressure).firstKey();
			Entry e = entries.get(samplePressure);
			return e.n;
		} catch ( NoSuchElementException e ){
			log.warn("Pressure " + pressure + " is outside of expiermental range for " + this.getClass().getSimpleName());
			return entries.get(entries.lastKey()).n;
		}
	}

}
