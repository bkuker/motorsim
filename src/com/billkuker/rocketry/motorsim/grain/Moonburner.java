package com.billkuker.rocketry.motorsim.grain;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyVetoException;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.ExtrudedShapeGrain;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;

public class Moonburner extends ExtrudedShapeGrain {

	private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);
	private Amount<Length> iD = Amount.valueOf(10, SI.MILLIMETER);
	private Amount<Length> coreOffset = Amount.valueOf(0, SI.MILLIMETER);
	
	public Moonburner(){
		try {
			setLength(Amount.valueOf(70, SI.MILLIMETER));
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		generateGeometry();
	}

	public Amount<Length> getOD() {
		return oD;
	}

	public void setOD(Amount<Length> od) throws PropertyVetoException {
		if (od.equals(this.oD))
			return;
		this.oD = od;
		generateGeometry();
	}

	public Amount<Length> getID() {
		return iD;
	}

	public void setID(Amount<Length> id) throws PropertyVetoException {
		if (id.equals(this.iD))
			return;
		iD = id;
		generateGeometry();
	}

	public Amount<Length> getCoreOffset() {
		return coreOffset;
	}

	public void setCoreOffset(Amount<Length> coreOffset)
			throws PropertyVetoException {
		if (coreOffset.equals(this.coreOffset))
			return;
		this.coreOffset = coreOffset;
		generateGeometry();
	}

	private void generateGeometry() {
		double odmm = oD.doubleValue(SI.MILLIMETER);
		double idmm = iD.doubleValue(SI.MILLIMETER);
		double offmm = coreOffset.doubleValue(SI.MILLIMETER);
		xsection = new BurningShape();
		Shape outside = new Ellipse2D.Double(0, 0, odmm, odmm);
		xsection.add(outside);
		xsection.inhibit(outside);

		xsection.subtract(new Ellipse2D.Double(odmm/2 - idmm/2 + offmm, odmm/2 - idmm/2 + offmm, idmm, idmm));
		webThickness = null;
	}
	
	public static void main(String args[]) throws Exception {
		Moonburner e = new Moonburner();
		new Editor(e).showAsWindow();
		new GrainPanel(e).showAsWindow();
	}

}
