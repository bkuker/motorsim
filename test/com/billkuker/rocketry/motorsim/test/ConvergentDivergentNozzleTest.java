package com.billkuker.rocketry.motorsim.test;

import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Pressure;
import javax.measure.unit.SI;

import junit.framework.Assert;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.fuel.KNSU;

public class ConvergentDivergentNozzleTest extends AbstractRocketTest {
	
	ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
	
	{
		n.setThroatDiameter(Amount.valueOf(6.6, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(20.87, SI.MILLIMETER));
		n.setEfficiency(0.85);
	}

	@Test
	public void testThroatArea() {
		Amount<Area> a = n.throatArea().to(SI.MILLIMETER.pow(2).asType(Area.class));
		assertApproximate(Amount.valueOf(34.2, .1, SI.MILLIMETER.pow(2)), a);
	}

	@Test
	public void testExitArea() {
		Amount<Area> a = n.exitArea();
		assertApproximate(Amount.valueOf(342, .1, SI.MILLIMETER.pow(2)), a);
	}

	@Test
	public void testThrust() {
		Amount<Pressure> Patm = Amount.valueOf(101000, SI.PASCAL);
		Amount<Pressure> Po = Amount.valueOf(2046491, SI.PASCAL);
		Amount<Pressure> Pe = Patm;

		KNSU f = new KNSU();
		
		Amount<Force> t = n.thrust(Po, Pe, Patm, f.getCombustionProduct().getRatioOfSpecificHeats2Phase());
		
		Amount<Force> expected = Amount.valueOf(87.2, .1, SI.NEWTON);
		
		assertApproximate(t, expected);
	}

	@Test
	public void testThrustCoefficient() {
		Amount<Pressure> Patm = Amount.valueOf(101000, SI.PASCAL);
		Amount<Pressure> Po = Amount.valueOf(2046491, SI.PASCAL);
		Amount<Pressure> Pe = Patm;

		KNSU f = new KNSU();
		
		double cF = n.thrustCoefficient(Po, Pe, Patm, f.getCombustionProduct().getRatioOfSpecificHeats2Phase());
		
		
		Assert.assertEquals(cF, 1.2454812344324655);
	}

}
