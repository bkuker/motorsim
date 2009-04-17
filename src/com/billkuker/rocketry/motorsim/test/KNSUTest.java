package com.billkuker.rocketry.motorsim.test;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import com.billkuker.rocketry.motorsim.fuel.KNSU;

public class KNSUTest extends RocketTest {

	@Test
	public void testBurnRate() {
		KNSU f = new KNSU();
		
		System.out.println(f.burnRate(Amount.valueOf(6.89, SI.MEGA(SI.PASCAL) )).to(NonSI.INCH.divide(SI.SECOND)));
	}

	@Test
	public void testEffectiveMolarWeight(){
		(new KNSU()).getCombustionProduct().getEffectiveMolarWeight();
	}
	
	@Test
	public void testIdealDensity(){
		System.out.println((new KNSU()).getIdealDensity());
		System.out.println((new KNSU()).getIdealDensity().isExact());
	}
}
