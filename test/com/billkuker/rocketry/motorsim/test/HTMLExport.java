package com.billkuker.rocketry.motorsim.test;

import java.io.File;
import java.io.FileOutputStream;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.io.HTMLExporter;
import com.billkuker.rocketry.motorsim.motors.kuker.PVC9;

public class HTMLExport {
	public static void main(String args[]) throws Exception {

		Motor m = new PVC9();
		Burn b = new Burn(m);
		b.burn();
		
		File f = new File("test.html");
		HTMLExporter.export(b, System.out);
		HTMLExporter.export(b, new FileOutputStream(f));

	}
}
