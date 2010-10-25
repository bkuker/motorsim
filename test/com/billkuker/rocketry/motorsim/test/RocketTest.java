package com.billkuker.rocketry.motorsim.test;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.junit.Assert;

public class RocketTest {
	
	protected static final Unit<javax.measure.quantity.Area> sqMM = SI.MILLIMETER.pow(2).asType(javax.measure.quantity.Area.class);
	
	
	protected static final Unit<javax.measure.quantity.Volume> cubeMM = SI.MILLIMETER.pow(3).asType(javax.measure.quantity.Volume.class);

	@SuppressWarnings("unchecked")
	protected void assertApproximate(Amount a, Amount b){
		Assert.assertTrue("" + a.to(b.getUnit()) + " !~ " + b , a.approximates(b));
	}
	
	@SuppressWarnings("unchecked")
	protected void assertApproximate(Amount a, Amount b, Amount diff){
		Assert.assertTrue("" + a.to(b.getUnit()) + " !~~ " + b ,  a.minus(b).abs().isLessThan(diff));
	}
}
