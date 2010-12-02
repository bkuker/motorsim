package com.billkuker.rocketry.motorsim;



import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.MassFlowRate;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.Constants;

import com.billkuker.rocketry.motorsim.Validating.ValidationException;

public class Burn {
	private static Logger log = Logger.getLogger(Burn.class);
	
	private static final BurnSettings settings = new BurnSettings();
	public static final BurnSettings getBurnSettings(){
		return settings;
	}
	
	/**
	 * A class representing all the settigns one can change on a burn
	 * @author bkuker
	 */
	public static class BurnSettings {
		private BurnSettings(){};

		public enum BurnVolumeMethod {
			DeltaVolume,
			SurfaceTimesRegression;
		}
		
		private BurnVolumeMethod volumeMethod = BurnVolumeMethod.SurfaceTimesRegression;
		private double regStepIncreaseFactor = 1.01;
		private double regStepDecreaseFactor = .5;
		private Amount<Pressure> chamberPressureMaxDelta = Amount.valueOf(.5, SI.MEGA(SI.PASCAL));
		private Amount<Pressure> endPressure = Amount.valueOf(.1, RocketScience.PSI);
		private Amount<Pressure> atmosphereicPressure = Amount.valueOf(101000, SI.PASCAL);
		
		public void setVolumeMethod(BurnVolumeMethod volumeMethod) {
			this.volumeMethod = volumeMethod;
		}
		public BurnVolumeMethod getVolumeMethod() {
			return volumeMethod;
		}
		public double getRegStepIncreaseFactor() {
			return regStepIncreaseFactor;
		}
		public void setRegStepIncreaseFactor(double regStepIncreaseFactor) {
			this.regStepIncreaseFactor = regStepIncreaseFactor;
		}
		public double getRegStepDecreaseFactor() {
			return regStepDecreaseFactor;
		}
		public void setRegStepDecreaseFactor(double regStepDecreaseFactor) {
			this.regStepDecreaseFactor = regStepDecreaseFactor;
		}
		public Amount<Pressure> getChamberPressureMaxDelta() {
			return chamberPressureMaxDelta;
		}
		public void setChamberPressureMaxDelta(Amount<Pressure> chamberPressureMaxDelta) {
			this.chamberPressureMaxDelta = chamberPressureMaxDelta;
		}
		public Amount<Pressure> getEndPressure() {
			return endPressure;
		}
		public void setEndPressure(Amount<Pressure> endPressure) {
			this.endPressure = endPressure;
		}
		public Amount<Pressure> getAtmosphereicPressure() {
			return atmosphereicPressure;
		}
		public void setAtmosphereicPressure(Amount<Pressure> atmosphereicPressure) {
			this.atmosphereicPressure = atmosphereicPressure;
		}
	}
	
	protected final Motor motor;
	
	private boolean burning = false;
	private boolean done = false;
	
	public interface BurnProgressListener{
		public void setProgress(float p);
		public void burnComplete();
	}
		
	private Set<BurnProgressListener> bpls = new HashSet<Burn.BurnProgressListener>();

	public class Interval{
		public Amount<Duration> time;
		public Amount<Duration> dt;
		public Amount<Length> regression;
		public Amount<Pressure> chamberPressure;
		Amount<Mass> chamberProduct;
		public Amount<Force> thrust;

		public String toString(){
			return time + " " + dt + " " + regression + " " + chamberPressure + " " + chamberProduct;
		}
	}
	
	protected SortedMap<Amount<Duration>,Interval> data = new TreeMap<Amount<Duration>, Interval>();
	
	public SortedMap<Amount<Duration>,Interval> getData(){
		if ( !done )
			throw new IllegalStateException("Burn not complete!");
		return data;
	}
	
	public Motor getMotor(){
		return motor;
	}

	public Amount<Duration> burnTime(){
		return getData().lastKey();
	}
	
	public Burn(Motor m){
		try {
			m.validate();
		} catch (ValidationException e) {
			throw new IllegalArgumentException("Invalid Motor: " + e.getMessage());
		}
		motor = m;
	}
	
	public void addBurnProgressListener( BurnProgressListener bpl ){
		bpls.add(bpl);
		if ( done )
			bpl.burnComplete();
	}
	
