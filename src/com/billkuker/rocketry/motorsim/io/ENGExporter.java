package com.billkuker.rocketry.motorsim.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.GraphSimplifier;
import com.billkuker.rocketry.motorsim.ICylindricalChamber;

public class ENGExporter {

	public static void export(Iterable<Burn> bb, File f) throws IOException {
		export(bb, new FileOutputStream(f));
	}

	public static void export(Iterable<Burn> bb, OutputStream os) throws IOException {
		for (Burn b : bb) {
			export(b, os);
		}
	}

	public static void export(Burn b, OutputStream os) throws IOException {

		ICylindricalChamber cha = (ICylindricalChamber) b.getMotor().getChamber();

		NumberFormat nf = new DecimalFormat("0.####", new DecimalFormatSymbols(Locale.US));

		StringBuffer out = new StringBuffer();

		out.append(";Output from Motorsim, motorsim@billkuker.com\n");
		out.append(";You must fill in Delays and Total Weight\n");
		out.append(";Name Diameter Length Delays ProWt Wt Manufacturer\n");
		out.append(b.getMotor().getName().replace(" ", "-") + " ");

		double dia = cha.getOD().doubleValue(SI.MILLIMETER);
		double len = cha.getLength().doubleValue(SI.MILLIMETER);

		Amount<Mass> prop = b.getMotor().getGrain().volume(
				Amount.valueOf(0, SI.MILLIMETER)).times(
				b.getMotor().getFuel().getIdealDensity().times(
						b.getMotor().getFuel().getDensityRatio())).to(
				SI.KILOGRAM);
		double wt = prop.doubleValue(SI.KILOGRAM);
		
		double delay = b.getMotor().getEjectionDelay().doubleValue(SI.SECOND);
		String delayString = Integer.toString((int)delay);

		out.append(nf.format(dia) + " " + nf.format(len) + " " + delayString + "-0-0 "
				+ nf.format(wt) + " " + nf.format(wt + 0.1) + " MF\n");

		GraphSimplifier<Duration, Force> gs = null;
		try {
			gs = new GraphSimplifier<Duration, Force>(b, "thrust", b.getData()
					.keySet().iterator());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		int cnt = 0;
		double lastTime = 0;
		for (Amount<Duration> t : gs.getDomain()) {
			cnt++;
			double thrust = gs.value(t).doubleValue(SI.NEWTON);
			if (cnt < 10 && thrust == 0.0) {
				continue; // This is a hack to ignore 0 thrust early in burn
			}
			out.append("   ");
			out.append(nf.format(lastTime = t.doubleValue(SI.SECOND)));
			out.append(" ");
			out.append(nf.format(thrust));
			out.append("\n");
		}
		
		out.append("   ");
		out.append(nf.format(lastTime + 0.01));
		out.append(" ");
		out.append(nf.format(0.0));
		out.append("\n");
		out.append(";\n\n");

		os.write(out.toString().getBytes());
	}
}
