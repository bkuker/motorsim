package com.billkuker.rocketry.motorsim;



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
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.Constants;

public class Burn {
	
	private static Logger log = Logger.getLogger(Burn.class);
	protected final Motor motor;
	
	private static final Amount<Pressure> atmosphereicPressure = Amount.valueOf(101000, SI.PASCAL);
	
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
		return data;
	}
	
	public Motor getMotor(){
		return motor;
	}

	public Amount<Duration> burnTime(){
		return data.lastKey();
	}
	
	public Burn(Motor m){
		motor = m;
		burn();
	}
	
	private void burn(){
		log.info("Starting burn...");
		
		Amount<Length> regStep = Amount.valueOf(0.0119904077, SI.MILLIMETER);

		//if ( motor.getGrain() instanceof Grain.DiscreteRegression )
			//regStep = ((Grain.DiscreteRegression)motor.getGrain()).optimalRegressionStep();
		
		Interval initial = new Interval();
		initial.time = Amount.valueOf(0, SI.SECOND);
		initial.dt = Amount.valueOf(0, SI.SECOND);
		initial.regression = Amount.valueOf(0, SI.MILLIMETER);
		initial.chamberPressure = atmosphereicPressure;
		initial.chamberProduct = Amount.valueOf(0, SI.KILOGRAM);
		initial.thrust = Amount.valueOf(0, SI.NEWTON);
		
		data.put(Amount.valueOf(0, SI.SECOND), initial);
		
		for ( int i = 0; i < 5000; i++ ){

			Interval prev = data.get(data.lastKey());
			log.debug(prev);
			log.debug("Step " + i + " ==============================");
			Interval next = new Interval();
			
			Amount<Velocity> burnRate = motor.getFuel().burnRate(prev.chamberPressure);
			
			log.debug("Burn Rate: " + burnRate);
			
			Amount<Duration> dt = regStep.divide(burnRate).to(Duration.UNIT);
			next.dt = dt;
			
			data.put(data.lastKey().plus(dt), next);
			
			log.debug("Dt: " + dt);
			
			next.regression = prev.regression.plus(regStep);
			
			log.info("Regression: " + next.regression);
			
			next.time = prev.time.plus(dt);
			
			//log.debug("Vold: " + motor.getGrain().volume(prev.regression).to(SI.MILLIMETER.pow(3)));
			
			//log.debug("Vnew: " + motor.getGrain().volume(next.regression).to(SI.MILLIMETER.pow(3)));
			
			//TODO Amount<Volume> volumeBurnt = motor.getGrain().volume(prev.regression).minus(motor.getGrain().volume(next.regression));
			Amount<Volume> volumeBurnt = motor.getGrain().surfaceArea(prev.regression).times(regStep).to(Volume.UNIT);
			//log.info("Volume Burnt: " + volumeBurnt.to(SI.MILLIMETER.pow(3)));
			
			Amount<MassFlowRate> mGenRate = volumeBurnt.times(motor.getFuel().getIdealDensity().times(motor.getFuel().getDensityRatio())).divide(dt).to(MassFlowRate.UNIT);
			//log.debug("Mass Gen Rate: " + mGenRate);
			
			//Calculate specific gas constant
			Amount specificGasConstant = Constants.R.divide(motor.getFuel().getCombustionProduct().getEffectiveMolarWeight());
			//This unit conversion helps JScience to convert nozzle flow rate to
			//kg/s a little later on I verified the conversion by hand and
			//JScience checks it too.
			specificGasConstant = convertSpecificGasConstantUnits(specificGasConstant);
			
			//Calculate chamber temperature
			Amount<Temperature> chamberTemp = motor.getFuel().getCombustionProduct().getIdealCombustionTemperature().times(motor.getFuel().getCombustionEfficiency());
			
			Amount<MassFlowRate> mNozzle;
			{
				Amount<Pressure> pDiff = prev.chamberPressure.minus(atmosphereicPressure);
				//log.debug("Pdiff: " + pDiff);
				Amount<Area> aStar = motor.getNozzle().throatArea();
				double k = motor.getFuel().getCombustionProduct().getRatioOfSpecificHeats();
				double kSide = Math.sqrt(k) * Math.pow((2/(k+1)) , (((k+1)/2)/(k-1)));
				Amount sqrtPart = specificGasConstant.times(chamberTemp).sqrt();
				mNozzle = pDiff.times(aStar).times(kSide).divide(sqrtPart).to(MassFlowRate.UNIT);
				//log.debug("Mass Exit Rate: " + mNozzle.to(MassFlowRate.UNIT));		
			}
			
			Amount<MassFlowRate> massStorageRate = mGenRate.minus(mNozzle);
			
			//log.debug("Mass Storage Rate: " + massStorageRate);

			next.chamberProduct = prev.chamberProduct.plus(massStorageRate.times(dt));
			
			//Product can not go negative!
			if ( next.chamberProduct.isLessThan(Amount.valueOf(0, SI.KILOGRAM)) )
				next.chamberProduct = Amount.valueOf(0, SI.KILOGRAM);
			
			//log.debug("Chamber Product: " + next.chamberProduct);
			
			Amount<VolumetricDensity> combustionProductDensity = next.chamberProduct.divide(motor.getChamber().chamberVolume().minus(motor.getGrain().volume(next.regression))).to(VolumetricDensity.UNIT);
			
			log.debug("Product Density: " + combustionProductDensity);
			
			next.chamberPressure = combustionProductDensity.times(specificGasConstant).times(chamberTemp).plus(atmosphereicPressure).to(Pressure.UNIT);
			
			next.chamberPressure = Amount.valueOf(
					next.chamberPressure.doubleValue(SI.PASCAL),
					SI.PASCAL);
			
			next.thrust = motor.getNozzle().thrust(next.chamberPressure, atmosphereicPressure, atmosphereicPressure, motor.getFuel().getCombustionProduct().getRatioOfSpecificHeats2Phase());
			
			if ( i > 100 && next.chamberPressure.approximates(atmosphereicPressure)){
				log.info("Pressure at Patm on step " + i);
				break;
			}
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
		return data.get(time).chamberPressure;
	}
	
	public Amount<Force> thrust(Amount<Duration> time){
		return data.get(time).thrust;
	}
	
	public Amount<Dimensionless> kn(Amount<Length> regression){
		return motor.getGrain().surfaceArea(regression).divide(motor.getNozzle().throatArea()).to(Dimensionless.UNIT);
	}
	
}
