package com.billkuker.rocketry.motorsim.test;

import org.jscience.physics.amount.Amount;
import org.junit.Assert;

public class RocketTest {

	protected void assertApproximate(Amount a, Amount b){
		Assert.assertTrue("" + a + " !~ " + b , a.approximates(b));
	}
}
