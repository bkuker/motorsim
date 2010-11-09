package com.billkuker.rocketry.motorsim.fuel;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.visual.Chart;

public class PiecewiseLinearFuel implements Fuel{
	private static final Logger log = Logger.getLogger(PiecewiseLinearFuel.class);
	
	private String name;
	private URI uri;
	private Amount<VolumetricDensity> density;
	private float densityRatio;
	private float combustionEfficiency;
	
	private class Entry implements Comparable<Entry>{
		Amount<Pressure> pressure;
		Amount<Velocity> burnRate;
		@Override
		public int compareTo(Entry o) {
			if ( o.pressure.approximates(pressure) )
				return 0;
			return o.pressure.isGreaterThan(pressure)?-1:1;
		}
	}
	
	private SortedMap<Amount<Pressure>, Entry> entries = new TreeMap<Amount<Pressure>, Entry>();
	
	protected void add(final Amount<Pressure> p, final Amount<Velocity> r){
		entries.put(p, new Entry(){{pressure = p; burnRate = r;}});
	}
	
	public PiecewiseLinearFuel(){
		add(Amount.valueOf(-1,SI.MEGA(SI.PASCAL)), Amount.valueOf(0, SI.METERS_PER_SECOND));
		add(Amount.valueOf(0,SI.MEGA(SI.PASCAL)), Amount.valueOf(0, SI.METERS_PER_SECOND));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public Amount<VolumetricDensity> getIdealDensity() {
		return density;
	}

	@Override
	public Amount<Velocity> burnRate(Amount<Pressure> pressure) {
		Entry low = null;
		low = entries.get(entries.headMap(pressure).lastKey());
		Entry high = null;
		try {
			high = entries.get(entries.tailMap(pressure).firstKey());
		} catch ( NoSuchElementException e ){
			log.warn("Pressure " + pressure + " is outside of expiermental range for " + this.getName());
			high = low;
			low = entries.get(entries.headMap(low.pressure).lastKey());
		}
		
		Amount<Pressure> lowToHigh = high.pressure.minus(low.pressure);
		Amount<Pressure> lowToTarget = pressure.minus(low.pressure);
		Amount<Dimensionless> frac = lowToTarget.divide(lowToHigh).to(Dimensionless.UNIT);
		
		Amount<Velocity> vdiff = high.burnRate.minus(low.burnRate);
		Amount<Velocity> ret = low.burnRate.plus(vdiff.times(frac));
		
		return ret;
		
	}

	@Override
	public CombustionProduct getCombustionProduct(){
		return null;
	}

	@Override
	public double getDensityRatio() {
		return densityRatio;
	}

	@Override
	public double getCombustionEfficiency() {
		return combustionEfficiency;
	}

	public static void main( String args[]) throws Exception{
		PiecewiseLinearFuel f = new PiecewiseLinearFuel();
		f.add(Amount.valueOf(0,SI.MEGA(SI.PASCAL)), Amount.valueOf(0, SI.METERS_PER_SECOND));
		f.add(Amount.valueOf(2,SI.MEGA(SI.PASCAL)), Amount.valueOf(2, SI.METERS_PER_SECOND));
		f.add(Amount.valueOf(4,SI.MEGA(SI.PASCAL)), Amount.valueOf(1, SI.METERS_PER_SECOND));
		f.add(Amount.valueOf(10,SI.MEGA(SI.PASCAL)), Amount.valueOf(3, SI.METERS_PER_SECOND));
		f.add(Amount.valueOf(20,SI.MEGA(SI.PASCAL)), Amount.valueOf(4, SI.METERS_PER_SECOND));
		Chart<Pressure, Velocity> burnRate = new Chart<Pressure, Velocity>(
				SI.MEGA(SI.PASCAL),
				SI.METERS_PER_SECOND,
				f,
				"burnRate");
		burnRate.setDomain(
				burnRate.new IntervalDomain(
						Amount.valueOf(0, SI.MEGA(SI.PASCAL)),
						Amount.valueOf(11, SI.MEGA(SI.PASCAL)),
						200
						));
		
		burnRate.show();
	}
}
