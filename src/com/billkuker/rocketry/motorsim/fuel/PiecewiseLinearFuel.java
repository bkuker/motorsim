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
	private static final Amount<Pressure> ZERO_PRESSURE =Amount.valueOf(0, SI.PASCAL);
	private static final Amount<Velocity> ZERO_VELOCITY =Amount.valueOf(0, SI.METERS_PER_SECOND);

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

	
	private String name = "New Linear Fuel";
	private URI uri;
	private double combustionEfficiency = .97;
	private double densityRatio = .96;
	private Amount<VolumetricDensity> density = Amount.valueOf(1889, 0, SI.KILOGRAM.divide(SI.METER.pow(3))).to(VolumetricDensity.UNIT);

	
	private EditableCombustionProduct product = new EditableCombustionProduct();
	private SortedMap<Amount<Pressure>, Entry> entries ;
	
	public PiecewiseLinearFuel(){
		clear();
	}
	public void add(final Amount<Pressure> p, final Amount<Velocity> r){
		entries.put(p, new Entry(){{pressure = p; burnRate = r;}});
	}
	
	@Override
	public Amount<Velocity> burnRate(final Amount<Pressure> pressure) {
		if ( pressure.isLessThan(ZERO_PRESSURE) )
			return ZERO_VELOCITY;
		
		if ( entries.size() == 1 ){
			return entries.get(entries.firstKey()).burnRate;
		}
		
		if ( entries.containsKey(pressure) ){
			return entries.get(pressure).burnRate;
		}
			
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
		
		if ( ret.isLessThan(ZERO_VELOCITY) )
			return ZERO_VELOCITY;
		
		return ret;
		
	}
	
	public void clear(){
		entries = new TreeMap<Amount<Pressure>, Entry>();
		add(Amount.valueOf(0,SI.MEGA(SI.PASCAL)), Amount.valueOf(0, SI.METERS_PER_SECOND));
	}
	
	@Override
	public double getCombustionEfficiency() {
		return combustionEfficiency;
	}
	

	public void setCombustionEfficiency(double combustionEfficiency) {
		this.combustionEfficiency = combustionEfficiency;
	}
	
	@Override
	public CombustionProduct getCombustionProduct(){
		return product;
	}
	
	@Override
	public double getDensityRatio() {
		return densityRatio;
	}
	
	public void setDensityRatio(double densityRatio) {
		this.densityRatio = densityRatio;
	}

	@Override
	public Amount<VolumetricDensity> getIdealDensity() {
		return density;
	}
	
	public void setIdealDensity(Amount<VolumetricDensity> density) {
		this.density = density;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public URI getURI() {
		return uri;
	}





	
	public static void main( String args[]) throws Exception{
		PiecewiseLinearFuel f = new PiecewiseLinearFuel();
		f.add(Amount.valueOf(0,SI.MEGA(SI.PASCAL)), Amount.valueOf(2, SI.METERS_PER_SECOND));
		//f.add(Amount.valueOf(2,SI.MEGA(SI.PASCAL)), Amount.valueOf(2, SI.METERS_PER_SECOND));
		//f.add(Amount.valueOf(4,SI.MEGA(SI.PASCAL)), Amount.valueOf(1, SI.METERS_PER_SECOND));
		//f.add(Amount.valueOf(10,SI.MEGA(SI.PASCAL)), Amount.valueOf(3, SI.METERS_PER_SECOND));
		//f.add(Amount.valueOf(20,SI.MEGA(SI.PASCAL)), Amount.valueOf(4, SI.METERS_PER_SECOND));
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