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

import com.billkuker.rocketry.motorsim.fuel.KNSB;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.CompoundGrain;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.visual.BurnPanel;

public class Burn {
	
	private static Logger log = Logger.getLogger(Burn.class);
	protected final Motor motor;
	
	private static final Amount<Pressure> atmosphereicPressure = Amount.valueOf(101000, SI.PASCAL);
	
	
	private static double combustionEfficency = 0.97;
	
	private static double densityRatio = 0.96;
	
	public class Interval{
		Amount<Duration> time;
		public Amount<Length> regression;
		public Amount<Pressure> chamberPressure;
		Amount<Mass> chamberProduct;
		Amount<Force> thrust;

		public String toString(){
			return time + " " + regression + " " + chamberPressure + " " + chamberProduct;
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
			
			data.put(data.lastKey().plus(dt), next);
			
			log.debug("Dt: " + dt);
			
			next.regression = prev.regression.plus(regStep);
			
			log.info("Regression: " + next.regression);
			
			next.time = prev.time.plus(dt);
			
			log.debug("Vold: " + motor.getGrain().volume(prev.regression).to(SI.MILLIMETER.pow(3)));
			
			log.debug("Vnew: " + motor.getGrain().volume(next.regression).to(SI.MILLIMETER.pow(3)));
			
			//TODO Amount<Volume> volumeBurnt = motor.getGrain().volume(prev.regression).minus(motor.getGrain().volume(next.regression));
			Amount<Volume> volumeBurnt = motor.getGrain().surfaceArea(prev.regression).times(regStep).to(Volume.UNIT);
			
			log.info("Volume Burnt: " + volumeBurnt.to(SI.MILLIMETER.pow(3)));
			
			Amount<MassFlowRate> mGenRate = volumeBurnt.times(motor.getFuel().idealDensity().times(densityRatio)).divide(dt).to(MassFlowRate.UNIT);
			
			log.debug("Mass Gen Rate: " + mGenRate);
			
			Amount specificGasConstant = Constants.R.divide(motor.getFuel().getCombustionProduct().effectiveMolarWeight());
			Amount<Temperature> chamberTemp = motor.getFuel().getCombustionProduct().idealCombustionTemperature().times(combustionEfficency);
			
			Amount<MassFlowRate> mNozzle;
			{
				Amount<Pressure> pDiff = prev.chamberPressure.minus(atmosphereicPressure);
				
				//pDiff = Amount.valueOf(.7342, MPA).minus(atmosphereicPressure);
				
				log.debug("Pdiff: " + pDiff);
				
				Amount<Area> aStar = motor.getNozzle().throatArea();
				
				double k = motor.getFuel().getCombustionProduct().ratioOfSpecificHeats();
				
				log.debug("K: " + k);
				
				double kSide = Math.sqrt(k) * Math.pow((2/(k+1)) , (((k+1)/2)/(k-1))); //Math.pow(2/k+1, (k+1)/(2*(k-1)));
				
				log.debug("K-Part: (good)" + kSide);
				
				
				
				//This unit conversion helps JScience to convert nozzle flow rate to
				//kg/s a little later on I verified the conversion by hand and
				//JScience checks it too.
				specificGasConstant = specificGasConstant.to(
						SI.METER.pow(2).divide(SI.SECOND.pow(2).times(SI.KELVIN)));
				
				log.debug("Specific Gas Constant: (good)" + specificGasConstant);
				
				Amount sqrtPart = specificGasConstant.times(chamberTemp).sqrt();

				//Unit x = SI.JOULE.divide(SI.KILOGRAM).root(2);
				
				//sqrtPart = sqrtPart.times(Amount.valueOf(1, x));
				
				log.debug("Square Root Part: " + sqrtPart);
				
				mNozzle = pDiff.times(aStar).times(kSide).divide(sqrtPart).to(MassFlowRate.UNIT);
				
				log.debug("Nozzle Flow: " + mNozzle);
				
				log.debug("Nozzle Flow: " + mNozzle.to(MassFlowRate.UNIT));
				
				
				
				
			}
			
			Amount<MassFlowRate> massStorageRate = mGenRate.minus(mNozzle);
			
			log.debug("Chamber Product rate: " + massStorageRate);
			
			next.chamberProduct = prev.chamberProduct.plus(massStorageRate.times(dt));
			
			log.debug("Chamber Product: " + next.chamberProduct);
			
			Amount<VolumetricDensity> combustionProductDensity = next.chamberProduct.divide(motor.getChamber().chamberVolume().minus(motor.getGrain().volume(next.regression))).to(VolumetricDensity.UNIT);
			
			log.debug("Product Density: " + combustionProductDensity);
			
			next.chamberPressure = combustionProductDensity.times(specificGasConstant).times(chamberTemp).plus(atmosphereicPressure).to(Pressure.UNIT);
			
			next.chamberPressure = Amount.valueOf(
					next.chamberPressure.doubleValue(SI.PASCAL),
					SI.PASCAL);
			
			next.thrust = motor.getNozzle().thrust(next.chamberPressure, atmosphereicPressure, atmosphereicPressure, motor.getFuel().getCombustionProduct().ratioOfSpecificHeats2Phase());
			
			if ( next.chamberPressure.approximates(atmosphereicPressure)){
				log.info("Pressure at Patm on step " + i);
				break;
			}
		}
				
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
	
