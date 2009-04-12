package com.billkuker.rocketry.motorsim;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class ConvergentDivergentNozzle extends MotorPart implements Nozzle {

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
		if ( exitDiameter != null && throatDiameter.isGreaterThan(exitDiameter))
			throw new IllegalArgumentException("Throat > Exit");
		Amount<Length> old = this.throatDiameter;
		this.throatDiameter = throatDiameter;
		firePropertyChange("throatDiameter", old, throatDiameter);
	}
	

	public Amount<Length> getExitDiameter() {
		return exitDiameter;
	}


	public void setExitDiameter(Amount<Length> exitDiameter) {
		if ( throatDiameter != null && exitDiameter.isLessThan(throatDiameter))
			throw new IllegalArgumentException("Throat > Exit");
		Amount<Length> old = this.exitDiameter;
		this.exitDiameter = exitDiameter;
		firePropertyChange("exitDiameter", old, exitDiameter);
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
		double old = this.efficiency;
		this.efficiency = efficiency;
		firePropertyChange("efficiency", old, efficiency);
	}

	public Shape nozzleShape(Amount<Length> chamberDiameter){
		GeneralPath s = new GeneralPath();
		double throatR = throatDiameter.divide(2).doubleValue(SI.MILLIMETER);
		double exitR = exitDiameter.divide(2).doubleValue(SI.MILLIMETER);
		double cR;
		if (chamberDiameter == null)
			cR = exitR;
		else
			cR = chamberDiameter.divide(2).doubleValue(SI.MILLIMETER);
		
		double diff = exitR-throatR;
		double cDiff = cR-throatR;
		
		s.append(new Line2D.Double(0,0,diff,diff*3), false);
		s.append(new Line2D.Double(0,0,cDiff,-cDiff), true);
		
		s.transform(AffineTransform.getScaleInstance(-1, 1));
		s.transform(AffineTransform.getTranslateInstance(-throatR, 0));
		
		s.append(new Line2D.Double(0,0,diff,diff*3), false);
		s.append(new Line2D.Double(0,0,cDiff,-cDiff), true);
		
		//a.add(new java.awt.geom.Area( new Ellipse2D.Double(0,0,5,5)));
		
		return s;
	}
}
