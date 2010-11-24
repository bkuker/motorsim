package com.billkuker.rocketry.motorsim.io;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.BurnSummary;
import com.billkuker.rocketry.motorsim.GraphSimplifier;
import com.billkuker.rocketry.motorsim.RocketScience;

public class HTMLExporter {

	@SuppressWarnings("deprecation")
	private static String encode(String s) {
		return URLEncoder.encode(s).replace("%B2", "%C2%B2");
	}

	private static <X extends Quantity, Y extends Quantity> String toChart(
			final Unit<X> xUnit, final Unit<Y> yUnit, final Object source,
			final String method, final Iterator<Amount<X>> domain, String title)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		NumberFormat nf = new DecimalFormat("#.##");
		Method f = source.getClass().getMethod(method, Amount.class);

		double xMin = 0, xMax = 0;
		double yMin = 0, yMax = 0;

		StringBuffer xVals = new StringBuffer();
		StringBuffer yVals = new StringBuffer();

		while (domain.hasNext()) {
			Amount<X> aX = domain.next();
			double x = aX.doubleValue(xUnit);
			@SuppressWarnings("unchecked")
			double y = ((Amount<Y>) f.invoke(source, aX)).doubleValue(yUnit);
			xMin = x < xMin ? x : xMin;
			xMax = x > xMax ? x : xMax;
			yMin = y < yMin ? y : yMin;
			yMax = y > yMax ? y : yMax;

			xVals.append(nf.format(x));
			xVals.append(",");
			yVals.append(nf.format(y));
			yVals.append(",");
		}
		xVals.deleteCharAt(xVals.length() - 1);
		yVals.deleteCharAt(yVals.length() - 1);

		// Get the non-preferred Y Unit
		Unit<Y> yUnit2 = RocketScience.UnitPreference.SI
				.getPreferredUnit(yUnit);
		// if ( yUnit2.equals(yUnit) )
		yUnit2 = RocketScience.UnitPreference.NONSI.getPreferredUnit(yUnit);
		double y2Min, y2Max;
		y2Min = Amount.valueOf(yMin, yUnit).doubleValue(yUnit2);
		y2Max = Amount.valueOf(yMax, yUnit).doubleValue(yUnit2);

		// Get the non-preferred X Unit
		Unit<X> xUnit2 = RocketScience.UnitPreference.SI
				.getPreferredUnit(xUnit);
		// if ( xUnit2.equals(xUnit) )
		xUnit2 = RocketScience.UnitPreference.NONSI.getPreferredUnit(xUnit);
		double x2Min, x2Max;
		x2Min = Amount.valueOf(xMin, xUnit).doubleValue(xUnit2);
		x2Max = Amount.valueOf(xMax, xUnit).doubleValue(xUnit2);

		boolean x2 = !xUnit.equals(xUnit2);
		boolean y2 = x2 || !yUnit.equals(yUnit2);

		StringBuffer sb = new StringBuffer();
		sb.append("<img src='");
		sb.append("http://chart.apis.google.com/chart?chxt=x,x,y,y"
				+ (y2 ? ",r,r" : "") + (x2 ? ",t,t" : "")
				+ "&chs=640x240&cht=lxy&chco=3072F3");
		sb.append("&chds=" + nf.format(xMin) + "," + nf.format(xMax) + ","
				+ nf.format(yMin) + "," + nf.format(yMax));
		sb.append("&chxr=" + "0," + nf.format(xMin) + "," + nf.format(xMax)
				+ "|2," + nf.format(yMin) + "," + nf.format(yMax));
		if (y2)
			sb.append("|4," + nf.format(y2Min) + "," + nf.format(y2Max));
		if (x2)
			sb.append("|6," + nf.format(x2Min) + "," + nf.format(x2Max));
		sb.append("&chd=t:" + xVals.toString() + "|" + yVals.toString());
		sb.append("&chxl=1:|" + encode(xUnit.toString()) + "|3:|"
				+ encode(yUnit.toString()));
		if (y2)
			sb.append("|5:|" + encode(yUnit2.toString()));
		if (x2)
			sb.append("|7:|" + encode(xUnit2.toString()));
		sb.append("&chxp=1,50|3,50");
		if (y2)
			sb.append("|5,50");
		if (x2)
			sb.append("|7,50");
		sb.append("&chtt=" + title);
		sb.append("' width='640' height='240' alt='" + title + "' />");

