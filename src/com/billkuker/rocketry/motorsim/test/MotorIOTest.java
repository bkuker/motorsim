package com.billkuker.rocketry.motorsim.test;

import java.io.File;

import org.junit.Test;

import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import com.billkuker.rocketry.motorsim.motors.kuker.PVC9;


public class MotorIOTest {

	@Test
	public void testReadWrite() throws Exception{
		Motor m = new PVC9();
		File f = new File("MotorIOTest.xml");
		
		MotorIO.writeMotor(m, f);
		
		Motor r = MotorIO.readMotor(f);
		
	
	}
}
