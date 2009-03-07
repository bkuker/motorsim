package com.billkuker.rocketry.motorsim.test;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.validation.ValidationException;

public class CoredCylindricalGrainTest extends RocketTest {

	@Test
	public void testSurfaceArea() {
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		
		g.setLength(Amount.valueOf(70, SI.MILLIMETER));
		
		g.setOD(Amount.valueOf(23.5, SI.MILLIMETER));
		
		g.setID(Amount.valueOf(7.9375, SI.MILLIMETER));
		
		for ( int mm = 0; mm < 15; mm++ ){
			Amount<Length> r = Amount.valueOf(mm, SI.MILLIMETER);
			System.out.println( r + ", "  + g.surfaceArea(r).to(SI.MILLIMETER.pow(2)) + ", " + g.volume(r) );
		}
	}
	

	@Test
	public void testWebThickness() {
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
	public void testVolume() {
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		
		//thin and long
		g.setLength(Amount.valueOf(100, SI.MILLIMETER));
		g.setOD(Amount.valueOf(30, SI.MILLIMETER));
		g.setID(Amount.valueOf(10, SI.MILLIMETER));
		
		System.out.println(g.volume(Amount.valueOf(0, SI.MILLIMETER)));
	}

	@Test(expected=ValidationException.class)
	public void testCheckValidity() throws ValidationException{
		CoredCylindricalGrain g = new CoredCylindricalGrain();
		
		//thin and long
		g.setLength(Amount.valueOf(100, SI.MILLIMETER));
		g.setOD(Amount.valueOf(10, SI.MILLIMETER));
		g.setID(Amount.valueOf(30, SI.MILLIMETER));
		
		g.checkValidity();
	}

}
