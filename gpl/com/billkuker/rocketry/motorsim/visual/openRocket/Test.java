package com.billkuker.rocketry.motorsim.visual.openRocket;

import java.io.File;

import javax.measure.unit.SI;
import javax.swing.JFrame;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.motors.kuker.PVC9;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		UnitPreference.setUnitPreference(UnitPreference.NONSI);

		JFrame frame = new JFrame();
		frame.setSize(1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		RocketSimTable st = new RocketSimTable();


		com.billkuker.rocketry.motorsim.Motor m = new PVC9();
		m.setEjectionDelay(Amount.valueOf(1, SI.SECOND));
		Burn b = new Burn(m);
		b.burn();
		st.replace(null, b);

		st.openRocket(new File("simple.ork"));
		
		frame.setContentPane(st);
		frame.setVisible(true);
		


	}

}
