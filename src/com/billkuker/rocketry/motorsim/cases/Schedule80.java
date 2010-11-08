package com.billkuker.rocketry.motorsim.cases;

import java.awt.Shape;

import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Volume;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.RocketScience;

public class Schedule80 implements Chamber {
	public static enum Size {		
		
		//Dimensions: http://www.harvel.com/pipepvc-sch40-80-dim.asp
		//Pressure: http://www.engineeringtoolbox.com/pvc-cpvc-pipes-pressures-d_796.html
		//Also: http://www.jacobsrocketry.com/aer/misc_tables.htm
		//OneEighth(		"\u215B\u2033",		0.405,	0.249,	0.051,	2630),
		OneQuarter(		"\u00BC\u2033",		0.54,	0.282,	10,	7319),
		ThreeEights(	"\u215C\u2033",		0.675,	0.403,	13.8,	5400),
		OneHalf(		"\u00BD\u2033",		0.84,	0.546,	20.2,	4308),
		ThreeQuarters(	"\u00BE\u2033",		1.05,	0.722,	27.3,	3634),
		OneInch(		"1\u2033",			1.315,	0.936,	40.2,	3239),
		OneAndAQuarter(	"1\u00BC\u2033",	1.66,	1.255,	55.4,	2582),
		OneAndOneHalf(	"1\u00BD\u2033",	1.9,	1.476,	67.3,	2298),
		TwoInch(		"2\u2033",			2.375,	1.913,	93.2,	1932),
		TowAndOneHalf(	"2\u00BD\u2033",	2.875,	2.29,	141.9,	2044),
		ThreeInch(		"3\u2033",			3.5,	2.864,	190.3,	1777),
		FourInch(		"4\u2033",			4.5,	3.786,	278.2,	1509),
		SixInch(		"6\u2033",			6.625,	5.709,	531.3,	1284);

		private final Amount<Length> iD, oD;
		private final Amount<Pressure> burst;
		private final String name;
		public String toString(){
			return name + " Schedule 80";
		}
		Size(String name, double oD, double iD, double wt, double psi) {
			this.oD = Amount.valueOf(oD, NonSI.INCH);
			this.iD = Amount.valueOf(iD, NonSI.INCH);
			this.name  = name;
			this.burst = Amount.valueOf(psi, RocketScience.PSI);
		}
	}

	private CylindricalChamber chamber = new CylindricalChamber();
	private Size size;

	{
		setSize(Size.OneInch);
	}

	@Override
	public Amount<Volume> chamberVolume() {
		return chamber.chamberVolume();
	}

	@Override
	public Amount<Pressure> getBurstPressure() {
		return size.burst;
	}

	@Override
	public Shape chamberShape() {
		return chamber.chamberShape();
	}

	public void setLength(Amount<Length> length) {
		chamber.setLength(length);
	}

	public Amount<Length> getLength() {
		return chamber.getLength();
	}

	public void setSize(Size size) {
		this.size = size;
		chamber.setID(size.iD);
		chamber.setOD(size.oD);
	}

	public Size getSize() {
		return size;
	}
	
	public  Amount<Length> getID() {
		return chamber.getID();
	}
	
	public  Amount<Length> getOD() {
		return chamber.getOD();
	}
}
