package com.billkuker.rocketry.motorsim.grain;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.ExtrudedShapeGrain;
import com.billkuker.rocketry.motorsim.visual.Editor;
import com.billkuker.rocketry.motorsim.visual.GrainPanel;

public class CSlot extends ExtrudedShapeGrain implements Validating {

	private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);
	private Amount<Length> iD = Amount.valueOf(0, SI.MILLIMETER);
	private Amount<Length> slotWidth = Amount.valueOf(5, SI.MILLIMETER);
	private Amount<Length> slotDepth = Amount.valueOf(15, SI.MILLIMETER);
	private Amount<Length> slotOffset = Amount.valueOf(0, SI.MILLIMETER);

	public CSlot(){
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
		this.oD = od;
		generateGeometry();
	}
	
	public Amount<Length> getSlotWidth() {
		return slotWidth;
	}

	public void setSlotWidth(Amount<Length> slotWidth) {
		this.slotWidth = slotWidth;
		generateGeometry();
	}

	public Amount<Length> getSlotDepth() {
		return slotDepth;
	}

	public void setSlotDepth(Amount<Length> slotDepth) {
		this.slotDepth = slotDepth;
		generateGeometry();
	}

	public Amount<Length> getSlotOffset() {
		return slotOffset;
	}

	public void setSlotOffset(Amount<Length> slotOffset) {
		this.slotOffset = slotOffset;
		generateGeometry();
	}

	private void generateGeometry() {
		double odmm = oD.doubleValue(SI.MILLIMETER);
		double wmm = slotWidth.doubleValue(SI.MILLIMETER);
		double dmm = slotDepth.doubleValue(SI.MILLIMETER);
		xsection = new BurningShape();
		Shape outside = new Ellipse2D.Double(0, 0, odmm, odmm);
		xsection.add(outside);
		xsection.inhibit(outside);
		
		double offmm = slotOffset.doubleValue(SI.MILLIMETER);
		
		double ymm = odmm/2.0 - wmm/2.0 - offmm; //The Y position of the slot
		double xmm = odmm - dmm; //X pos of slot
		Rectangle2D.Double slot;
		slot = new Rectangle2D.Double(xmm, ymm, dmm, wmm);
		xsection.subtract(slot);
		
		double idmm = iD.doubleValue(SI.MILLIMETER);
		double idymm = odmm/2.0 - idmm/2.0 - offmm; //y pos of id
		double idxmm = xmm - idmm/2.0; //x pos of id
		Ellipse2D.Double id = new Ellipse2D.Double(idxmm, idymm, idmm, idmm);
		xsection.subtract(id);
		
		webThickness = null;
	}
	
	public Amount<Length> getID() {
		return iD;
	}

	public void setID(Amount<Length> id) {
		iD = id;
		generateGeometry();
	}

	public static void main(String args[]) throws Exception {
		CSlot e = new CSlot();
		new Editor(e).showAsWindow();
		new GrainPanel(e).showAsWindow();
	}
	
	public void validate() throws ValidationException{
		if ( oD.equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid oD");
		if ( getLength().equals(Amount.ZERO) )
			throw new ValidationException(this, "Invalid Length");
	}

}
