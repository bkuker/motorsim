package com.billkuker.rocketry.motorsim;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class CylindricalChamber implements Chamber {
	
	private Amount<Length> length = Amount.valueOf(200, SI.MILLIMETER);
	private Amount<Length> oD = Amount.valueOf(31, SI.MILLIMETER);
	private Amount<Length> iD = Amount.valueOf(30, SI.MILLIMETER);

	public Amount<Pressure> getBurstPressure() {
		return null;
	}

	public Amount<Volume> chamberVolume() {
		return iD.divide(2).pow(2).times(Math.PI).times(length).to(SI.CUBIC_METRE);
	}

	public Amount<Length> getLength() {
		return length;
	}

	public void setLength(Amount<Length> length) {
		this.length = length;
	}

	public Amount<Length> getID() {
		return iD;
	}

	public void setID(Amount<Length> id) {
		iD = id;
	}
	
	public Amount<Length> getOD() {
		return oD;
	}

	public void setOD(Amount<Length> oD) {
		this.oD = oD;
	}

	@Override
	public Shape chamberShape() {
		double ir = iD.doubleValue(SI.MILLIMETER) / 2;
		double or = oD.doubleValue(SI.MILLIMETER) / 2;
		double lenmm = length.doubleValue(SI.MILLIMETER);
		double thick = or-ir;

		Rectangle2D.Double l,r,t;
		l = new Rectangle2D.Double(-or,0,thick,lenmm);
		r = new Rectangle2D.Double(ir, 0, thick, lenmm);
		t = new Rectangle2D.Double(-or,0,or*2,thick);
		Area a = new Area(l);
		a.add(new Area(r));
		a.add(new Area(t));
		return a;
	}

}