	public static void main( String args[]) throws Exception{
		Motor m = new Motor();
		m.setFuel(new KNSB());
		
		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(300, SI.MILLIMETER));
		c.setID(Amount.valueOf(30, SI.MILLIMETER));
		m.setChamber(c);
		
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		g.setLength(Amount.valueOf(35, SI.MILLIMETER));
		g.setOD(Amount.valueOf(30, SI.MILLIMETER));
		g.setID(Amount.valueOf(10, SI.MILLIMETER));
		m.setGrain(g);
		
		CoredCylindricalGrain g1 = new CoredCylindricalGrain();
		g1.setLength(Amount.valueOf(70, SI.MILLIMETER));
		g1.setOD(Amount.valueOf(30, SI.MILLIMETER));
		g1.setID(Amount.valueOf(18, SI.MILLIMETER));
		g1.inhibit(false, true, true);
		
		CoredCylindricalGrain g2 = new CoredCylindricalGrain();
		g2.setLength(Amount.valueOf(70, SI.MILLIMETER));
		g2.setOD(Amount.valueOf(12, SI.MILLIMETER));
		g2.setID(Amount.valueOf(0, SI.MILLIMETER));
		g2.inhibit(true, false, true);
		
		CompoundGrain cg = new CompoundGrain();
		cg.add(g1);
		cg.add(g2);
		
		//m.setGrain( new MultiGrain(cg, 2) );
		
		//g.setAftEndInhibited(true);
		//g.setForeEndInhibited(true);
		m.setGrain(new MultiGrain(g,3));
		
		//m.setGrain(new ExtrudedGrain());
		
		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(5.500, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(20.87, SI.MILLIMETER));
		n.setEfficiency(.87);
		m.setNozzle(n);
		
		Burn b = new Burn(m);
		
		b.burn();
		
		new BurnPanel(b).show();
		/*
		Chart<Duration, Pressure> r = new Chart<Duration, Pressure>(
				SI.SECOND,
				SI.MEGA(SI.PASCAL),
				b,
				"pressure");
		r.setDomain(b.data.keySet());
		r.show();
		
		Chart<Duration, Force> t = new Chart<Duration, Force>(
				SI.SECOND,
				SI.NEWTON,
				b,
				"thrust");
		t.setDomain(b.data.keySet());
		t.show();
		
		new GrainPanel( m.getGrain() ).show();*/
		
	}
}
