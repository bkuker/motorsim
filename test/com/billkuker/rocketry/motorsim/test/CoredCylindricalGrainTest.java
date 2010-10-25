package com.billkuker.rocketry.motorsim.test;

import java.beans.PropertyVetoException;

import javax.measure.quantity.Area;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;

public class CoredCylindricalGrainTest extends RocketTest {

	@Test
	public void testSurfaceArea() throws PropertyVetoException {
	
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		
		g.setLength(Amount.valueOf(100, SI.MILLIMETER));
		g.setOD(Amount.valueOf(30, SI.MILLIMETER));
		g.setID(Amount.valueOf(10, SI.MILLIMETER));
		
		g.setForeEndInhibited(false);
		g.setAftEndInhibited(false);
		g.setOuterSurfaceInhibited(true);
		g.setInnerSurfaceInhibited(false);
		
		Amount<Area> a = g.surfaceArea(Amount.valueOf(6, SI.MILLIMETER));
		assertApproximate(a, Amount.valueOf(6736, sqMM), Amount.valueOf(1, sqMM) );
	}
	

	@Test
	public void testWebThickness() throws PropertyVetoException {
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		
		//thin and long
		g.setLength(Amount.valueOf(100, SI.MILLIMETER));
		g.setOD(Amount.valueOf(30, SI.MILLIMETER));
		g.setID(Amount.valueOf(10, SI.MILLIMETER));
		
		assertApproximate(g.webThickness(), Amount.valueOf("10mm"));
		
		//thick and short
		g.setLength(Amount.valueOf(100, SI.MILLIMETER));
		g.setOD(Amount.valueOf(300, SI.MILLIMETER));
		g.setID(Amount.valueOf(100, SI.MILLIMETER));
		
		assertApproximate(g.webThickness(), Amount.valueOf("50mm"));
	}

	@Test
	public void testVolume() throws PropertyVetoException {
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		
		//thin and long
		g.setLength(Amount.valueOf(100, SI.MILLIMETER));
		g.setOD(Amount.valueOf(30, SI.MILLIMETER));
		g.setID(Amount.valueOf(10, SI.MILLIMETER));
		
		g.setForeEndInhibited(false);
		g.setAftEndInhibited(false);
		g.setOuterSurfaceInhibited(true);
		g.setInnerSurfaceInhibited(false);
		
		Amount<Volume> v = g.volume(Amount.valueOf(3, SI.MILLIMETER));
		assertApproximate(v, Amount.valueOf(47545, cubeMM), Amount.valueOf(1, cubeMM));
		
		v = g.volume(Amount.valueOf(5, SI.MILLIMETER));
		assertApproximate(v, Amount.valueOf(35343, cubeMM), Amount.valueOf(1, cubeMM));
		
		v = g.volume(Amount.valueOf(9, SI.MILLIMETER));
		assertApproximate(v, Amount.valueOf(7471, cubeMM), Amount.valueOf(1, cubeMM));
	}

	/*
	@Test(expected=MotorPart.Validating.ValidationException.class)
	public void testCheckValidity() throws MotorPart.Validating.ValidationException, PropertyVetoException{
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		
		//thin and long
		g.setLength(Amount.valueOf(100, SI.MILLIMETER));
		g.setOD(Amount.valueOf(10, SI.MILLIMETER));
		g.setID(Amount.valueOf(30, SI.MILLIMETER));
		
		g.checkValidity();
	}*/

}