	public void burn(){
		synchronized(this){
			if ( burning )
				throw new IllegalStateException("Already burning!");
			burning = true;
		}
		log.info("Starting burn...");
		for (BurnProgressListener bpl : bpls ){
			bpl.setProgress(0);
		}
		
		int endPressureSteps = 0;
		long start = new Date().getTime();
		
		Amount<Length> regStep = Amount.valueOf(0.01, SI.MILLIMETER);

		Interval initial = new Interval();
		initial.time = Amount.valueOf(0, SI.SECOND);
		initial.dt = Amount.valueOf(0, SI.SECOND);
		initial.regression = Amount.valueOf(0, SI.MILLIMETER);
		initial.chamberPressure = settings.getAtmosphereicPressure();
		initial.chamberProduct = Amount.valueOf(0, SI.KILOGRAM);
		initial.thrust = Amount.valueOf(0, SI.NEWTON);
		
		data.put(Amount.valueOf(0, SI.SECOND), initial);
		
		step:
		for ( int i = 0; i < 5000; i++ ) {
			assert(positive(regStep));
			regStep = regStep.times(settings.getRegStepIncreaseFactor());
			
			Interval prev = data.get(data.lastKey());
			log.debug(prev);
			log.debug("Step " + i + " ==============================");
			Interval next = new Interval();
			
			Amount<Velocity> burnRate = motor.getFuel().burnRate(prev.chamberPressure);
			assert(positive(burnRate));
			
			log.debug("Burn Rate: " + burnRate);
			
			Amount<Duration> dt = regStep.divide(burnRate).to(Duration.UNIT);
			assert(positive(dt));
			next.dt = dt;
			

			
			log.debug("Dt: " + dt);
			
			next.regression = prev.regression.plus(regStep);
			assert(positive(next.regression));
			
			log.debug("Regression: " + next.regression);
			
			//Update BurnProgressListeners
			Amount<Dimensionless> a = next.regression.divide(motor.getGrain().webThickness()).to(Dimensionless.UNIT);
			for (BurnProgressListener bpl : bpls ){
				bpl.setProgress((float)a.doubleValue(Dimensionless.UNIT));
			}

			
			next.time = prev.time.plus(dt);
			
			//log.debug("Vold: " + motor.getGrain().volume(prev.regression).to(SI.MILLIMETER.pow(3)));
			
			//log.debug("Vnew: " + motor.getGrain().volume(next.regression).to(SI.MILLIMETER.pow(3)));
			
			Amount<Volume> volumeBurnt;
			if ( settings.getVolumeMethod() == BurnSettings.BurnVolumeMethod.DeltaVolume ){
				volumeBurnt = motor.getGrain().volume(prev.regression).minus(motor.getGrain().volume(next.regression));
			} else {
				volumeBurnt = motor.getGrain().surfaceArea(prev.regression).times(regStep).to(Volume.UNIT);
			}
			assert(positive(volumeBurnt));
			//log.info("Volume Burnt: " + volumeBurnt.to(SI.MILLIMETER.pow(3)));
			
			Amount<MassFlowRate> mGenRate = volumeBurnt.times(motor.getFuel().getIdealDensity().times(motor.getFuel().getDensityRatio())).divide(dt).to(MassFlowRate.UNIT);
			assert(positive(mGenRate));
			
			//log.debug("Mass Gen Rate: " + mGenRate);
			
			//Calculate specific gas constant
			Amount<?> specificGasConstant = Constants.R.divide(motor.getFuel().getCombustionProduct().getEffectiveMolarWeight());
			//This unit conversion helps JScience to convert nozzle flow rate to
			//kg/s a little later on I verified the conversion by hand and
			//JScience checks it too.
			specificGasConstant = convertSpecificGasConstantUnits(specificGasConstant);
			
			//Calculate chamber temperature
			Amount<Temperature> chamberTemp = motor.getFuel().getCombustionProduct().getIdealCombustionTemperature().times(motor.getFuel().getCombustionEfficiency());
			
			Amount<MassFlowRate> mNozzle;
			{
				Amount<Pressure> pDiff = prev.chamberPressure.minus(settings.getAtmosphereicPressure());
				//log.debug("Pdiff: " + pDiff);
				Amount<Area> aStar = motor.getNozzle().throatArea();
				double k = motor.getFuel().getCombustionProduct().getRatioOfSpecificHeats();
				double kSide = Math.sqrt(k) * Math.pow((2/(k+1)) , (((k+1)/2)/(k-1)));
				Amount<?> sqrtPart = specificGasConstant.times(chamberTemp).sqrt();
				mNozzle = pDiff.times(aStar).times(kSide).divide(sqrtPart).to(MassFlowRate.UNIT);
				//log.debug("Mass Exit Rate: " + mNozzle.to(MassFlowRate.UNIT));		
			}
			assert(positive(mNozzle));
			
			Amount<MassFlowRate> massStorageRate = mGenRate.minus(mNozzle);
			
			//log.debug("Mass Storage Rate: " + massStorageRate);

			next.chamberProduct = prev.chamberProduct.plus(massStorageRate.times(dt));
			
			//Product can not go negative!
			if ( !positive(next.chamberProduct) ){
				log.warn("ChamberProduct Negative on step " + i + "!, Adjusting regstep down and repeating step!");
				regStep = regStep.times(settings.getRegStepDecreaseFactor());
				continue step;
			}
			assert(positive(next.chamberProduct));
			if ( next.chamberProduct.isLessThan(Amount.valueOf(0, SI.KILOGRAM)) )
				next.chamberProduct = Amount.valueOf(0, SI.KILOGRAM);
			
			//log.debug("Chamber Product: " + next.chamberProduct);
			
			Amount<VolumetricDensity> combustionProductDensity = next.chamberProduct.divide(motor.getChamber().chamberVolume().minus(motor.getGrain().volume(next.regression))).to(VolumetricDensity.UNIT);
			
			log.debug("Product Density: " + combustionProductDensity);
			
			next.chamberPressure = combustionProductDensity.times(specificGasConstant).times(chamberTemp).plus(settings.getAtmosphereicPressure()).to(Pressure.UNIT);
			assert(positive(next.chamberPressure));
			
			next.chamberPressure = Amount.valueOf(
					next.chamberPressure.doubleValue(SI.PASCAL),
					SI.PASCAL);
			
			Amount<Pressure> dp = next.chamberPressure.minus(prev.chamberPressure);
			if ( dp.abs().isGreaterThan(settings.getChamberPressureMaxDelta())){
				log.warn("DP " + dp + " too big!, Adjusting regstep down and repeating step!");
				regStep = regStep.times(settings.getRegStepDecreaseFactor());
				continue step;
			}
			
			next.thrust = motor.getNozzle().thrust(next.chamberPressure, settings.getAtmosphereicPressure(), settings.getAtmosphereicPressure(), motor.getFuel().getCombustionProduct().getRatioOfSpecificHeats2Phase());
			assert(positive(next.thrust));
			
			if ( i > 100 && next.chamberPressure.minus(settings.getAtmosphereicPressure()).abs().isLessThan(settings.getEndPressure())){
				log.info("Pressure at ~Patm on step " + i);
				endPressureSteps++;
				if ( endPressureSteps > 5 )
					break;
			}
			
			data.put(data.lastKey().plus(dt), next);
		}

		long time = new Date().getTime() - start;
		log.info("Burn took " + time + " millis.");
		done = true;
		for (BurnProgressListener bpl : bpls ){
			bpl.burnComplete();
		}
	}
	
	@SuppressWarnings("unchecked")
	/*
	 * This converts the units of this constant to something JScience is able
	 * to work from. This conversion is unchecked at compile time, but
	 * JScience keeps me honest at runtime.
	 */
	private Amount convertSpecificGasConstantUnits(Amount a){
		return a.to(
				SI.METER.pow(2).divide(SI.SECOND.pow(2).times(SI.KELVIN)));
	}
	
	public Amount<Pressure> pressure(Amount<Duration> time){
		return getData().get(time).chamberPressure;
	}
	
	public Amount<Force> thrust(Amount<Duration> time){
		return getData().get(time).thrust;
	}
	
	public Amount<Dimensionless> kn(Amount<Length> regression){
		return motor.getGrain().surfaceArea(regression).divide(motor.getNozzle().throatArea()).to(Dimensionless.UNIT);
	}
	
	
	private <Q extends Quantity> boolean positive(Amount<Q> a){
		return ( a.isGreaterThan(a.minus(a)) || a.equals(a.minus(a)));
	}

}
