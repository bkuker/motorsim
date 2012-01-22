package com.billkuker.rocketry.motorsim.visual.openRocket;

import java.io.File;

import javax.swing.JFrame;

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

		RocketSimTable st = new RocketSimTable();
		st.openRocket(new File("simple.ork"));

		com.billkuker.rocketry.motorsim.Motor m = new PVC9();
		Burn b = new Burn(m);
		b.burn();
		st.replace(null, b);

		frame.setContentPane(st);
		frame.setVisible(true);

	}

}
