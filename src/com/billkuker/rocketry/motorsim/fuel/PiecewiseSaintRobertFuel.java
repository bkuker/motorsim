package com.billkuker.rocketry.motorsim.fuel;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.VolumetricDensity;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.fuel.SaintRobertFuel.Type;

public abstract class PiecewiseSaintRobertFuel extends SaintRobertFuel {

	private class Entry{
		double a;
		double n;
	}
	
	private SortedMap<Amount<Pressure>, Entry> entries = new TreeMap<Amount<Pressure>, Entry>();
	
	public PiecewiseSaintRobertFuel(Type t) {
		super(t);
	}
	
	public void add(Amount<Pressure> p, final double _a, final double _n){
		entries.put(p, new Entry(){{a = _a; n = _n;}});
	}

	@Override
	protected double burnrateCoefficient(Amount<Pressure> pressure) {
		try {
			Amount<Pressure> samplePressure = entries.tailMap(pressure).firstKey();
			Entry e = entries.get(samplePressure);
			return e.a;
		} catch ( NoSuchElementException e ){
			return 0;
		}
	}

	@Override
	protected double burnrateExponent(Amount<Pressure> pressure) {
		try {
			Amount<Pressure> samplePressure = entries.tailMap(pressure).firstKey();
			Entry e = entries.get(samplePressure);
			return e.n;
		} catch ( NoSuchElementException e ){
			return 0;
		}
	}

}