		return sb.toString();
	}

	public static void export(Burn b, OutputStream os) throws Exception {
		PrintWriter out = new PrintWriter(os);

		BurnSummary bs = new BurnSummary(b);
		
		out.println("<!--Begin motor " + b.getMotor().getName() + " HTML export from MotorSim-->");
		out.print("<table class='motor'>");

		out.print("<tr>");
		out.print("<th colspan='6' class='title'>" + b.getMotor().getName() + "</th>");
		out.print("</tr>");

		out.print("<tr class='summary'>");
		out.print("<th>Rating:</th>");
		
		out.print("<td>" + bs.getRating() + "</td>");
		out.print("<th>Total Impulse:</th>");
	
		out.print("<td>"
				+ RocketScience.ammountToRoundedString(bs.totalImpulse())
				+ "</td>");
		out.print("<th>Specific Impulse:</th>");
		out.print("<td>"
				+ RocketScience.ammountToRoundedString(bs.specificImpulse())
				+ "</td>");
		out.print("</tr>");
		
		
		out.print("<tr class='summary'>");
		out.print("<th>Max Thrust:</th>");
		
		
		out.print("<td>"
				+ RocketScience.ammountToRoundedString(bs.maxThrust())
				+ "</td>");
		out.print("<th>Average Thrust:</th>");
	
		out.print("<td>"
				+ RocketScience.ammountToRoundedString(bs.averageThrust())
				+ "</td>");
		out.print("<th>Max Pressure:</th>");
		out.print("<td>"
				+ RocketScience.ammountToRoundedString(bs.maxPressure())
				+ "</td>");
		out.print("</tr>");

		out.print("<tr>");
		out.print("<td colspan='6' class='thrust'>");
		GraphSimplifier<Duration, Force> thrust = new GraphSimplifier<Duration, Force>(
				b, "thrust", b.getData().keySet().iterator());

		out.print(toChart(SI.SECOND, SI.NEWTON, thrust, "value", thrust
				.getDomain().iterator(), "Thrust"));
		out.print("</td>");
		/*
		out.print("<td colspan='3' class='pressure'>");
		GraphSimplifier<Duration, Pressure> pressure = new GraphSimplifier<Duration, Pressure>(
				b, "pressure", b.getData().keySet().iterator());

		out.print(toChart(SI.SECOND,
				javax.measure.unit.SI.MEGA(javax.measure.unit.SI.PASCAL),
				pressure, "value", pressure.getDomain().iterator(), "Pressure"));

		out.print("</td>");
		*/
		out.print("</tr>");
		/*
		out.print("<tr>");
		out.print("<th colspan='3'>.ENG File</th>");
		out.print("<th colspan='3'>MotorSim File</th>");
		out.print("</tr>");
		out.print("<tr>");
		out.print("<td colspan='3'>");
		out.print("<textarea>");
		out.flush();
		os.flush();
		ENGExporter.export(b, os);
		os.flush();
		out.print("</textarea>");
		out.print("</td>");
		out.print("<td colspan='3'>");
		out.print("<textarea>");
		out.print(MotorIO.writeMotor(b.getMotor()).replace("<", "&lt;").replace(">", "&gt;"));
		out.print("</textarea>");
		out.print("</td>");
		out.print("</tr>");
		*/
		out.print("</table>");

		out.println("\n<!--End motor " + b.getMotor().getName() + "-->");
		out.flush();
		out.close();
	}

}
