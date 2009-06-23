package com.billkuker.rocketry.motorsim.grain;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.ExtrudedShapeGrain;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;

public class Finocyl extends ExtrudedShapeGrain {
	private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);
	private Amount<Length> iD = Amount.valueOf(10, SI.MILLIMETER);
	private Amount<Length> finWidth = Amount.valueOf(2, SI.MILLIMETER);
	private Amount<Length> finDiameter = Amount.valueOf(20, SI.MILLIMETER);
	private int finCount = 5;
	
	public Finocyl(){
		try {
			setLength(Amount.valueOf(70, SI.MILLIMETER));
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		generateGeometry();
	}
	
	private void generateGeometry(){
		double odmm = oD.doubleValue(SI.MILLIMETER);
		double idmm = iD.doubleValue(SI.MILLIMETER);
		double fwmm = finWidth.doubleValue(SI.MILLIMETER);
		double fdmm = finDiameter.doubleValue(SI.MILLIMETER);
		
		xsection = new BurningShape();
		Shape outside = new Ellipse2D.Double(-odmm/2, -odmm/2, odmm, odmm);
		xsection.add(outside);
		xsection.inhibit(outside);
		xsection.subtract(new Ellipse2D.Double(-idmm/2, -idmm/2, idmm, idmm));
		webThickness = null;
		
		for ( int i = 0; i < finCount; i++ ){
			Shape fin = new Rectangle2D.Double(-fwmm/2,0,fwmm,fdmm/2);
			xsection.subtract(fin, AffineTransform.getRotateInstance(i*(2.0*Math.PI/finCount)));
		}
	}
	
	public Amount<Length> getOD() {
		return oD;
	}

	public void setOD(Amount<Length> od) throws PropertyVetoException {
		if (od.equals(this.oD))
			return;
		fireVetoableChange("od", this.oD, od);
		Amount<Length> old = this.oD;
		this.oD = od;
		generateGeometry();
		firePropertyChange("OD", old, oD);
	}

	public Amount<Length> getID() {
		return iD;
	}

	public void setID(Amount<Length> id) throws PropertyVetoException {
		if (id.equals(this.iD))
			return;
		fireVetoableChange("id", this.iD, id);
		Amount<Length> old = this.iD;
		iD = id;
		generateGeometry();
		firePropertyChange("ID", old, iD);
	}
	
	public Amount<Length> getFinWidth() {
		return finWidth;
	}

	public void setFinWidth(Amount<Length> finWidth) throws PropertyVetoException {
		if (finWidth.equals(this.finWidth))
			return;
		fireVetoableChange("finWidth", this.finWidth, finWidth);
		Amount<Length> old = this.finWidth;
		this.finWidth = finWidth;
		generateGeometry();
		firePropertyChange("finWidth", old, finWidth);
	}
	
	
	public Amount<Length> getFinDiameter() {
		return finDiameter;
	}

	public void setFinDiameter(Amount<Length> finDiameter) throws PropertyVetoException {
		if (finDiameter.equals(this.finDiameter))
			return;
		fireVetoableChange("finDiameter", this.finDiameter, finDiameter);
		Amount<Length> old = this.finDiameter;
		this.finDiameter = finDiameter;
		generateGeometry();
		firePropertyChange("finDiameter", old, finDiameter);
	}
	
	
	public int getFinCount() {
		return finCount;
	}

	public void setFinCount(int finCount) throws PropertyVetoException {
		if (finCount==this.finCount)
			return;
		fireVetoableChange("finCount", this.finCount, finCount);
		int old = this.finCount;
		this.finCount = finCount;
		generateGeometry();
		firePropertyChange("finCount", old, finCount);
	}
	
	public static void main(String args[]) throws Exception {
		Finocyl e = new Finocyl();
		new Editor(e).showAsWindow();
		new GrainPanel(e).showAsWindow();
	}
}
