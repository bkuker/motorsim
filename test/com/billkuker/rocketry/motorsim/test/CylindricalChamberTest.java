package com.billkuker.rocketry.motorsim.test;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Test;

import com.billkuker.rocketry.motorsim.CylindricalChamber;

public class CylindricalChamberTest extends AbstractRocketTest {

	@Test
	public void testBurstPressure() {
		Assert.assertNull( new CylindricalChamber().getBurstPressure() );
	}

	@Test
	public void testChamberVolume() {
		CylindricalChamber c = new CylindricalChamber();
		
		c.setLength(Amount.valueOf(100, SI.MILLIMETER));
		c.setID(Amount.valueOf(20, SI.MILLIMETER));
		
		assertApproximate(c.chamberVolume(), Amount.valueOf(10000*Math.PI, SI.MILLIMETER.pow(3)));
	}

}
