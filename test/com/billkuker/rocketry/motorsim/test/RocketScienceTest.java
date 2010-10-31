package com.billkuker.rocketry.motorsim.test;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.RadioactiveActivity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import junit.framework.Assert;

import org.junit.Test;

import com.billkuker.rocketry.motorsim.RocketScience;

public class RocketScienceTest {

	@Test
	public void getPreferredUnitByQuantity() throws Exception {
		Unit<Length> l = RocketScience.UnitPreference.SI.getPreferredUnit(Length.class);
		Assert.assertTrue(l.isCompatible(SI.MILLIMETER));
		
		Unit<Duration> t = RocketScience.UnitPreference.SI.getPreferredUnit(Duration.class);
		Assert.assertTrue(t.isCompatible(SI.SECOND));
		
		//Just make sure it'll return for any quantity
		Unit<RadioactiveActivity> r = RocketScience.UnitPreference.SI.getPreferredUnit(RadioactiveActivity.class);
		Assert.assertTrue(r.isCompatible(NonSI.RUTHERFORD));
	}
	
	@Test
	public void getPreferredUnitByUnit() throws Exception {
		Unit<Length> l = RocketScience.UnitPreference.SI.getPreferredUnit(NonSI.INCH);
		Assert.assertTrue(l.isCompatible(SI.MILLIMETER));
		
		Unit<Duration> t = RocketScience.UnitPreference.SI.getPreferredUnit(NonSI.DAY);
		Assert.assertTrue(t.isCompatible(SI.SECOND));
		
		//Just make sure it'll return for any quantity
		Unit<RadioactiveActivity> r = RocketScience.UnitPreference.SI.getPreferredUnit(NonSI.RUTHERFORD);
		Assert.assertTrue(r.isCompatible(NonSI.RUTHERFORD));
	}
}
