package com.billkuker.rocketry.motorsim.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.measure.quantity.Pressure;
import javax.measure.unit.SI;

import junit.framework.Assert;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.fuel.editable.EditablePiecewiseLinearFuel;
import com.billkuker.rocketry.motorsim.fuel.editable.EditablePiecewiseSaintRobertFuel;
import com.billkuker.rocketry.motorsim.io.MotorIO;

public class FuelIOTest {
	final Amount<Pressure> testPressure = Amount.valueOf(100, RocketScience.PSI);

	@Test
	public void saveEPL() throws Exception{
		EditablePiecewiseLinearFuel f = new EditablePiecewiseLinearFuel();
		f.setName("Test1");
		f.add(Amount.valueOf(100, RocketScience.PSI), Amount.valueOf(1, SI.METERS_PER_SECOND));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MotorIO.writeFuel(f, out);
		String s = new String(out.toByteArray());
		System.out.println(s);
		
		Fuel f2 = MotorIO.readFuel(new ByteArrayInputStream(out.toByteArray()));
		Assert.assertTrue(f2 instanceof EditablePiecewiseLinearFuel);
		Assert.assertEquals(f2.getName(), "Test1");
		Assert.assertEquals(f.burnRate(testPressure), f2.burnRate(testPressure));
	}
	
	@Test
	public void saveEPSR() throws Exception{
		EditablePiecewiseSaintRobertFuel f = new EditablePiecewiseSaintRobertFuel();
		f.setName("Test2");
		f.add(Amount.valueOf(100, RocketScience.PSI), 1,1);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MotorIO.writeFuel(f, out);
		String s = new String(out.toByteArray());
		System.out.println(s);
		
		Fuel f2 = MotorIO.readFuel(new ByteArrayInputStream(out.toByteArray()));
		Assert.assertTrue(f2 instanceof EditablePiecewiseSaintRobertFuel);
		Assert.assertEquals(f2.getName(), "Test2");
		Assert.assertEquals(f.burnRate(testPressure), f2.burnRate(testPressure));
	}
}
