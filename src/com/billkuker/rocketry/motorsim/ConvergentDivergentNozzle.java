package com.billkuker.rocketry.motorsim;

import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;

import org.jscience.physics.amount.Amount;

public class ConvergentDivergentNozzle implements Nozzle {

	private Amount<Length> throatDiameter;
	
	private Amount<Length> exitDiameter;
	
	private double efficiency = 1.0;
	
	
	@Override
	public Amount<Area> throatArea() {
		return throatDiameter.divide(2).pow(2).times(Math.PI).to(Area.UNIT);
	}
	
	public Amount<Area> exitArea() {
		return exitDiameter.divide(2).pow(2).times(Math.PI).to(Area.UNIT);
	}


	public Amount<Length> getThroatDiameter() {
		return throatDiameter;
	}


	public void setThroatDiameter(Amount<Length> throatDiameter) {
		this.throatDiameter = throatDiameter;
	}
	

	public Amount<Length> getExitDiameter() {
		return exitDiameter;
	}


	public void setExitDiameter(Amount<Length> exitDiameter) {
		this.exitDiameter = exitDiameter;
	}
	
	@Override
	public Amount<Force> thrust(Amount<Pressure> Po, Amount<Pressure> Pe, Amount<Pressure> Patm, final double k ){
		double cF = thrustCoefficient(Po, Pe, Patm, k);
		return Po.times(throatArea()).times(cF).to(Force.UNIT);
	}

	public double thrustCoefficient(Amount<Pressure> Po, Amount<Pressure> Pe, Amount<Pressure> Patm, final double k ){
		double pRatio = Pe.divide(Po).to(Dimensionless.UNIT).doubleValue(Dimensionless.UNIT);

		double a = (2*k*k)/(k-1);
		
		double b = Math.pow(2/(k+1), (k+1)/(k-1) );
		
		double c = 1.0 - Math.pow(pRatio, (k-1)/k);
		
		Amount<Pressure> pDiff = Pe.minus(Patm);
		
		double d = pDiff.times(exitArea()).divide(Po.times(throatArea())).to(Dimensionless.UNIT).doubleValue(Dimensionless.UNIT);
		
		double Cf = efficiency * Math.sqrt(a*b*c)+d;
		
		return Cf;
	}

	public double getEfficiency() {
		return efficiency;
	}

	public void setEfficiency(double efficiency) {
		this.efficiency = efficiency;
	}


}
